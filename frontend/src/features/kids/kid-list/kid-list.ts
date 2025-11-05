import { Component, OnInit, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Kid } from '../../../types/kid';
import { KidService } from '../../../core/services/kid-service';
import { ToastService } from '../../../core/services/toast-service';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-kid-list',
  imports: [UpperCasePipe, RouterLink],
  templateUrl: './kid-list.html',
  styleUrl: './kid-list.css'
})
export class KidList implements OnInit {
  private kidService = inject(KidService);
  private toast = inject(ToastService);
  protected readonly MAX_KIDS = 4;
  protected kids = signal<Kid[]>([]);
  protected isLoading = signal(true);

  ngOnInit(): void {
    this.loadKids();
  }

  private loadKids() {
    this.isLoading.set(true);
    
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
        const sorted = (kids || []).slice().sort((a, b) => (a.name || '').localeCompare(b.name || '', undefined, { sensitivity: 'base' }));
        const limited = sorted.length > this.MAX_KIDS ? sorted.slice(0, this.MAX_KIDS) : sorted;
        this.kids.set(limited);
        this.isLoading.set(false);
      },
      error: err => {
        this.toast.error('Failed to load kids: ' + err.message);
        this.isLoading.set(false);
      }
    });
  }
}
