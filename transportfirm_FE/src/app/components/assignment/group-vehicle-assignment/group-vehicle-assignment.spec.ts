import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupVehicleAssignment } from './group-vehicle-assignment';

describe('GroupVehicleAssignment', () => {
  let component: GroupVehicleAssignment;
  let fixture: ComponentFixture<GroupVehicleAssignment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupVehicleAssignment]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupVehicleAssignment);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
