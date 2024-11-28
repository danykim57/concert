import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션 설정
export let options = {
    vus: 50, // 가상 사용자 수
    duration: '5m', // 테스트 실행 시간
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95%의 요청이 2초 이내에 완료되어야 함
    },
};

const BASE_URL = 'http://app:8080';

export function setup() {
    const initialUserId = 1;
    const setupRes = http.post(`${BASE_URL}/api/v1/token`,
        JSON.stringify({ userId: initialUserId }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    return { setupToken: setupRes.json().data.queueToken };
}

export default function () {
    // POST 요청: 예약 확인
    const reservationId = Math.floor(Math.random() * 10000) + 1; // 무작위 예약 ID 생성
    const response = http.post(`${BASE_URL}/reservations/confirm`,
        JSON.stringify({ reservationId: reservationId }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    // 응답 상태 확인
    check(response, {
        'status is 200': (r) => r.status === 200,
    });

    // 사용자별 대기
    sleep(1);
}


export function setup() {
    const initialUserId = 1;
    const setupRes = http.post(`${BASE_URL}/queue/token`,
        JSON.stringify({ userId: initialUserId }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    return { setupToken: setupRes.json().data.queueToken };
}

export default function (data) {
    const userId = Math.floor(Math.random() * 100000) + 1; // 사용자 ID 범위: 1 ~ 100,000

    // 1. 토큰 발급
    const tokenRes = http.post(`${BASE_URL}/queue/token`,
        JSON.stringify({ userId: userId }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(tokenRes, { '토큰 생성 성공': (r) => r.status === 200 });
    const token = tokenRes.json().data.queueToken;

    // 2. 대기열 확인
    let queueStatus = 'WAITING';
    let attempts = 0;
    const maxAttempts = 3;
    const waitTime = 1;

    while (queueStatus !== 'SUCCESS' && attempts < maxAttempts) {
        const queueRes = http.post(`${BASE_URL}/queue/token/check`, null, {
            headers: { 'Content-Type': 'application/json', 'Authorization': token }
        });

        check(queueRes, { '대기열 확인 성공': (r) => r.status === 200 });
        queueStatus = queueRes.status === 200 ? queueRes.json().data.status : queueStatus;

        if (queueStatus === 'SUCCESS') break;

        attempts++;
        sleep(waitTime); // 대기
    }

    if (queueStatus === 'SUCCESS') {
        // 3. 콘서트 스케줄 조회
        const scheduleRes = http.get(`${BASE_URL}/concerts/schedule`, {
            headers: { 'Content-Type': 'application/json', 'Authorization': token }
        });

        check(res, { '스케줄 조회 성공': (r) => r.status === 200 });

        if (res.status === 200 && res.json().data.length > 0) {
            const seatId = res.json().data.first().seatId;

            // 4. 좌석 조회
            const seatRes = http.get(`${BASE_URL}/concerts/seat?=${seatId}`, {
                headers: { 'Content-Type': 'application/json', 'Authorization': token }
            });

            check(seatRes, { '좌석 조회 성공': (r) => r.status === 200 });
        }
    } else {
        console.warn(`사용자 ${userId}은 PROGRESS 상태에 도달하지 못했습니다.`);
    }

    sleep(1);
}

export function teardown(data) {
    console.log('테스트 완료');
}
