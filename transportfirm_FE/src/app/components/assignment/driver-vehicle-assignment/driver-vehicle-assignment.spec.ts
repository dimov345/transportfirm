import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverVehicleAssignment } from './driver-vehicle-assignment';

describe('DriverVehicleAssignment', () => {
  let component: DriverVehicleAssignment;
  let fixture: ComponentFixture<DriverVehicleAssignment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverVehicleAssignment]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverVehicleAssignment);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
