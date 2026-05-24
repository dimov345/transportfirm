export type TripStatus = 'PLANNED' | 'IN_TRANSIT' | 'COMPLETED';

export interface FreightTrip {
  id?: string;
  vehicle?: {
    id: string;
    plateNumber: string;
    model?: string;
    dispatcherGroup?: { id: string; groupName?: string } | null;
  };
  status: TripStatus;
  departureDate: string;
  arrivalDate?: string;
  originCity: string;
  destinationCity: string;
  distanceKm?: number;
  clientName?: string;
  clientEik?: string;
  clientAddress?: string;
  sellerName?: string;
  sellerEik?: string;
  sellerAddress?: string;
  revenueEur?: number;
  vatEur?: number;
  tollFeesEur?: number;
  fuelLiters?: number;
  fuelCostEur?: number;
  borderFeesEur?: number;
  parkingAccommodationEur?: number;
  otherExpensesEur?: number;
  cargoDescription?: string;
  cargoWeightTons?: number;
  notes?: string;
  createdAt?: string;
}
