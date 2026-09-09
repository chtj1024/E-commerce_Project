import { useEffect, useState } from "react";
import type { OrderResponse } from "../types/order";

const wonFormatter = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0,
});

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  dateStyle: "medium",
  timeStyle: "short",
});

type OrderPendingPageProps = {
  orderId: number | null;
  order: OrderResponse | null;
  onBack: () => void;
};

export default function OrderPendingPage({
  orderId,
  order,
  onBack,
}: OrderPendingPageProps) {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const timerId = window.setInterval(() => setNow(Date.now()), 1_000);
    return () => window.clearInterval(timerId);
  }, []);

  const expiresAt = order ? new Date(order.expiresAt) : null;
  const isExpired =
    order?.status === "EXPIRED" ||
    (expiresAt !== null && expiresAt.getTime() <= now);

  return (
    <main className="order-pending-page">
      <section className="order-pending-card">
        <span className="order-status-icon" aria-hidden="true">
          {isExpired ? "!" : "✓"}
        </span>
        <p className="eyebrow">ORDER CREATED</p>
        <h1>{isExpired ? "결제 대기 시간이 만료됐어요" : "주문이 생성됐어요"}</h1>
        <p className="order-pending-description">
          {isExpired
            ? "예약 재고는 서버에서 자동으로 복구됩니다. 새 주문을 진행해 주세요."
            : "결제가 완료될 때까지 상품 재고가 예약됩니다."}
        </p>

        <dl className="order-summary-list">
          <div>
            <dt>주문번호</dt>
            <dd>#{orderId ?? "-"}</dd>
          </div>
          {order && (
            <>
              <div>
                <dt>결제 예정 금액</dt>
                <dd>{wonFormatter.format(order.totalPrice)}</dd>
              </div>
              <div>
                <dt>결제 가능 시간</dt>
                <dd>{dateFormatter.format(expiresAt!)}</dd>
              </div>
            </>
          )}
        </dl>

        {!order && (
          <p className="management-message error" role="alert">
            주문 상세 정보를 표시할 수 없습니다. 상품 목록으로 돌아가 새 주문을
            진행해 주세요.
          </p>
        )}

        {!isExpired && order && (
          <p className="payment-placeholder">
            결제 수단 연동 전 단계입니다. PG 연동 후 이 위치에 결제 버튼을
            연결합니다.
          </p>
        )}

        <button className="primary-button" type="button" onClick={onBack}>
          상품 목록으로
        </button>
      </section>
    </main>
  );
}
