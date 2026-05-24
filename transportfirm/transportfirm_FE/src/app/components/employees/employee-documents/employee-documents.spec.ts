import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeDocuments } from './employee-documents';

describe('EmployeeDocuments', () => {
  let component: EmployeeDocuments;
  let fixture: ComponentFixture<EmployeeDocuments>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeDocuments]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeDocuments);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
