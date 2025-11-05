import { Component, OnInit, inject, signal } from '@angular/core';
import { SuggestionService } from '../../../core/services/suggestion-service';
import { KidService } from '../../../core/services/kid-service';
import { Suggestion } from '../../../types/suggestion';
import { Kid } from '../../../types/kid';
import { RouterLink } from '@angular/router';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { ToastService } from '../../../core/services/toast-service';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-suggestion-list',
  imports: [RouterLink, DatePipe, UpperCasePipe],
  templateUrl: './suggestion-list.html',
  styleUrl: './suggestion-list.css'
})
export class SuggestionList implements OnInit {
  private suggestionService = inject(SuggestionService);
  private kidService = inject(KidService);
  private toast = inject(ToastService);
  protected loading = signal(true);
  protected suggestions = signal<(Suggestion & { kidName?: string })[]>([]);

  ngOnInit(): void {
    this.loading.set(true);
    this.kidService.getKids().pipe(
      timeout(10000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: kids => {
        const kidMap = new Map<number, string>(
          (kids as Kid[]).filter(k => k.id != null).map(k => [k.id!, k.name])
        );
        this.suggestionService.getPendingSuggestions().pipe(
          timeout(10000),
          catchError(err => {
            if (err.name === 'TimeoutError') {
              return throwError(() => new Error('Request timed out'));
            }
            return throwError(() => err);
          })
        ).subscribe({
          next: suggestions => {
            const mapped = (suggestions as Suggestion[]).map(s => ({
              ...s,
              kidName: kidMap.get(s.createdById)
            }));
            this.suggestions.set(this.sort(mapped));
            this.loading.set(false);
          },
          error: err => {
            this.suggestions.set([]);
            this.loading.set(false);
            this.toast.error('Failed to load suggestions: ' + err.message);
          }
        });
      },
      error: err => {
        this.suggestions.set([]);
        this.loading.set(false);
        this.toast.error('Failed to load suggestions: ' + err.message);
      }
    });
  }

  private sort(list: (Suggestion & { kidName?: string })[]) {
    return [...list].sort((a, b) => new Date(a.proposedStart).getTime() - new Date(b.proposedStart).getTime());
  }
}
