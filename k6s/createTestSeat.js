import http from 'k6/http';
import { check } from 'k6';

// 테스트 옵션 설정
export let options = {
    target: 1
};

export default function () {

    const url = 'http://localhost:8080/api/seat/saveForTest';

    const response = http.get(url);

    check(response, {
        'status is 200': (r) => r.status === 200,  // 상태 코드가 200인지 확인
        'response has id': (r) => r.json().seat.id !== undefined,  // 응답에 ID가 있는지 확인
        'seat is available': (r) => r.json().seat.isAvailable === true // 좌석이 사용 가능 상태인지 확인
    });
}
