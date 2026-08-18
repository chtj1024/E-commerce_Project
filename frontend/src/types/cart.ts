export type CartItemResponse = {
  cartItemId: number;
  productId: number;
  productName: string;
  price: number;
  imageUrl: string | null;
  stockQuantity: number;
  quantity: number;
  totalPrice: number;
};

export type CartItemAddRequest = {
  productId: number;
  quantity: number;
};

export type CartQuantityUpdateRequest = {
  quantity: number;
};
