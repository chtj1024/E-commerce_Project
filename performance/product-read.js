import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const productId = __ENV.PRODUCT_ID || '2';

const requestRate = Number(__ENV.RATE || 10);
const preAllocatedVUs = Number(__ENV.PREALLOCATED_VUS || 20);
const maxVUs = Number(__ENV.MAX_VUS || 100);

export const options = {
    discardResponseBodies: true,

    summaryTrendStats: [
        'avg',
        'min',
        'med',
        'max',
        'p(90)',
        'p(95)',
        'p(99)'
    ],

    scenarios: {
        product_read: {
            executor: 'constant-arrival-rate',

            // 초당 요청 수
            rate: requestRate,
            timeUnit: '1s',

            // 테스트 시간
            duration: __ENV.DURATION || '2m',

            preAllocatedVUs,
            maxVUs,
        },
    },

    thresholds: {
        // 실패율 1% 미만
        http_req_failed: ['rate<0.01'],

        // 전체 요청의 95%가 500ms 미만
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    const headers = {
        Accept: 'application/json',
    };

    if (__ENV.TOKEN) {
        headers.Authorization = `Bearer ${__ENV.TOKEN}`;
    }

    const response = http.get(
        `${baseUrl}/api/products/${productId}`,
        {
            headers,
            tags: {
                name: 'GET /api/products/:productId',
            },
        },
    );

    check(response, {
        'HTTP 상태가 200이다': (res) => res.status === 200,
    });
}