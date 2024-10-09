# 콘서트 예약 서비스

## 요구사항

- 기능
  - 유저 토큰 발급 API
  - 예약 가능 날짜 / 좌석 API
  - 좌석 예약 요청 API
  - 잔액 충전 / 조회 API
  - 결제 API
- 테스트
  - 기능 및 제약사항에 대한 테스트 (하나 이상)
  - 복수의 서버 인스턴스에서도 기능하도록 작성
  - 동시성 이슈 고려
  - 대기열 개념 고려
- 개발 환경
  - Architecture
      - Testable Business logics
      - 클린 + Layered Architecture
  - DB ORM: JPA
  - Test: JUnit

## 일정 마일스톤 

### 3주차
- 시나리오 분석 및 작업 계획 (시퀀스 다이어그램, 플로우 차트)
- ERD 설계 자료, API 명세, Mock API 작성, 패키지 구조, 서버 Config

### 4주차:
- 유저 대기열 토큰 기능 구현 및 테스트 코드 작성
- 예약 가능 날짜/ 좌석 API, 좌석 예약 요청 API 기능 구현 및 테스트 코드 작성
- 잔액 충전 / 조회 API 기능 구현 및 테스트 코드 작성

### 5주차:
- 결제 API 기능 구현 및 테스트 코드 작성
- 대기열 고도화

### 패키지 구조

```
/src
    /interfaces
        /api
            /concert
                ConcertController.kt
            /point
                PointController.kt
            /seat
                SeatController.kt
    /application
        /concert
            ConcertFacade.kt
            ConcertService.kt
        /point
            PointService.kt
        /seat
            UserService.kt
        /user
            UserService.kt
            
    /domain
        /concert
            Concert.kt
            ConcertRepository.kt 
        /point
            Point.kt
            PointRepository.kt
        /user
            User.kt
            UserRepository.kt
        /registration
            Registration.kt
            RegistrationRepository.kt
        /queue
    /infrastructure
        /concert
        /point
        /seat
        /user
```

## ERD 설계
![erd1.png](/erd1.png)
## 시퀀스 다이어그램
#### 유저 토큰 발급
```mermaid
sequenceDiagram
    participant User as 사용자
    participant API_Server as TokenController
    participant Queue_System as QueueService
    User->>API_Server: 토큰 발급 요청
    API_Server->>Queue_System: 사용자 대기열 확인
    Queue_System-->>API_Server: 대기열 정보
    API_Server-->>User: 토큰 발급 및 대기열 정보 반환
```

#### 예약 가능 날짜
```mermaid
sequenceDiagram
    participant User as User
    participant Server as Concert Server
    participant DB as Database

    User->>Server: 예약 가능 
    Server->>DB: Query available concerts
    DB-->>Server: Return concert list
    Server-->>User: Send concert list
```

#### 좌석 조회
```mermaid
sequenceDiagram
    participant User as User
    participant UI as Concert Booking UI
    participant Server as Concert Server
    participant DB as Database

    User->>UI: Request available seats for concert
    UI->>Server: Fetch available seats (concert ID)
    Server->>DB: Query available seats (concert ID)
    DB-->>Server: Return available seats list
    Server-->>UI: Send available seats list
    UI-->>User: Display available seats
```

#### 좌석 예약 요청
```mermaid
sequenceDiagram
    participant User as User
    participant Server as Concert Server
    participant DB as Database
    
    User->>Server: Select seat and request reservation
    Server->>DB: Check seat availability (seat ID)
    DB-->>Server: Seat available
    Server->>DB: Reserve seat (seat ID, user ID)
    DB-->>Server: Confirm reservation
    Server-->>User: Reservation success response
```
#### 잔액 충전
```mermaid
sequenceDiagram
    participant User as User
    participant PaymentGateway as Payment Gateway
    participant Server as Wallet Server
    participant DB as Wallet Database

    User->>PaymentGateway: Submit payment details
    PaymentGateway-->>UI: Payment success
    UI->>Server: Notify server of successful payment (amount, user ID)
    Server->>DB: Update balance in user account (user ID, amount)
    DB-->>Server: Balance update success
    Server-->>User: Confirm balance recharge
```
#### 잔액 조회
```mermaid
sequenceDiagram
    participant User as User
    participant Server as Wallet Server
    participant DB as Wallet Database

    User->>Server: Request to check balance
    Server->>DB: Query balance for user (user ID)
    DB-->>Server: Return user balance
    Server-->>User: Send balance information
```
## API 명세

#### 유저 토큰 발급
- **GET** ```/token```
- Request
  - ```json
    {
      "id": "id", 
      "password": "password"
    }

- Response
  - ```200```
    - ```json
      { 
        "code": "success", 
        "message": "token provided",
        "token": "provided"
      }
  - cookie에 토큰 주입
  - ```401```
    - ```json
      {
        "code": "fail",
        "message": "authentication failed"
      }

#### 예약 가능한 콘서트 조회
- **GET** ```/concert```
- Response
  - ```200```
    - ```json
      {
        "code": "success",
        "concerts": []
      }

#### 예약 가능 좌석 조회
- **GET** ```/seat/{concertId} ```
- Response
  - ```200```
  - ```json
    {
      "code": "success",
      "seats": []
    }

#### 콘서트 예약 하기
- **POST** ```/seat/{concertId} ```
- Request
  - ```json
    {
      "code": "success",
      "concert": {
                    "name" : "concert",
                    "date" : "2024-12-01"
                  }
    } 
    
- Response
  - ```409```
  - ```json
    {
      "code": "fail",
      "message": "Invalid request, already occupied"
    }

#### 유저 포인트 조회
- **GET** ```/user/{userId}/point```
- Response
  - ```200```
  - ```json
    {
      "code": "success",
      "point": "123"
    }
#### 유저 포인트 충전
- **POST** ```/user/{userId}/charge ```
- Request
  - ```json
    {
      "amount": "amount"
    } 
- Response
  - ```200```
  - ```json
    {
      "code": "success",
      "point": "246"
    }

## Mock API