import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TaskService } from '../../../core/services/task-service';
import { Task } from '../../../types/task';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { ToastService } from '../../../core/services/toast-service';
 
@Component({
  selector: 'app-task-table',
  imports: [DatePipe],
  templateUrl: './task-table.html',
  styleUrl: './task-table.css'
})
export class TaskTable implements OnInit {
  protected taskService = inject(TaskService);
  private toast = inject(ToastService);
  protected todayTasks = signal<Task[]>([]);
  protected isLoading = signal(true);

  ngOnInit(): void {
    this.loadTodayTasks();
  }

  loadTodayTasks() {
    this.isLoading.set(true);
    
    return this.taskService.getTodayTasks().pipe(
      timeout(15000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: result => {
        this.todayTasks.set(
          [...result].sort((a, b) => new Date(a.taskStart).getTime() - new Date(b.taskStart).getTime())
        );
        this.isLoading.set(false);
      },
      error: err => {
        this.toast.error('Failed to load today\'s tasks: ' + err.message);
        this.isLoading.set(false);
      }
    });
  }
}
