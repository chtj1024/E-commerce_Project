import { apiClient } from "./apiClient";
import type {
  PageResponse,
  ProductCreateRequest,
  ProductResponse,
  ProductSearchCondition,
  ProductStatusUpdateRequest,
  ProductStockUpdateRequest,
  ProductUpdateRequest,
} from "../types/product";

export type ProductSort = "latest" | "priceAsc" | "priceDesc";

export type GetProductsParams = ProductSearchCondition & {
  page?: number;
  size?: number;
  sort?: ProductSort;
};

export const createProduct = async (
  request: ProductCreateRequest,
): Promise<ProductResponse> => {
  const response = await apiClient.post<ProductResponse>(
    "/admin/products",
    request,
  );

  return response.data;
};

export const getProducts = async (
  {
    page = 0,
    size = 12,
    sort = "latest",
    keyword,
    category,
    minPrice,
    maxPrice,
  }: GetProductsParams = {},
): Promise<PageResponse<ProductResponse>> => {
  const response = await apiClient.get<PageResponse<ProductResponse>>(
    "/products",
    {
      params: {
        page,
        size,
        sort,
        keyword: keyword || undefined,
        category: category || undefined,
        minPrice,
        maxPrice,
      },
    },
  );

  return response.data;
};

export const getAdminProducts = async (): Promise<ProductResponse[]> => {
  const response = await apiClient.get<ProductResponse[]>("/admin/products");
  return response.data;
};

export const updateProduct = async (
  productId: number,
  request: ProductUpdateRequest,
): Promise<ProductResponse> => {
  const response = await apiClient.put<ProductResponse>(
    `/admin/products/${productId}`,
    request,
  );
  return response.data;
};

export const updateProductStock = async (
  productId: number,
  request: ProductStockUpdateRequest,
): Promise<ProductResponse> => {
  const response = await apiClient.patch<ProductResponse>(
    `/admin/products/${productId}/stock`,
    request,
  );
  return response.data;
};

export const updateProductStatus = async (
  productId: number,
  request: ProductStatusUpdateRequest,
): Promise<ProductResponse> => {
  const response = await apiClient.patch<ProductResponse>(
    `/admin/products/${productId}/status`,
    request,
  );
  return response.data;
};

export const deleteProduct = async (productId: number): Promise<void> => {
  await apiClient.delete(`/admin/products/${productId}`);
};
