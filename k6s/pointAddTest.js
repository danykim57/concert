import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 3,
    iterations: 3,
};

const userId = "123e4567-e89b-12d3-a456-426614174000";
const url = `http://localhost:8080/api/user/point/add`;

const pointRequests = [
    JSON.stringify({ amount: 10, userId: userId }),
    JSON.stringify({ amount: 20, userId: userId }),
    JSON.stringify({ amount: 30, userId: userId })
];

export default function () {

    const vuId = __VU;

    const payload = pointRequests[vuId - 1];

    // HTTP 요청 헤더 설정
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.put(url, payload, params);


    check(response, {
        'status is 200': (r) => r.status === 200,
        'response has correct point': (r) => r.json().point === (10 * vuId + (vuId - 1) * 10),
    });

    sleep(2);
}
