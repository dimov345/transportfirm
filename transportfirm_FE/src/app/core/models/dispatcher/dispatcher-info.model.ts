import { TruckGroup } from '../../services/truck-group.service';

export interface DispatcherInfo {
  id: string;
  dispatcherGroups?: TruckGroup[];
}
