export interface EmployeeDocument {
  id: string;
  fileName: string;
  filePath: string;
  type: string;               
  employee?: { id: string };   
}
