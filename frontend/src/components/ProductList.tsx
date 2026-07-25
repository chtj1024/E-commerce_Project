import { useEffect, useState } from "react";
import { getProducts } from "../api/productApi";
import type { ProductResponse } from "../types/product";

const wonFormatter = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0,
});

export default function ProductList() {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    const loadProducts = async () => {
      try {
        const response = await getProducts();
        if (isMounted) setProducts(response);
      } catch {
        if (isMounted) setErrorMessage("상품을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    void loadProducts();
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <section className="product-section" aria-labelledby="product-section-title">
      <div className="product-section-heading">
        <div>
          <p className="eyebrow">NEW PRODUCTS</p>
          <h2 id="product-section-title">새로 들어온 상품</h2>
        </div>
        {!isLoading && !errorMessage && (
          <span className="product-count">{products.length}개의 상품</span>
        )}
      </div>

      {isLoading && <p className="product-state">상품을 불러오는 중입니다...</p>}
      {errorMessage && <p className="product-state error" role="alert">{errorMessage}</p>}
      {!isLoading && !errorMessage && products.length === 0 && (
        <p className="product-state">아직 등록된 상품이 없습니다.</p>
      )}

      {products.length > 0 && (
        <div className="product-grid">
          {products.map((product) => (
            <article className="shop-product-card" key={product.id}>
              <div className="product-image-wrap">
                <img
                  src={product.imageUrl || "/icons.svg"}
                  alt={product.name}
                  loading="lazy"
                  onError={(event) => {
                    event.currentTarget.onerror = null;
                    event.currentTarget.src = "/icons.svg";
                  }}
                />
                {product.status === "SOLD_OUT" && (
                  <span className="sold-out-badge">품절</span>
                )}
              </div>
              <div className="product-card-body">
                <h3>{product.name}</h3>
                <p className="product-description">{product.description}</p>
                <div className="product-card-footer">
                  <strong>{wonFormatter.format(product.price)}</strong>
                  <span>{product.status === "SOLD_OUT" ? "재고 없음" : `재고 ${product.stockQuantity}개`}</span>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
