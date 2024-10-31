import http from 'k6/http';
import { check } from 'k6';

// 테스트 옵션 설정
export let options = {
    target: 1
};

export default function () {

    const url = 'http://localhost:8080/api/test/user';

    const response = http.get(url);

    check(response, {
        'status is 200': (r) => r.status === 200,
        'response has id': (r) => r.json().message === "success"
    });
}
