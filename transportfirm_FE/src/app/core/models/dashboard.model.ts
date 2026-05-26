export interface MonthlyStatDto {
  monthLabel:      string;
  revenueEur:      number;
  tripExpensesEur: number;
  netProfitEur:    number;
  tripCount:       number;
}

export interface DashboardStats {
  activeEmployeesCount:          number;
  activeVehiclesCount:           number;
  tripsThisMonthCount:           number;
  revenueThisMonthEur:           number;
  maintenanceCostsThisMonthBgn:  number;

  // Current month expense breakdown
  salaryBrutoEur:    number;  // gross salaries → EUR
  salaryNetoEur:     number;  // net salaries → EUR
  salaryTaxesEur:    number;  // taxes + social insurance → EUR
  tripExpensesEur:   number;  // all trip costs → EUR
  vatThisMonthEur:   number;  // VAT component of trip expenses
  maintenanceEur:    number;  // maintenance costs → EUR

  expiredDocsCount:  number;
  expiringDocsCount: number;
  monthlyStats:      MonthlyStatDto[];
}

export interface RoleStats {
  // Dispatcher
  dispatcherGroupsCount:         number;
  dispatcherVehiclesCount:       number;
  plannedTripsCount:             number;
  inTransitTripsCount:           number;
  completedTripsThisMonthCount:  number;
  // Mechanic
  mechanicGroupsCount:           number;
  mechanicVehiclesCount:         number;
  openMaintenanceCount:          number;
  closedMaintenanceThisMonthCount: number;
  // Driver
  expiredDocsCount:    number;
  expiringDocsCount:   number;
  assignedVehiclePlate: string | null;
  daysUntilNextExpiry: number | null;
}

export interface DashboardNotification {
  type:            string;   // 'vehicle' | 'driver'
  documentType:    string;
  entityName:      string;
  entityId:        string;
  expiryDate:      string;   // ISO date string
  status:          string;   // 'expired' | 'expiring'
  daysUntilExpiry: number;
  navigateTo:      string;
}
