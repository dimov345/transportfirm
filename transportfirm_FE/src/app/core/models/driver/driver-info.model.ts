export interface DriverInfo {
  id: string;

  employee: EmployeeInDriverInfo;
  vehicle: any | null;

  driverLicenseIssuedOn: string | null;
  driverLicenseExpiresOn: string | null;

  qualificationCardIssuedOn: string | null;
  qualificationCardExpiresOn: string | null;

  psychologicalExamIssuedOn: string | null;
  psychologicalExamExpiresOn: string | null;

  digitalCardIssuedOn: string | null;
  digitalCardExpiresOn: string | null;
}

export interface EmployeeInDriverInfo {
  id: string;
  egn: string;
  name: string;
  dateOfBirth: string | null;
  phone: string;
  email: string;

  addressPermanent?: string | null;
  addressCurrent?: string | null;

  jobTitle?: string | null;
  hiredDate?: string | null;
  employmentStatus?: string | null;
  firedDate?: string | null;

  contractType?: string | null;
  salary?: number | null;
  salaryNeto?: number | null;
  salaryCurrency?: string | null;
  workingHours?: string | null;

  bankName?: string | null;
  iban?: string | null;
  role?: string | null;
}
