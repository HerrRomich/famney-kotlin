export type StoreOperation<T extends string> = {
  type: T;
  correlationId: string;
  error?: string;
};
