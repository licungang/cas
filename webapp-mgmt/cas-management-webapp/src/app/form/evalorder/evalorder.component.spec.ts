/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { EvalorderComponent } from './evalorder.component';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";
import {SharedModule} from "../../shared/shared.module";

describe('EvalorderComponent', () => {
  let component: EvalorderComponent;
  let fixture: ComponentFixture<EvalorderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ EvalorderComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EvalorderComponent);
    component = fixture.componentInstance;
    component.serviceData = new ServiceData();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
