import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverDocuments } from './driver-documents';

describe('DriverDocuments', () => {
  let component: DriverDocuments;
  let fixture: ComponentFixture<DriverDocuments>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverDocuments]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverDocuments);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
