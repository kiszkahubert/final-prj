import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Kid, KidRequest, ChildAccessToken } from '../../types/kid';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class KidService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getKids() {
    return this.http.get<Kid[]>(this.baseUrl + 'api/kids');
  }

  getKid(kidId: number) {
    return this.http.get<Kid>(this.baseUrl + 'api/kids/' + kidId);
  }

  addKid(kid: KidRequest) {
    return this.http.post<Kid>(this.baseUrl + 'api/kids/new', kid);
  }

  updateKid(kidId: number, kid: KidRequest) {
    return this.http.put<Kid>(this.baseUrl + 'api/kids/' + kidId, kid);
  }

  deleteKid(kidId: number) {
    return this.http.delete(this.baseUrl + 'api/kids/' + kidId, { responseType: 'text' as 'json' });
  }

  getChildAccessTokens() {
    return this.http.get<ChildAccessToken[]>(this.baseUrl + 'api/tokens');
  }
}
