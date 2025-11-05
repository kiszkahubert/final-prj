import { Component, input, output } from '@angular/core';

@Component({
	selector: 'app-edit-button',
	imports: [],
	templateUrl: './edit-button.html',
    styleUrl: './edit-button.css'
})
export class EditButton {
	disabled = input<boolean>();
	clickEvent = output<Event>();

	onClick(event: Event) {
		this.clickEvent.emit(event);
	}
}
