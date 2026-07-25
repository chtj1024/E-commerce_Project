import axios from "axios";
import { useState, type FormEvent } from "react";
import { createProduct } from "../api/productApi";

type ProductCreatePageProps = {
  onBack: () => void;
};

export default function ProductCreatePage({ onBack }: ProductCreatePageProps) {
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [stockQuantity, setStockQuantity] = useState("");
  const [description, setDescription] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [message, setMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessage("");
    setIsSuccess(false);

    const parsedPrice = Number(price);
    const parsedStockQuantity = Number(stockQuantity);

    if (!Number.isSafeInteger(parsedPrice) || parsedPrice < 0) {
      setMessage("가격은 0 이상의 정수로 입력해 주세요.");
      return;
    }

    if (!Number.isSafeInteger(parsedStockQuantity) || parsedStockQuantity < 0) {
      setMessage("재고는 0 이상의 정수로 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);

    try {
      const product = await createProduct({
        name: name.trim(),
        price: parsedPrice,
        stockQuantity: parsedStockQuantity,
        description: description.trim(),
        imageUrl: imageUrl.trim(),
      });

      setMessage(`상품이 등록되었습니다. 상품 번호: ${product.id}`);
      setIsSuccess(true);
      setName("");
      setPrice("");
      setStockQuantity("");
      setDescription("");
      setImageUrl("");
    } catch (error) {
      setMessage(getProductErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="product-create-page">
      <button className="back-button" type="button" onClick={onBack}>
        홈으로
      </button>

      <section className="product-create-card">
        <p className="eyebrow">PRODUCT MANAGEMENT</p>
        <h1>상품 등록</h1>
        <p className="product-form-description">
          판매할 상품의 기본 정보와 재고를 입력해 주세요.
        </p>

        <form className="product-form" onSubmit={submit}>
          <label className="field">
            <span>상품명</span>
            <input
              required
              maxLength={100}
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="예: 오버핏 코튼 셔츠"
            />
          </label>

          <div className="product-form-row">
            <label className="field">
              <span>가격</span>
              <input
                required
                min={0}
                step={1}
                type="number"
                value={price}
                onChange={(event) => setPrice(event.target.value)}
                placeholder="39000"
              />
            </label>

            <label className="field">
              <span>재고 수량</span>
              <input
                required
                min={0}
                step={1}
                type="number"
                value={stockQuantity}
                onChange={(event) => setStockQuantity(event.target.value)}
                placeholder="30"
              />
            </label>
          </div>

          <label className="field">
            <span>대표 이미지 URL</span>
            <input
              maxLength={500}
              type="url"
              value={imageUrl}
              onChange={(event) => setImageUrl(event.target.value)}
              placeholder="https://example.com/product.jpg"
            />
          </label>

          <label className="field">
            <span>상품 설명</span>
            <textarea
              required
              maxLength={2000}
              rows={7}
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="상품의 소재, 특징, 사용 방법 등을 입력해 주세요."
            />
          </label>

          {message && (
            <p className={`message ${isSuccess ? "success" : "error"}`} role="status">
              {message}
            </p>
          )}

          <button className="primary-button form-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? "등록 중..." : "상품 등록"}
          </button>
        </form>
      </section>
    </main>
  );
}

function getProductErrorMessage(error: unknown) {
  if (!axios.isAxiosError(error) || !error.response) {
    return "서버에 연결할 수 없습니다. 서버 실행 상태를 확인해 주세요.";
  }

  if (error.response.status === 400) return "입력값을 다시 확인해 주세요.";
  if (error.response.status === 401) return "로그인이 필요합니다.";
  if (error.response.status === 403) return "상품을 등록할 관리자 권한이 없습니다.";

  return "상품 등록 중 오류가 발생했습니다.";
}
