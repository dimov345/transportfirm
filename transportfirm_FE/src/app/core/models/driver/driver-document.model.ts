export interface DriverDocument {
  id: string;
  type: string;            
  fileName: string;
  filePath: string;
  employee?: { id: number } | null;
}
