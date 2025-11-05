import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Task } from '../../../../types/task';

@Component({
  selector: 'app-day-in-calendar',
  imports: [DatePipe],
  templateUrl: './day-in-calendar.html',
  styleUrl: './day-in-calendar.css'
})
export class DayInCalendar {
  @Input({ required: true }) date!: Date;
  @Input({ required: true }) tasks: Task[] = [];
  @Output() editRequested = new EventEmitter<Task>();
  @Output() deleteRequested = new EventEmitter<Task>();
  @Input() loading: boolean = false;
}
