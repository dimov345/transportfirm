import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriversList } from './drivers-list';

describe('DriversList', () => {
  let component: DriversList;
  let fixture: ComponentFixture<DriversList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriversList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriversList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
