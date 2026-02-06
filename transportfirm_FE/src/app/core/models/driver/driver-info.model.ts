export interface DriverInfo {
  id: string;
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
