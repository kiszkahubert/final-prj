import { Component, output, signal } from '@angular/core';

@Component({
  selector: 'app-image-upload',
  imports: [],
  templateUrl: './image-upload.html',
  styleUrl: './image-upload.css'
})
export class ImageUpload {
  protected imageSrc = signal<string | ArrayBuffer | null | undefined>(null);
  protected isDragging = false;
  private fileToUpload: File | null = null;
  private fileInput?: HTMLInputElement | null;
  uploadFile = output<File>();

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer?.files.length) {
      const file = event.dataTransfer.files[0];
      this.previewImage(file);
      this.fileToUpload = file;
      if (this.fileInput) this.fileInput.value = '';
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.fileInput = input;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.previewImage(file);
    this.fileToUpload = file;
  }

  onCancel() {
    this.fileToUpload = null;
    this.imageSrc.set(null);
    if (this.fileInput) {
      this.fileInput.value = '';
    }
  }

  onUploadFile() {
    if (this.fileToUpload) {
      this.uploadFile.emit(this.fileToUpload);
      this.onCancel();
    }
  }

  private previewImage(file: File) {
    const reader = new FileReader();
    reader.onload = (e) => this.imageSrc.set(e.target?.result);
    reader.readAsDataURL(file);
  }
}
