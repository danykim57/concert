# 1. 예약 확정 트랜잭션

아래는 결제 및 예약 상태 변경을 위한 함수입니다.

```kotlin
// 결제 및 예약 상태 변경
    @Transactional
    fun confirmReservation(reservationId: Long): String {

        val reservation = reservationService.get(reservationId)

        // 비관락이 들어감
        val user = userService.getUserWithLock(reservation.userId)

        concertService.get(reservation.concert.id)

        val seat = seatService.get(reservation.seat.id)!!

        val queue = queueService.get(reservation.userId)

        if (reservation.createdAt?.plusMinutes(5)!!.isBefore(LocalDateTime.now())) {
            //대기열 삭제
            queueService.delete(queue)
            // 좌석 예약 가능으로 변경
            seat.isAvailable = true
            seatService.save(seat)
            // 예약 취소처리
            reservation.status = ReservationStatus.CANCELLED
            reservationService.save(reservation)
            throw IllegalArgumentException("만료된 대기열 토큰입니다.")
        }


        // 상태를 결제 완료로 변경
        if (reservation.status != ReservationStatus.RESERVED) {
            throw IllegalArgumentException("결제 완료 처리를 할 수 없는 상태입니다.")
        }

        val point = userService.getPoint(user.id)

        // 포인트 잔액이 좌석 금액보다 적으면 예외 처리
        if (point.amount < reservation.seat.price) {
            throw IllegalArgumentException("포인트 잔액이 부족합니다.")
        }

        // 포인트 차감
        point.amount -= reservation.seat.price
        pointService.save(point)

        // 결제 정보 저장
        val payment = Payment(
            userId = reservation.userId,
            reservationId = reservation.id,
            amount = point.amount,
            type = PaymentType.SPEND
        )
        // 히스토리 저장
        paymentService.save(payment)

        // 예약 정보 저장
        reservation.status = ReservationStatus.CONFIRMED
//        reservationService.save(reservation)
        eventPublisher.publish(ReservationEvent.from(reservation))


        //대기열 삭제
        queueService.delete(queue)

        return ReservationMessage.CONFIRM.message
    }
```

절차지향식으로 되어있어서 난잡하여 대략적인 프로세스를 축약하자면 아래와 같습니다.
```
1. 검증: 사용자, 콘서트, 좌석
2. 잔여 포인트 검증
3. 예약 확인 처리
4. 사용자 포인트 차감
5. 지불 히스토리 저장
6. 예약 정보 저장
7. 대기열에서 해당 토큰 삭제
```

# 2. 문제점

---


- 예약 확정과 결제처리 히스토리 저장이 한 트랜잭션에 묶여있어서 복잡하고 결합도가 높게 되어있습니다.

- 트랜잭션을 오래 물게 되면서 DB 상에서의 성능 저하가 예상되고

- 수정에는 닫혀있고 확장에는 열려있는 이상적인 구조와는 정반대의 코드 구조입니다.


# 3. 해결책:

---

1. 객체지향식으로 검증 부분을 Entity로 가져온다
2. 비동기로 처리가능한 부분을 비동기로 전환하다
3. 트랜잭션을 쪼개어서 분산 트랜잭션으로 만든다

이번 주 핵심 내용인 Event를 이용해서 비동기로 전환을 하는 방법만 해보도록 하였습니다.

# 4. 짚고 넘어갈 부분

### 2PC(2 Phase Commit)

단일 DB가 아닌 다중 DB를 사용하면서 분산된 트랜잭션의 순서와 처리를 관리하기 위해서 사용됩니다.

트랜잭션을 쪼개기 때문에 복잡성이 늘고 네트워크 IO나 인프란 단의 장애에서 문제가 생길 수 있습니다.

### Saga Pattern

크게 Choreography와 Orchestration 방식이 있다.

Event를 릴레이 방식으로 하고 실패시 보상 Event를 작동시키는 것이 Choreography

미들웨어가 복잡한 Event관리를 가지고 가고 트랜잭션 실패시 미들웨어가 보상 트랜잭션을 보내는 방식이 Orchestration 입니다.


### 구현방식

Saga Pattern을 하기에는 시간이 없고 2PC 방식을 간소화 시켜서 하는 방식으로 진행합니다.



# 5. 작업 내용:


예약 정보를 저장하는 부분은 비동기로 처리하도록 변경하였습니다.


```kotlin
        // 예약 정보 저장
        reservation.status = ReservationStatus.CONFIRMED
//        reservationService.save(reservation)
        eventPublisher.publish(ReservationEvent.from(reservation))
```

프로젝트 전체 구조를 변경할 수 없어서 간단하게 EventListener와 EventPublisher를 사용하는 것으로 타협을 봤습니다.

부모 트랜잭션이 실패할 시의 보상 트랜잭션을 구현하지 않아서 트랜잭션 전파는 REQUIRED로 

이전의 결재 트랜잭션이 커밋이 완료되고 예약 확정 EventListener가 돌도록 AFTER_COMMIT을 추가하였습니다.
```kotlin
@Component
class ExternalEventListener(
    private val eventApiClient: EventApiClient
): EventListener<ReservationEvent> {
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: ReservationEvent) {
        eventApiClient.sendReservationEvent()
    }
}
```

```kotlin
@Component
class ExternalEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
): EventPublisher<ReservationEvent>{

    override fun publish(event: ReservationEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
```


