export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PAID' | 'CANCELLED';

export interface Invoice {
  id: string;
  invoiceNumber: string;
  status: InvoiceStatus;
  issueDate: string;
  dueDate: string | null;
  // Клиент
  clientName: string | null;
  clientAddress: string | null;
  clientVatNumber: string | null;
  // Продавач
  sellerName: string | null;
  sellerVatNumber: string | null;
  sellerAddress: string | null;
  // Курс
  tripId: string;
  tripRoute: string | null;
  vehiclePlate: string | null;
  tripDepartureDate: string | null;
  tripArrivalDate: string | null;
  distanceKm: number | null;
  // Финансови данни
  revenueEur: number | null;
  vatEur: number | null;
  tollFeesEur: number | null;
  fuelCostEur: number | null;
  borderFeesEur: number | null;
  parkingAccommodationEur: number | null;
  otherExpensesEur: number | null;
  totalExpensesEur: number | null;
  // Друго
  notes: string | null;
  createdAt: string;
}

export interface InvoiceRequest {
  issueDate?: string;
  dueDate?: string;
  clientName?: string;
  clientAddress?: string;
  clientVatNumber?: string;
  sellerName?: string;
  sellerVatNumber?: string;
  sellerAddress?: string;
  notes?: string;
  status?: InvoiceStatus;
}

export interface InvoicePage {
  content: Invoice[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
