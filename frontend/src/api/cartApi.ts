import { apiClient } from "./apiClient";
import type {
  CartItemAddRequest,
  CartItemResponse,
  CartQuantityUpdateRequest,
} from "../types/cart";

export const getCartItems = async (): Promise<CartItemResponse[]> => {
  const response = await apiClient.get<CartItemResponse[]>("/user/cart");
  return response.data;
};

export const addCartItem = async (
  request: CartItemAddRequest,
): Promise<CartItemResponse> => {
  const response = await apiClient.post<CartItemResponse>(
    "/user/cart",
    request,
  );
  return response.data;
};

export const updateCartItemQuantity = async (
  cartItemId: number,
  request: CartQuantityUpdateRequest,
): Promise<CartItemResponse> => {
  const response = await apiClient.patch<CartItemResponse>(
    `/user/cart/${cartItemId}`,
    request,
  );
  return response.data;
};

export const deleteCartItem = async (cartItemId: number): Promise<void> => {
  await apiClient.delete(`/user/cart/${cartItemId}`);
};
