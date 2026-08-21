import { apiClient } from "./apiClient";
import type { OrderCreateRequest, OrderResponse } from "../types/order";

export const createOrder = async (
  request: OrderCreateRequest,
): Promise<OrderResponse> => {
  const response = await apiClient.post<OrderResponse>(
    "/user/orders",
    request,
  );

  return response.data;
};
