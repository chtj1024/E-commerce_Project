import { useEffect, useState } from "react";
import axios from "axios";
import {
  deleteCartItem,
  getCartItems,
  updateCartItemQuantity,
} from "../api/cartApi";
import { createOrder } from "../api/orderApi";
import type { CartItemResponse } from "../types/cart";
import type { OrderResponse } from "../types/order";

const wonFormatter = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0,
});

type CartPageProps = {
  onBack: () => void;
  onOrderCreated: (order: OrderResponse) => void;
};

export default function CartPage({ onBack, onOrderCreated }: CartPageProps) {
  const [items, setItems] = useState<CartItemResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [pendingItemId, setPendingItemId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [isOrdering, setIsOrdering] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const loadCart = async () => {
      try {
        const response = await getCartItems();
        if (isMounted) setItems(response);
      } catch {
        if (isMounted) {
          setErrorMessage("장바구니를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    void loadCart();
    return () => {
      isMounted = false;
    };
  }, []);

  const changeQuantity = async (
    item: CartItemResponse,
    nextQuantity: number,
  ) => {
    if (nextQuantity < 1 || nextQuantity > item.stockQuantity) return;

    setPendingItemId(item.cartItemId);
    setErrorMessage("");

    try {
      const updatedItem = await updateCartItemQuantity(item.cartItemId, {
        quantity: nextQuantity,
      });

      setItems((currentItems) =>
        currentItems.map((currentItem) =>
          currentItem.cartItemId === updatedItem.cartItemId
            ? updatedItem
            : currentItem,
        ),
      );
    } catch {
      setErrorMessage("수량을 변경하지 못했습니다. 상품 재고를 확인해 주세요.");
    } finally {
      setPendingItemId(null);
    }
  };

  const removeItem = async (cartItemId: number) => {
    setPendingItemId(cartItemId);
    setErrorMessage("");

    try {
      await deleteCartItem(cartItemId);
      setItems((currentItems) =>
        currentItems.filter((item) => item.cartItemId !== cartItemId),
      );
    } catch {
      setErrorMessage("장바구니에서 상품을 삭제하지 못했습니다.");
    } finally {
      setPendingItemId(null);
    }
  };

  const cartTotal = items.reduce(
    (total, item) => total + item.totalPrice,
    0,
  );

  const submitOrder = async () => {
    if (items.length === 0 || isOrdering) return;

    setIsOrdering(true);
    setErrorMessage("");

    try {
      const order = await createOrder({
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });

      setItems([]);
      onOrderCreated(order);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 409) {
        setErrorMessage(
          "주문 중 재고가 변경되었습니다. 장바구니 수량을 다시 확인해 주세요.",
        );
      } else if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage("로그인이 만료되었습니다. 다시 로그인해 주세요.");
      } else {
        setErrorMessage("주문을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.");
      }
    } finally {
      setIsOrdering(false);
    }
  };

  return (
    <main className="cart-page">
      <section className="cart-panel">
        <div className="cart-heading">
          <div>
            <p className="eyebrow">MY CART</p>
            <h1>장바구니</h1>
          </div>
          <button className="secondary-button" type="button" onClick={onBack}>
            쇼핑 계속하기
          </button>
        </div>

        {errorMessage && (
          <p className="management-message error" role="alert">
            {errorMessage}
          </p>
        )}

        {isLoading && (
          <p className="product-state">장바구니를 불러오는 중입니다...</p>
        )}

        {!isLoading && items.length === 0 && (
          <div className="cart-empty">
            <strong>장바구니가 비어 있습니다.</strong>
            <p>마음에 드는 상품을 장바구니에 담아 보세요.</p>
            <button className="primary-button" type="button" onClick={onBack}>
              상품 보러 가기
            </button>
          </div>
        )}

        {!isLoading && items.length > 0 && (
          <div className="cart-layout">
            <div className="cart-list">
              {items.map((item) => {
                const isPending = pendingItemId === item.cartItemId;

                return (
                  <article className="cart-item" key={item.cartItemId}>
                    <img
                      src={item.imageUrl || "/icons.svg"}
                      alt={item.productName}
                      onError={(event) => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = "/icons.svg";
                      }}
                    />
                    <div className="cart-item-info">
                      <h2>{item.productName}</h2>
                      <p>{wonFormatter.format(item.price)}</p>
                      <span>재고 {item.stockQuantity}개</span>
                    </div>
                    <div className="quantity-control" aria-label={`${item.productName} 수량`}>
                      <button
                        type="button"
                        aria-label="수량 줄이기"
                        disabled={isPending || item.quantity <= 1}
                        onClick={() => void changeQuantity(item, item.quantity - 1)}
                      >
                        −
                      </button>
                      <strong>{item.quantity}</strong>
                      <button
                        type="button"
                        aria-label="수량 늘리기"
                        disabled={isPending || item.quantity >= item.stockQuantity}
                        onClick={() => void changeQuantity(item, item.quantity + 1)}
                      >
                        +
                      </button>
                    </div>
                    <div className="cart-item-actions">
                      <strong>{wonFormatter.format(item.totalPrice)}</strong>
                      <button
                        type="button"
                        disabled={isPending}
                        onClick={() => void removeItem(item.cartItemId)}
                      >
                        삭제
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>

            <aside className="cart-summary">
              <span>총 {items.length}종의 상품</span>
              <div>
                <span>총 결제 예정 금액</span>
                <strong>{wonFormatter.format(cartTotal)}</strong>
              </div>
              <button
                className="primary-button order-button"
                type="button"
                disabled={isOrdering || pendingItemId !== null}
                onClick={() => void submitOrder()}
              >
                {isOrdering ? "주문 생성 중..." : "주문하기"}
              </button>
            </aside>
          </div>
        )}
      </section>
    </main>
  );
}
