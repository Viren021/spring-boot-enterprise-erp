import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanceManagement } from './finance-management';

describe('FinanceManagement', () => {
  let component: FinanceManagement;
  let fixture: ComponentFixture<FinanceManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinanceManagement],
    }).compileComponents();

    fixture = TestBed.createComponent(FinanceManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
