import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 50,
    target: 50,
    duration: '1s',
    noConnectionReuse: true,
};

export default function () {
    let seatId = 1;

    let url = 'http://localhost:8080/api/seat/reserve';

    let payload = JSON.stringify({ seatId: seatId });

    let params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let response = http.post(url, payload, params);
    console.log(response)

    check(response, {
        'status is 200': (r) => r.status === 200,
        'reservation successful': (r) => r.json().isAvailable === false,
    });
}
