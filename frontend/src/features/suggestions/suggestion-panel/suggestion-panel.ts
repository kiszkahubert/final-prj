import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { SuggestionService } from '../../../core/services/suggestion-service';
import { KidService } from '../../../core/services/kid-service';
import { Suggestion } from '../../../types/suggestion';
import { Kid } from '../../../types/kid';
import { SuggestionCard } from '../suggestion-card/suggestion-card';
import { ToastService } from '../../../core/services/toast-service';
import { TaskService } from '../../../core/services/task-service';
import { CreateTask } from '../../../types/task';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-suggestion-panel',
  imports: [SuggestionCard],
  templateUrl: './suggestion-panel.html',
  styleUrl: './suggestion-panel.css'
})
export class SuggestionPanel implements OnInit {
  private suggestionService = inject(SuggestionService);
  private kidService = inject(KidService);
  private taskService = inject(TaskService);
  private toast = inject(ToastService);
  protected loading = signal(true);
  protected all = signal<(Suggestion & { kidName?: string })[]>([]);
  protected activeTab = signal<'PENDING' | 'ACCEPTED' | 'REJECTED' | 'ALL'>('PENDING');

  tabs = [
    { key: 'PENDING' as const, label: 'Pending' },
    { key: 'ACCEPTED' as const, label: 'Accepted' },
    { key: 'REJECTED' as const, label: 'Rejected' },
    { key: 'ALL' as const, label: 'All suggestions' }
  ];

  filtered = computed(() => {
    const tab = this.activeTab();
    if (tab === 'ALL') return this.all();
    return this.all().filter(s => s.status === tab);
  });

  counts = computed(() => ({
    PENDING: this.all().filter(s => s.status === 'PENDING').length,
    ACCEPTED: this.all().filter(s => s.status === 'ACCEPTED').length,
    REJECTED: this.all().filter(s => s.status === 'REJECTED').length,
    ALL: this.all().length
  }));

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
        this.suggestionService.getAllSuggestions().pipe(
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
            this.all.set(mapped);
            this.loading.set(false);
          },
          error: err => {
            this.all.set([]);
            this.loading.set(false);
            this.toast.error('Failed to load suggestions: ' + err.message);
          }
        });
      },
      error: err => {
        this.all.set([]);
        this.loading.set(false);
        this.toast.error('Failed to load suggestions: ' + err.message);
      }
    });
  }

  setTab(tab: any) {
    this.activeTab.set(tab);
  }

  onAction(e: { id: number; type: 'ACCEPT' | 'REJECT' }) {
    if (e.type === 'ACCEPT') {
      this.suggestionService.reviewSuggestion(e.id, true).subscribe({
        next: (updated) => {
          this.all.update(list => list.map(s => s.id === e.id ? { ...s, status: updated.status } : s));
          const suggestion = this.all().find(s => s.id === e.id);
          if (!suggestion) return;
          const toLocalString = (val: string) => {
            if (!val) return val;
            return val.length === 16 ? val + ':00' : val.slice(0, 19);
          };
          const newTask: CreateTask = {
            title: suggestion.title,
            description: suggestion.description,
            taskStart: toLocalString(suggestion.proposedStart),
            taskEnd: toLocalString(suggestion.proposedEnd),
            status: 'PENDING',
            kidIds: [suggestion.createdById]
          };
          this.taskService.addTask(newTask).subscribe({
            next: () => {
              this.toast.success('Task added from suggestion');
            },
            error: () => {
              this.toast.error('Failed to add task');
            }
          });
        },
        error: () => {
          this.toast.error('Failed to review suggestion');
        }
      });
    } else {
      this.suggestionService.reviewSuggestion(e.id, false).subscribe({
        next: (updated) => {
          this.all.update(list => list.map(s => s.id === e.id ? { ...s, status: updated.status } : s));
          this.toast.success('Suggestion rejected successfully');
        }
      });
    }
  }
}