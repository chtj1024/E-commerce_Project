import { type FormEvent, useEffect, useState } from "react";
import { getProducts, type ProductSort } from "../api/productApi";
import { addCartItem } from "../api/cartApi";
import type {
  PageResponse,
  ProductResponse,
  ProductSearchCondition,
} from "../types/product";

const PAGE_SIZE = 12;
const EMPTY_CONDITION: ProductSearchCondition = {};

const wonFormatter = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0,
});

function parseOptionalNumber(value: string | null): number | undefined {
  if (value === null || value.trim() === "") return undefined;

  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined;
}

function getInitialPage() {
  const page = Number(new URLSearchParams(window.location.search).get("page"));
  return Number.isInteger(page) && page >= 0 ? page : 0;
}

function getInitialSort(): ProductSort {
  const sort = new URLSearchParams(window.location.search).get("sort");
  return sort === "priceAsc" || sort === "priceDesc" ? sort : "latest";
}

function getInitialCondition(): ProductSearchCondition {
  const params = new URLSearchParams(window.location.search);
  return {
    keyword: params.get("keyword") || undefined,
    category: params.get("category") || undefined,
    minPrice: parseOptionalNumber(params.get("minPrice")),
    maxPrice: parseOptionalNumber(params.get("maxPrice")),
  };
}

type ProductListProps = {
  isLoggedIn: boolean;
  onLoginRequired: () => void;
};

