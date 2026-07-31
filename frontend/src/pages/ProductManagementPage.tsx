import axios from "axios";
import { useEffect, useState, type FormEvent } from "react";
import {
  deleteProduct,
  getAdminProducts,
  updateProduct,
  updateProductStatus,
  updateProductStock,
} from "../api/productApi";
import type { ProductResponse } from "../types/product";

type Props = {
  onBack: () => void;
  onCreate: () => void;
};

type EditForm = {
  id: number;
  name: string;
  category: string;
  price: string;
  description: string;
  imageUrl: string;
};

const statusLabels: Record<ProductResponse["status"], string> = {
  ACTIVE: "판매 중",
  SOLD_OUT: "품절",
  HIDDEN: "숨김",
};

export default function ProductManagementPage({ onBack, onCreate }: Props) {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [stockDrafts, setStockDrafts] = useState<Record<number, string>>({});
  const [editForm, setEditForm] = useState<EditForm | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [message, setMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const load = async () => {
      try {
        const response = await getAdminProducts();
        if (!isMounted) return;
        setProducts(response);
        setStockDrafts(Object.fromEntries(response.map((product) => [product.id, String(product.stockQuantity)])));
      } catch (error) {
        if (isMounted) showError(error, "상품 목록을 불러오지 못했습니다.");
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    void load();
    return () => { isMounted = false; };
  }, []);

  const replaceProduct = (updated: ProductResponse) => {
    setProducts((current) => current.map((product) => product.id === updated.id ? updated : product));
    setStockDrafts((current) => ({ ...current, [updated.id]: String(updated.stockQuantity) }));
  };

  const showSuccess = (text: string) => {
    setMessage(text);
    setIsSuccess(true);
  };

  const showError = (error: unknown, fallback: string) => {
    setIsSuccess(false);
    if (axios.isAxiosError(error) && error.response?.status === 403) {
      setMessage("상품을 관리할 권한이 없습니다.");
      return;
    }
    setMessage(fallback);
  };

  const saveStock = async (product: ProductResponse) => {
    const stockQuantity = Number(stockDrafts[product.id]);
    if (!Number.isSafeInteger(stockQuantity) || stockQuantity < 0) {
      setMessage("재고는 0 이상의 정수로 입력해 주세요.");
      setIsSuccess(false);
      return;
    }

    setBusyId(product.id);
    try {
      replaceProduct(await updateProductStock(product.id, { stockQuantity }));
      showSuccess("재고를 수정했습니다.");
    } catch (error) {
      showError(error, "재고 수정에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  const toggleStatus = async (product: ProductResponse) => {
    const status = product.status === "ACTIVE" ? "SOLD_OUT" : "ACTIVE";
    if (status === "ACTIVE" && product.stockQuantity === 0) {
      setMessage("재고가 0인 상품은 판매 중으로 변경할 수 없습니다.");
      setIsSuccess(false);
      return;
    }

    setBusyId(product.id);
    try {
      replaceProduct(await updateProductStatus(product.id, { status }));
      showSuccess(`${statusLabels[status]} 상태로 변경했습니다.`);
    } catch (error) {
      showError(error, "상품 상태 변경에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  const removeProduct = async (product: ProductResponse) => {
    if (!window.confirm(`\"${product.name}\" 상품을 삭제하시겠습니까?`)) return;

    setBusyId(product.id);
    try {
      await deleteProduct(product.id);
      setProducts((current) => current.filter((item) => item.id !== product.id));
      showSuccess("상품을 삭제했습니다.");
    } catch (error) {
      showError(error, "상품 삭제에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  const startEdit = (product: ProductResponse) => {
    setEditForm({
      id: product.id,
      name: product.name,
      category: product.category,
      price: String(product.price),
      description: product.description,
      imageUrl: product.imageUrl ?? "",
    });
    setMessage("");
  };

  const submitEdit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!editForm) return;

    const price = Number(editForm.price);
    if (!Number.isSafeInteger(price) || price < 0) {
      setMessage("가격은 0 이상의 정수로 입력해 주세요.");
      setIsSuccess(false);
      return;
    }

    setBusyId(editForm.id);
    try {
      const updated = await updateProduct(editForm.id, {
        name: editForm.name.trim(),
        category: editForm.category.trim(),
        price,
        description: editForm.description.trim(),
        imageUrl: editForm.imageUrl.trim(),
      });
      replaceProduct(updated);
      setEditForm(null);
      showSuccess("상품 정보를 수정했습니다.");
    } catch (error) {
      showError(error, "상품 정보 수정에 실패했습니다.");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <main className="product-management-page">
      <button className="back-button" type="button" onClick={onBack}>홈으로</button>
      <section className="management-panel">
        <div className="management-heading">
          <div><p className="eyebrow">PRODUCT MANAGEMENT</p><h1>상품 관리</h1></div>
          <button className="primary-button" type="button" onClick={onCreate}>상품 등록</button>
        </div>

        {message && <p className={`management-message ${isSuccess ? "success" : "error"}`} role="status">{message}</p>}
        {isLoading && <p className="product-state">상품을 불러오는 중입니다...</p>}
        {!isLoading && products.length === 0 && <p className="product-state">등록된 상품이 없습니다.</p>}

        {products.length > 0 && (
          <div className="management-table-wrap">
            <table className="management-table">
              <thead><tr><th>상품</th><th>가격</th><th>재고</th><th>상태</th><th>관리</th></tr></thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td><div className="management-product"><img src={product.imageUrl || "/icons.svg"} alt="" /><span>{product.name}</span></div></td>
                    <td>{product.price.toLocaleString("ko-KR")}원</td>
                    <td><div className="stock-control"><input aria-label={`${product.name} 재고`} min={0} step={1} type="number" value={stockDrafts[product.id] ?? ""} onChange={(event) => setStockDrafts((current) => ({ ...current, [product.id]: event.target.value }))} /><button disabled={busyId === product.id} type="button" onClick={() => void saveStock(product)}>저장</button></div></td>
                    <td><span className={`status-badge ${product.status.toLowerCase()}`}>{statusLabels[product.status]}</span></td>
                    <td><div className="management-actions"><button disabled={busyId === product.id} type="button" onClick={() => startEdit(product)}>수정</button><button disabled={busyId === product.id} type="button" onClick={() => void toggleStatus(product)}>{product.status === "ACTIVE" ? "품절 처리" : "판매 시작"}</button><button className="danger-button" disabled={busyId === product.id} type="button" onClick={() => void removeProduct(product)}>삭제</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {editForm && (
        <div className="edit-modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setEditForm(null); }}>
          <section className="edit-modal" role="dialog" aria-modal="true" aria-labelledby="edit-product-title">
            <h2 id="edit-product-title">상품 정보 수정</h2>
            <form className="product-form" onSubmit={submitEdit}>
              <label className="field"><span>상품명</span><input required maxLength={100} value={editForm.name} onChange={(event) => setEditForm({ ...editForm, name: event.target.value })} /></label>
              <label className="field"><span>카테고리</span><input required maxLength={50} value={editForm.category} onChange={(event) => setEditForm({ ...editForm, category: event.target.value })} /></label>
              <label className="field"><span>가격</span><input required min={0} step={1} type="number" value={editForm.price} onChange={(event) => setEditForm({ ...editForm, price: event.target.value })} /></label>
              <label className="field"><span>이미지 URL</span><input maxLength={500} type="url" value={editForm.imageUrl} onChange={(event) => setEditForm({ ...editForm, imageUrl: event.target.value })} /></label>
              <label className="field"><span>상품 설명</span><textarea required maxLength={2000} rows={6} value={editForm.description} onChange={(event) => setEditForm({ ...editForm, description: event.target.value })} /></label>
              <div className="modal-actions"><button className="secondary-button" type="button" onClick={() => setEditForm(null)}>취소</button><button className="primary-button" disabled={busyId === editForm.id} type="submit">저장</button></div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
