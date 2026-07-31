export type ProductCreateRequest = {
  name: string;
  category: string;
  price: number;
  stockQuantity: number;
  description: string;
  imageUrl: string;
};

export type ProductUpdateRequest = {
  name: string;
  category: string;
  price: number;
  description: string;
  imageUrl: string;
};

export type ProductSearchCondition = {
  keyword?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
};

export type ProductStockUpdateRequest = {
  stockQuantity: number;
};

export type ProductStatusUpdateRequest = {
  status: ProductResponse["status"];
};

export type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type ProductResponse = {
  id: number;
  name: string;
  category: string;
  price: number;
  stockQuantity: number;
  description: string;
  imageUrl: string | null;
  status: "ACTIVE" | "SOLD_OUT" | "HIDDEN";
  createdAt: string;
  updatedAt: string;
};
