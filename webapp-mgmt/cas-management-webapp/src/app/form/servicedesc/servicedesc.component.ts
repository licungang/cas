import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-servicedesc',
  templateUrl: './servicedesc.component.html'
})
export class ServicedescComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
