# Redis Cache Performance Test

상품 단건 조회 API의 기본 상태와 Redis Cold/Warm Cache 상태를 비교합니다.

## k6 시나리오

- `k6/scripts/product-read-baseline.js`: 기본 상품 단건 조회
- `k6/scripts/product-read-cold.js`: 캐시를 제거한 뒤 상품 조회
- `k6/scripts/product-read-warm.js`: 캐시를 예열한 뒤 상품 조회

## 결과 디렉터리

- `k6/results/baseline`: 기본 조회 결과
- `k6/results/cold`: Cold Cache 결과
- `k6/results/warm`: Warm Cache 결과

결과 파일은 실행 순서대로 `run-01.json`, `run-02.json` 형식을 사용합니다.
