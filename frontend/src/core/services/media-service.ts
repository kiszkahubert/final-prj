import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.development';
import { Media } from '../../types/media';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getImages() {
    return this.http.get<Media[]>(this.baseUrl + 'api/media/parent/all');
  }

  uploadImage(files: File[]) {
    const fd = new FormData();
    files.forEach(f => fd.append('files', f));
    return this.http.post(this.baseUrl + 'api/media/upload', fd);
  }

  deleteImage(mediaId: number) {
    return this.http.delete(this.baseUrl + 'api/media/' + mediaId, { responseType: 'text' as 'json' });
  }
}
