export type ProductCreateRequest = {
  name: string;
  price: number;
  stockQuantity: number;
  description: string;
  imageUrl: string;
};

export type ProductResponse = {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
  description: string;
  imageUrl: string | null;
  status: "ACTIVE" | "SOLD_OUT" | "HIDDEN";
  createdAt: string;
  updatedAt: string;
};
