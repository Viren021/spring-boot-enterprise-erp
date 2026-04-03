import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceAudit } from './compliance-audit';

describe('ComplianceAudit', () => {
  let component: ComplianceAudit;
  let fixture: ComponentFixture<ComplianceAudit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComplianceAudit],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceAudit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
