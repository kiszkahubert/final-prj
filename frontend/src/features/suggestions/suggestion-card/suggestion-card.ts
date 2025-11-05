import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Suggestion } from '../../../types/suggestion';
import { DatePipe, NgClass, UpperCasePipe } from '@angular/common';

@Component({
  selector: 'app-suggestion-card',
  imports: [DatePipe, NgClass, UpperCasePipe],
  templateUrl: './suggestion-card.html',
  styleUrl: './suggestion-card.css'
})
export class SuggestionCard {
  @Input({ required: true }) suggestion!: Suggestion & { kidName?: string };
  @Output() action = new EventEmitter<{ id: number; type: 'ACCEPT' | 'REJECT' }>();

  accept() {
    this.action.emit({ id: this.suggestion.id, type: 'ACCEPT' }); 
  }

  reject() {
    this.action.emit({ id: this.suggestion.id, type: 'REJECT' }); 
  }
}
