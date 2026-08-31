# Product Search Index Tuning

20만 건의 상품 데이터에서 가격 범위 검색 SQL의 실행계획을 분석하고, `(price ASC, id DESC)` 복합 인덱스 적용 전후를 `EXPLAIN ANALYZE`와 k6로 검증했습니다.

## 1. 문제와 측정 대상

다음 상품 검색 API는 요청 한 번에 목록 SQL과 전체 개수를 구하는 COUNT SQL을 실행합니다.

```http
GET /api/products?minPrice=50000&maxPrice=60000&page=0&size=12&sort=priceAsc
```

핵심 SQL 조건은 다음과 같습니다.

```sql
WHERE status <> 'HIDDEN'
  AND price BETWEEN 50000 AND 60000
ORDER BY price ASC, id DESC
LIMIT 12;
```

인덱스 적용 전 실행계획에서 20만 건 전체를 탐색하고, 조건을 통과한 9,551건을 정렬한 뒤 12건을 반환하는 병목을 확인했습니다.

## 2. 인덱스 설계

```sql
CREATE INDEX idx_product_price_id
    ON product (price ASC, id DESC);
```

- `price`: `BETWEEN` 범위 조건과 첫 번째 정렬 기준
- `id`: 같은 가격의 상품 순서를 결정하는 두 번째 정렬 기준
- `status` 제외: 값 종류가 적고 `<> 'HIDDEN'` 조건이므로 선두 컬럼의 선택도가 낮다고 판단

검증된 인덱스는 `src/main/resources/db/migration/V3__add_product_composite-index.sql`에서 Flyway 버전 마이그레이션으로 관리합니다.

## 3. 실험 조건

| 항목 | 조건 |
| --- | --- |
| 데이터 | 상품 200,000건 |
| 가격 범위 | 50,000원 이상 60,000원 이하 |
| 정렬 | `price ASC, id DESC` |
| 페이지 | `page=0`, `size=12` |
| 실행계획 | Before/After 각각 5회 |
| k6 | 10 RPS, 2분, Before/After 각각 3회 |
| 대표값 | 반복 실행의 중앙값 |

인덱스 적용 전후에 동일한 Hibernate SQL, 데이터, 검색 조건을 사용했습니다.

## 4. EXPLAIN ANALYZE 결과

### 목록 SQL

| 항목 | Before | After |
| --- | ---: | ---: |
| 실행시간 5회 | 325 / 344 / 166 / 175 / 185 ms | 1.82 / 0.178 / 0.135 / 0.147 / 0.43 ms |
| 중앙값 | 185 ms | 0.178 ms |
| 접근 방식 | Table scan | Index range scan |
| 전체 탐색 | 200,000건 | 필요한 12건을 찾은 뒤 종료 |
| 별도 Sort | 발생 | 제거 |
| 사용 인덱스 | 없음 | `idx_product_price_id` |

목록 SQL 중앙값은 185ms에서 0.178ms로 약 99.9% 감소했습니다. 인덱스 적용 후에는 가격 범위를 정렬된 순서로 탐색해 `LIMIT 12`에 필요한 행을 확보한 즉시 중단했습니다.

### COUNT SQL

| 항목 | Before | After |
| --- | ---: | ---: |
| 실행시간 5회 | 253 / 80 / 198 / 82.8 / 84.5 ms | 118 / 81.5 / 29.4 / 24.2 / 21.1 ms |
| 중앙값 | 84.5 ms | 29.4 ms |
| 접근 방식 | Table scan | Index range scan |
| 가격 검사 대상 | 200,000건 | 10,051건 |
| 최종 COUNT 대상 | 9,551건 | 9,551건 |

COUNT SQL 중앙값은 약 65.2% 감소했습니다. 목록 조회와 달리 전체 건수를 계산해야 하므로 가격 범위의 10,051건을 모두 확인한 뒤 `HIDDEN` 500건을 제외했습니다.

## 5. k6 API 결과

`constant-arrival-rate`로 10 RPS를 2분간 유지하고 Before/After를 각각 3회 측정했습니다.

| 실행 | 평균 | 중앙값 | p95 | p99 | 요청률 | dropped |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Before 1 | 7,842.72 ms | 8,694.76 ms | 17,920.23 ms | 18,423.40 ms | 8.19 req/s | 81 |
| Before 2 | 606.95 ms | 587.80 ms | 1,083.85 ms | 1,383.84 ms | 9.93 req/s | 0 |
| Before 3 | 704.27 ms | 663.26 ms | 1,333.63 ms | 1,497.33 ms | 9.91 req/s | 0 |
| After 1 | 29.96 ms | 26.27 ms | 51.40 ms | 74.48 ms | 10.00 req/s | 0 |
| After 2 | 25.04 ms | 23.94 ms | 29.67 ms | 36.81 ms | 10.00 req/s | 0 |
| After 3 | 28.35 ms | 25.57 ms | 39.86 ms | 80.27 ms | 10.00 req/s | 0 |

3회 중앙값 비교는 다음과 같습니다.

| 항목 | Before | After | 변화 |
| --- | ---: | ---: | ---: |
| 평균 응답시간 | 704.27 ms | 28.35 ms | 약 96.0% 감소 |
| 응답시간 중앙값 | 663.26 ms | 25.57 ms | 약 96.1% 감소 |
| p95 | 1,333.63 ms | 39.86 ms | 약 97.0% 감소 |
| p99 | 1,497.33 ms | 74.48 ms | 약 95.0% 감소 |
| HTTP 실패율 | 0% | 0% | 동일 |

Before 1회차는 DB 처리 지연이 누적되면서 목표 요청률을 유지하지 못하고 81건이 dropped된 이상치였습니다. 결과에서 제외하거나 숨기지 않고 원문을 보존했으며, 단일 실행이 결론을 왜곡하지 않도록 세 번의 중앙값을 대표값으로 사용했습니다. 적용 후에는 세 번 모두 목표 10 RPS를 유지했고 dropped iteration과 HTTP 실패가 없었습니다.

## 6. 결론

전체 탐색과 별도 정렬을 실행계획으로 확인한 뒤 실제 필터·정렬 순서에 맞춘 복합 인덱스를 적용했습니다. SQL 단위에서는 목록과 COUNT가 모두 개선됐고, API 단위에서도 p95가 약 97% 감소해 병목 원인과 개선 효과가 같은 방향으로 확인됐습니다.

이번 결과가 증명하는 범위는 **가격 범위 + `priceAsc` + 첫 페이지 조회**입니다. 키워드 부분 검색, 카테고리 검색, 최신순·가격 내림차순 및 깊은 OFFSET 페이지까지 동일하게 최적화한다고 일반화하지 않습니다.

## 7. 구성

- `k6/scripts/product-search.js`: 상품 검색 API 부하 테스트
- `k6/results/before`: 인덱스 적용 전 k6 원본 결과
- `k6/results/after`: 인덱스 적용 후 k6 원본 결과
- `explain-analyze/sql/00-inspect.sql`: DB와 기존 인덱스 확인
- `explain-analyze/sql/01-seed-products.sql`: 성능 테스트 데이터 생성
- `explain-analyze/sql/02-before.sql`: 인덱스 적용 전 실행계획
- `explain-analyze/sql/03-create-index.sql`: 후보 인덱스 생성
- `explain-analyze/sql/04-after.sql`: 인덱스 적용 후 실행계획
- `explain-analyze/sql/05-drop-index.sql`: 후보 인덱스 제거
- `explain-analyze/results/before`: 인덱스 적용 전 실행계획 원문
- `explain-analyze/results/after`: 인덱스 적용 후 실행계획 원문
