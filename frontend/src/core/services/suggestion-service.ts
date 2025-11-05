
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.development';
import { Suggestion } from '../../types/suggestion';

@Injectable({
  providedIn: 'root'
})
export class SuggestionService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getAllSuggestions() {
    return this.http.get<Suggestion[]>(this.baseUrl + 'api/suggestions/parent');
  }

  getPendingSuggestions() {
    return this.http.get<Suggestion[]>(this.baseUrl + 'api/suggestions/parent/pending');
  }

  reviewSuggestion(suggestionId: number, accepted: boolean) {
    return this.http.post<Suggestion>(this.baseUrl + 'api/suggestions/' + suggestionId + '/review?accepted=' + accepted, {});
  }
}
