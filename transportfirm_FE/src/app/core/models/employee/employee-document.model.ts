export interface EmployeeDocument {
  id: string;
  fileName: string;
  type: string;
  employee?: { id: string };
}
