import http from 'k6/http';
import { check, fail } from 'k6';
import { Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const productId = '2';

const requestRate = Number(__ENV.RATE || 10);
const preAllocatedVUs = Number(__ENV.PREALLOCATED_VUS || 20);
const maxVUs = Number(__ENV.MAX_VUS || 100);

const productReadDuration = new Trend(
    'warm_product_read_duration',
    true,
);

export const options = {
    discardResponseBodies: true,

    scenarios: {
        warm_product_read: {
            executor: 'constant-arrival-rate',

            // 초당 요청 수
            rate: requestRate,
            timeUnit: '1s',

            duration: __ENV.DURATION || '2m',

            preAllocatedVUs,
            maxVUs,
        },
    },

    summaryTrendStats: [
        'avg',
        'min',
        'med',
        'max',
        'p(90)',
        'p(95)',
        'p(99)',
    ],

    thresholds: {
        warm_product_read_duration: ['p(95)<200'],
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },
};

function createHeaders() {
    const headers = {
        Accept: 'application/json',
    };

    if (__ENV.TOKEN) {
        headers.Authorization = `Bearer ${__ENV.TOKEN}`;
    }

    return headers;
}

export function setup() {
    // 본 테스트 전에 상품 2를 조회해 캐시를 예열한다.
    const response = http.get(
        `${baseUrl}/api/products/${productId}`,
        {
            headers: createHeaders(),
            tags: {
                name: 'GET /api/products/2 - warm-up',
                cache_mode: 'warm-up',
                request_type: 'warm-up',
            },
        },
    );

    const warmedUp = check(response, {
        '캐시 예열 요청의 HTTP 상태가 200이다': (res) =>
            res.status === 200,
    });

    if (!warmedUp) {
        fail(`캐시 예열 실패: HTTP ${response.status}`);
    }
}

export default function () {
    const response = http.get(
        `${baseUrl}/api/products/${productId}`,
        {
            headers: createHeaders(),
            tags: {
                name: 'GET /api/products/2',
                cache_mode: 'warm',
                request_type: 'load-test',
            },
        },
    );

    productReadDuration.add(response.timings.duration);

    check(response, {
        '상품 조회 HTTP 상태가 200이다': (res) =>
            res.status === 200,
    });
}