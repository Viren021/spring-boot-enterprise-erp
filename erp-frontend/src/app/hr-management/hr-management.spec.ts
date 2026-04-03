import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HrManagement } from './hr-management';

describe('HrManagement', () => {
  let component: HrManagement;
  let fixture: ComponentFixture<HrManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrManagement],
    }).compileComponents();

    fixture = TestBed.createComponent(HrManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