export default function ProductList({
  isLoggedIn,
  onLoginRequired,
}: ProductListProps) {
  const [initialCondition] = useState(getInitialCondition);
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [page, setPage] = useState(getInitialPage);
  const [sort, setSort] = useState<ProductSort>(getInitialSort);
  const [condition, setCondition] =
    useState<ProductSearchCondition>(initialCondition);
  const [keyword, setKeyword] = useState(initialCondition.keyword ?? "");
  const [category, setCategory] = useState(initialCondition.category ?? "");
  const [minPrice, setMinPrice] = useState(
    initialCondition.minPrice?.toString() ?? "",
  );
  const [maxPrice, setMaxPrice] = useState(
    initialCondition.maxPrice?.toString() ?? "",
  );
  const [pageInfo, setPageInfo] =
    useState<PageResponse<ProductResponse> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [cartMessage, setCartMessage] = useState("");
  const [addingProductId, setAddingProductId] = useState<number | null>(null);

  const handleAddToCart = async (product: ProductResponse) => {
    if (!isLoggedIn) {
      onLoginRequired();
      return;
    }

    setAddingProductId(product.id);
    setCartMessage("");

    try {
      await addCartItem({ productId: product.id, quantity: 1 });
      setCartMessage(`${product.name} 상품을 장바구니에 담았습니다.`);
    } catch {
      setCartMessage("장바구니에 담지 못했습니다. 상품 재고를 확인해 주세요.");
    } finally {
      setAddingProductId(null);
    }
  };

  useEffect(() => {
    let isMounted = true;

    const loadProducts = async () => {
      setIsLoading(true);
      setErrorMessage("");

      try {
        const response = await getProducts({
          page,
          size: PAGE_SIZE,
          sort,
          ...condition,
        });

        if (isMounted) {
          setProducts(response.content);
          setPageInfo(response);
        }
      } catch {
        if (isMounted) {
          setErrorMessage(
            "상품을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
          );
        }
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    void loadProducts();
    return () => {
      isMounted = false;
    };
  }, [page, sort, condition]);

  const updateUrl = (
    nextPage: number,
    nextSort: ProductSort,
    nextCondition: ProductSearchCondition,
  ) => {
    const params = new URLSearchParams();
    params.set("page", nextPage.toString());
    params.set("sort", nextSort);

    if (nextCondition.keyword) params.set("keyword", nextCondition.keyword);
    if (nextCondition.category) params.set("category", nextCondition.category);
    if (nextCondition.minPrice !== undefined) {
      params.set("minPrice", nextCondition.minPrice.toString());
    }
    if (nextCondition.maxPrice !== undefined) {
      params.set("maxPrice", nextCondition.maxPrice.toString());
    }

    window.history.replaceState(
      null,
      "",
      `${window.location.pathname}?${params.toString()}${window.location.hash}`,
    );
  };

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const parsedMinPrice = parseOptionalNumber(minPrice);
    const parsedMaxPrice = parseOptionalNumber(maxPrice);

    if (
      parsedMinPrice !== undefined &&
      parsedMaxPrice !== undefined &&
      parsedMinPrice > parsedMaxPrice
    ) {
      setErrorMessage("최소 가격은 최대 가격보다 클 수 없습니다.");
      return;
    }

    const nextCondition: ProductSearchCondition = {
      keyword: keyword.trim() || undefined,
      category: category.trim() || undefined,
      minPrice: parsedMinPrice,
      maxPrice: parsedMaxPrice,
    };

    setErrorMessage("");
    setCondition(nextCondition);
    setPage(0);
    updateUrl(0, sort, nextCondition);
  };

  const handleReset = () => {
    setKeyword("");
    setCategory("");
    setMinPrice("");
    setMaxPrice("");
    setCondition(EMPTY_CONDITION);
    setPage(0);
    updateUrl(0, sort, EMPTY_CONDITION);
  };

  const handleSortChange = (nextSort: ProductSort) => {
    setSort(nextSort);
    setPage(0);
    updateUrl(0, nextSort, condition);
  };

  const handlePageChange = (nextPage: number) => {
    setPage(nextPage);
    updateUrl(nextPage, sort, condition);
  };

  return (
    <section className="product-section" aria-labelledby="product-section-title">
      <div className="product-section-heading">
        <div>
          <p className="eyebrow">PRODUCTS</p>
          <h2 id="product-section-title">상품 목록</h2>
        </div>
        {!isLoading && !errorMessage && (
          <span className="product-count">
            총 {pageInfo?.totalElements ?? 0}개의 상품
          </span>
        )}
        <select
          value={sort}
          onChange={(event) =>
            handleSortChange(event.target.value as ProductSort)
          }
          aria-label="상품 정렬"
        >
          <option value="latest">최신순</option>
          <option value="priceAsc">낮은 가격순</option>
          <option value="priceDesc">높은 가격순</option>
        </select>
      </div>

      <form className="product-search-form" onSubmit={handleSearch}>
        <input
          type="search"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="상품명 또는 설명"
          aria-label="검색어"
        />
        <input
          type="text"
          value={category}
          onChange={(event) => setCategory(event.target.value)}
          placeholder="카테고리"
          aria-label="카테고리"
        />
        <input
          type="number"
          min="0"
          value={minPrice}
          onChange={(event) => setMinPrice(event.target.value)}
          placeholder="최소 가격"
          aria-label="최소 가격"
        />
        <input
          type="number"
          min="0"
          value={maxPrice}
          onChange={(event) => setMaxPrice(event.target.value)}
          placeholder="최대 가격"
          aria-label="최대 가격"
        />
        <div className="product-search-actions">
          <button type="submit" disabled={isLoading}>검색</button>
          <button type="button" disabled={isLoading} onClick={handleReset}>
            초기화
          </button>
        </div>
      </form>

      {isLoading && <p className="product-state">상품을 불러오는 중입니다...</p>}
      {errorMessage && (
        <p className="product-state error" role="alert">{errorMessage}</p>
      )}
      {cartMessage && (
        <p className="cart-feedback" role="status">{cartMessage}</p>
      )}
      {!isLoading && !errorMessage && products.length === 0 && (
        <p className="product-state">검색 조건에 해당하는 상품이 없습니다.</p>
      )}

      {!isLoading && products.length > 0 && (
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
                <span className="product-category">{product.category}</span>
                <h3>{product.name}</h3>
                <p className="product-description">{product.description}</p>
                <div className="product-card-footer">
                  <strong>{wonFormatter.format(product.price)}</strong>
                  <span>
                    {product.status === "SOLD_OUT"
                      ? "재고 없음"
                      : `재고 ${product.stockQuantity}개`}
                  </span>
                </div>
                <button
                  className="add-cart-button"
                  type="button"
                  disabled={
                    product.status !== "ACTIVE" ||
                    addingProductId === product.id
                  }
                  onClick={() => void handleAddToCart(product)}
                >
                  {addingProductId === product.id
                    ? "담는 중..."
                    : product.status === "SOLD_OUT"
                      ? "품절"
                      : "장바구니 담기"}
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      {pageInfo && pageInfo.totalPages > 1 && (
        <nav className="pagination" aria-label="상품 페이지 이동">
          <button
            type="button"
            disabled={pageInfo.first || isLoading}
            onClick={() => handlePageChange(page - 1)}
          >
            이전
          </button>
          {Array.from({ length: pageInfo.totalPages }, (_, index) => index).map(
            (pageNumber) => (
              <button
                type="button"
                key={pageNumber}
                aria-current={page === pageNumber ? "page" : undefined}
                disabled={isLoading}
                onClick={() => handlePageChange(pageNumber)}
              >
                {pageNumber + 1}
              </button>
            ),
          )}
          <button
            type="button"
            disabled={pageInfo.last || isLoading}
            onClick={() => handlePageChange(page + 1)}
          >
            다음
          </button>
        </nav>
      )}
    </section>
  );
}
