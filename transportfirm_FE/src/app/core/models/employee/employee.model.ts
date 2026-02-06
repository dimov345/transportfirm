import { DriverInfo } from "../driver/driver-info.model";

export interface Employee {
  id: string;
  egn: string;
  name: string;
  dateOfBirth: string | null;
  phone: string;
  email: string;

  addressPermanent: string | null;
  addressCurrent: string | null;

  jobTitle: string;
  hiredDate: string | null;
  employmentStatus: string | null;
  firedDate: string | null;

  contractType: string | null;
  salary: number | null;
  salaryNeto: number | null;
  salaryCurrency: string | null;
  workingHours: string | null;

  bankName: string | null;
  iban: string | null;
  role: string | null;

  documents: any[];

  driverInfo?: DriverInfo | null;
  mechanicInfo?: any | null;
  dispatcherInfo?: any | null;
}
