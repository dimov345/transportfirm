export interface SalaryRow {
  employeeId: string;
  name: string;
  jobTitle: string;
  brutoEur: number;
  netoEur: number;
  taxesEur: number;
}

export interface TripRow {
  tripId: string;
  departureDate: string | null;
  route: string;
  vehiclePlate: string | null;
  vehicleId: string | null;
  clientName: string | null;
  status: string | null;
  revenueEur: number;
  totalExpensesEur: number;
  netProfitEur: number;
}

export interface MaintenanceRow {
  recordId: string;
  vehicleId: string | null;
  vehiclePlate: string | null;
  maintenanceType: string;
  workshopName: string | null;
  openedAt: string | null;
  totalGrossEur: number;
}

export interface LeasingRow {
  vehicleId: string | null;
  vehiclePlate: string | null;
  leasingCompany: string | null;
  contractNumber: string | null;
  startDate: string | null;
  endDate: string | null;
  monthlyPaymentEur: number;
  totalForPeriodEur: number;
}

export interface AccountingSummary {
  tripRevenueEur: number;
  totalSalaryBrutoEur: number;
  tripExpensesEur: number;
  maintenanceCostsEur: number;
  netBalanceEur: number;
  employeeCount: number;
  tripCount: number;
  maintenanceCount: number;
  leasingTotalEur: number;
  leasedVehicleCount: number;
  vatTotalEur: number;
  salaryTaxesEur: number;
  totalSalaryNetoEur: number;
}

export interface AccountingDashboard {
  periodLabel: string;
  summary: AccountingSummary;
  salaries: SalaryRow[];
  trips: TripRow[];
  maintenance: MaintenanceRow[];
  leasings: LeasingRow[];
}
