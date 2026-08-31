import http from 'k6/http';
import { check, fail } from 'k6';
import { Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const testType = __ENV.TEST_TYPE || 'smoke';

const searchDuration = new Trend(
    'product_search_duration',
    true,
);

function createUrl() {
    return (
        `${baseUrl}/api/products`
        + '?minPrice=50000'
        + '&maxPrice=60000'
        + '&page=0'
        + '&size=12'
        + '&sort=priceAsc'
    );
}

function createScenario() {
    if (testType === 'smoke') {
        return {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '30s',
        };
    }

    if (testType === 'load') {
        return {
            executor: 'constant-arrival-rate',
            rate: Number(__ENV.RATE || 10),
            timeUnit: '1s',
            duration: __ENV.DURATION || '2m',
            preAllocatedVUs: Number(
                __ENV.PREALLOCATED_VUS || 50,
            ),
            maxVUs: Number(__ENV.MAX_VUS || 200),
        };
    }

    throw new Error(
        `지원하지 않는 TEST_TYPE입니다: ${testType}`,
    );
}

export const options = {
    discardResponseBodies: true,

    scenarios: {
        product_search: createScenario(),
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
        product_search_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },
};

export function setup() {
    // 본 측정 전에 서버, JVM, 커넥션 풀, DB 버퍼를 예열한다.
    const response = http.get(createUrl(), {
        tags: {
            name: 'GET /api/products - warm-up',
            request_type: 'warm-up',
        },
    });

    const success = check(response, {
        '서버 워밍업에 성공했다': (res) =>
            res.status === 200,
    });

    if (!success) {
        fail(`워밍업 실패: HTTP ${response.status}`);
    }
}

export default function () {
    const response = http.get(createUrl(), {
        tags: {
            name: 'GET /api/products',
            request_type: testType,
        },
    });

    searchDuration.add(response.timings.duration);

    check(response, {
        '검색 API 상태가 200이다': (res) =>
            res.status === 200,
    });
}