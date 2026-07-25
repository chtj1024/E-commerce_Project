import { apiClient } from "./apiClient";
import type { ProductCreateRequest, ProductResponse } from "../types/product";

export const createProduct = async (
  request: ProductCreateRequest,
): Promise<ProductResponse> => {
  const response = await apiClient.post<ProductResponse>(
    "/admin/products",
    request,
  );

  return response.data;
};

export const getProducts = async (): Promise<ProductResponse[]> => {
  const response = await apiClient.get<ProductResponse[]>("/products");

  return response.data;
};
