export type OrderStatus =
  | "PAYMENT_PENDING"
  | "PAID"
  | "PAYMENT_FAILED"
  | "EXPIRED"
  | "CANCELED";

export type OrderItemRequest = {
  productId: number;
  quantity: number;
};

export type OrderCreateRequest = {
  items: OrderItemRequest[];
};

export type OrderResponse = {
  orderId: number;
  status: OrderStatus;
  totalPrice: number;
  expiresAt: string;
};
