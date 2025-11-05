import { Component, ViewChild, ElementRef, signal, computed, inject } from '@angular/core';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { Media } from '../../../types/media';
import { MediaCard } from '../media-card/media-card';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../../core/services/toast-service';
import { MediaService } from '../../../core/services/media-service';
import { AccountService } from '../../../core/services/account-service';
import { KidService } from '../../../core/services/kid-service';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog-service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { Paginator } from '../../../shared/paginator/paginator';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-gallery',
  imports: [FormsModule, ImageUpload, MediaCard, ConfirmDialog, Paginator],
  templateUrl: './gallery.html',
  styleUrl: './gallery.css'
})
export class Gallery {
  @ViewChild('uploadModal') uploadModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('previewModal') previewModal!: ElementRef<HTMLDialogElement>;
  private toast = inject(ToastService);
  private mediaService = inject(MediaService);
  private accountService = inject(AccountService);
  private kidService = inject(KidService);
  private confirmDialog = inject(ConfirmDialogService);
  protected images = signal<Media[]>([]);
  protected loading = signal(false);
  protected selectedImage: Media | null = null;
  protected users = signal<string[]>([]);
  protected filterUser = signal<string>('');
  protected filterDate = signal<string>('');
  protected maxDate = new Date().toISOString().slice(0, 10);
  protected pageNumber = signal(1);
  protected pageSize = signal(8);
  protected totalCount = computed(() => this.filteredImages().length);
  protected totalPages = computed(() => Math.ceil(this.totalCount() / this.pageSize()));
  protected filteredImages = computed(() => {
    const user = this.filterUser();
    const date = this.filterDate();
    return this.images().filter(m => {
      if (user && m.uploadByUsername !== user) return false;
      if (date) {
        const mDate = new Date(m.uploadedAt).toISOString().slice(0, 10);
        if (mDate !== date) return false;
      }
      return true;
    });
  });
  protected paginatedImages = computed(() => {
    const filtered = this.filteredImages();
    const start = (this.pageNumber() - 1) * this.pageSize();
    const end = start + this.pageSize();
    return filtered.slice(start, end);
  });

  constructor() {
    this.loadImages();
    this.loadUserOptions();
  }

  private loadImages() {
    this.loading.set(true);
    this.mediaService.getImages().pipe(
      timeout(15000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: (medias) => {
        this.images.set(medias);
        this.loading.set(false);
      },
      error: (err) => {
        this.images.set([]);
        this.loading.set(false);
        this.toast.error('Failed to load images' + err.message);
      }
    });
  }

  private loadUserOptions() {
    const currentUser = this.accountService.getUser();
    const userOptions: string[] = [];

    if (currentUser?.username) {
      userOptions.push(currentUser.username);
    }

    this.kidService.getKids().subscribe({
      next: (kids) => {
        kids.forEach(kid => userOptions.push(kid.name));
        this.users.set(userOptions);
      },
      error: () => {
        this.users.set(userOptions);
      }
    });
  }

  onModalUpload(file: File) {
    this.mediaService.uploadImage([file]).pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        this.toast.success('Media added successfully');
        this.loadImages();
        this.uploadModal.nativeElement.close();
      },
      error: () => {
        this.toast.error('Failed to upload media');
      }
    });
  }

  async onDelete(mediaId: number) {
    const media = this.images().find(m => m.mediaId === mediaId);
    const currentUser = this.accountService.getUser();
    if (!currentUser?.username) {
      this.toast.error('Unauthorized');
      return;
    }
    if (media && media.uploadByUsername && media.uploadByUsername !== currentUser.username) {
      this.toast.error('You cannot delete another user\'s image');
      return;
    }

    const confirmed = await this.confirmDialog.confirm('Are you sure you want to delete this image?');
    if (!confirmed) return;

    const prev = this.images();
    this.images.set(prev.filter(m => m.mediaId !== mediaId));
    this.mediaService.deleteImage(mediaId).pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        this.toast.success('Image deleted successfully');
        this.loadImages();
      },
      error: () => {
        this.toast.error('Failed to delete image');
        this.images.set(prev);
      }
    });
  }

  openAddModal() {
    this.uploadModal.nativeElement.showModal();
  }

  onDateModelChange(value: string) {
    if (!value) {
      this.filterDate.set('');
      return;
    }
    if (value > this.maxDate) {
      this.filterDate.set(this.maxDate);
      this.toast.info('Date cannot be in the future — set to today');
    } else {
      this.filterDate.set(value);
    }
  }

  openPreview(media: Media) {
    this.selectedImage = media;
    this.previewModal.nativeElement.showModal();
  }

  closePreview() {
    this.selectedImage = null;
    this.previewModal.nativeElement.close();
  }

  onPageChange(event: { pageNumber: number, pageSize: number }) {
    this.pageNumber.set(event.pageNumber);
    this.pageSize.set(event.pageSize);
  }
}

