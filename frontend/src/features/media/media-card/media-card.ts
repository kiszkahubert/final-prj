import { Component, Input, output } from '@angular/core';
import { Media } from '../../../types/media';
import { DatePipe } from '@angular/common';
import { DeleteButton } from '../../../shared/delete-button/delete-button';

@Component({
  selector: 'app-media-card',
  imports: [DatePipe, DeleteButton],
  templateUrl: './media-card.html',
  styleUrl: './media-card.css'
})
export class MediaCard {
  @Input({ required: true }) media!: Media;
  delete = output<number>();

  onDeleteClick(e: Event) {
    e.stopPropagation();
    this.delete.emit(this.media.mediaId);
  }
}
