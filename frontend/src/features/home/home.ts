import { Component } from '@angular/core';
import { KidList } from '../kids/kid-list/kid-list';
import { TaskTable } from '../tasks/task-table/task-table';
import { SuggestionList } from '../suggestions/suggestion-list/suggestion-list';

@Component({
  selector: 'app-home',
  imports: [KidList, TaskTable, SuggestionList],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home { 
}
