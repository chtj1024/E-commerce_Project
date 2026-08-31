import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const productId = '2';

// 예: http://localhost:8080/internal/cache/products/2
const cacheEvictUrl = __ENV.CACHE_EVICT_URL;

const productReadDuration = new Trend(
    'cold_product_read_duration',
    true,
);

export const options = {
    discardResponseBodies: true,

    scenarios: {
        cold_product_read: {
            executor: 'per-vu-iterations',

            // Cold 상태 보장을 위해 반드시 1 VU로 순차 실행
            vus: 1,

            // Cold cache 측정 횟수
            iterations: Number(__ENV.SAMPLES || 30),

            maxDuration: __ENV.MAX_DURATION || '5m',
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
        cold_product_read_duration: ['p(95)<500'],
        checks: ['rate>0.99'],
    },
};

export function setup() {
    if (!cacheEvictUrl) {
        fail(
            'Cold 테스트에는 CACHE_EVICT_URL 환경변수가 필요합니다.',
        );
    }
}

function createHeaders() {
    const headers = {
        Accept: 'application/json',
    };

    if (__ENV.TOKEN) {
        headers.Authorization = `Bearer ${__ENV.TOKEN}`;
    }

    return headers;
}

export default function () {
    const headers = createHeaders();

    /*
     * 상품 2의 캐시만 삭제한다.
     *
     * 캐시 삭제 API가 POST 방식이라면:
     * http.del(...) 대신 http.post(...)로 변경한다.
     */
    const evictResponse = http.del(
        cacheEvictUrl,
        null,
        {
            headers,
            tags: {
                name: 'DELETE product cache',
                request_type: 'cache-eviction',
            },
        },
    );

    const cacheEvicted = check(evictResponse, {
        '상품 2 캐시 삭제에 성공했다': (res) =>
            res.status === 200 || res.status === 204,
    });

    if (!cacheEvicted) {
        fail(`캐시 삭제 실패: HTTP ${evictResponse.status}`);
    }

    // 캐시 삭제 반영을 위한 짧은 대기 시간
    sleep(Number(__ENV.EVICT_WAIT || 0.1));

    const response = http.get(
        `${baseUrl}/api/products/${productId}`,
        {
            headers,
            tags: {
                name: 'GET /api/products/2',
                cache_mode: 'cold',
                request_type: 'load-test',
            },
        },
    );

    // 캐시 삭제 요청 시간을 제외하고 상품 조회 시간만 기록
    productReadDuration.add(response.timings.duration);

    check(response, {
        '상품 조회 HTTP 상태가 200이다': (res) =>
            res.status === 200,
    });
}