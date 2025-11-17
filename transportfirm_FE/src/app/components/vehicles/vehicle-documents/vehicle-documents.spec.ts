import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleDocuments } from './vehicle-documents';

describe('VehicleDocuments', () => {
  let component: VehicleDocuments;
  let fixture: ComponentFixture<VehicleDocuments>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleDocuments]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VehicleDocuments);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
