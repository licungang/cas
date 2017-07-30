import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-servicename',
  templateUrl: './servicename.component.html'
})
export class ServicenameComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
