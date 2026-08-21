# 이커머스 프로젝트

대용량 트래픽을 고려한 상품 검색, 주문 및 결제 기능을 구현한 Spring Boot 기반 이커머스 백엔드 프로젝트 입니다.



## 사용된 기술

* Java, Spring Boot
* React, TypeScript

패키지 구조 : 도메인

jwt (access token, refresh token)

상품등록 validation  
페이징  
QueryDSL 검색(키워드 검색, 카테고리 검색, 가격 범위 검색, 복합 조건 검색)  
관리자 상품 변경 낙관적 락으로, 재고 및 주문 상태와 결제 실패는 조건부 UPDATE로 동시성 처리.  
db 복합 인덱스 설정  
스냅샷 설정  
양방향 매핑  
■ 테스트  
- 주문 생성시 재고 차감
- 복수상품 중 하나의 재고가 부족하면 전체 차감 롤백
- 결제실패시 차감된 재고 복구
- 결제실패 중복호출되도 재고 한번만 복구
- 결제대기시간 만료시 재고 복구
- 만료처리 중복해도 재고 한번만 복구
- 만료되지 않은 주문은 처리하지 않는다  
공통 예외 처리 (ErrorCode, CustomException, ErrorResponse, GlobalExceptionHandler) 


## 담당



## 구동 영상



## 참고

