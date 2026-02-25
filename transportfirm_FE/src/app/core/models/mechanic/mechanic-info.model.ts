import { TruckGroup } from '../../services/truck-group.service';

export interface MechanicInfo {
  id: string;
  mechanicGroups?: TruckGroup[];
}
