import { Component, OnInit, computed, inject, signal, ElementRef, ViewChild } from '@angular/core';
import { ToastService } from '../../../core/services/toast-service';
import { DatePipe } from '@angular/common';
import { KidService } from '../../../core/services/kid-service';
import { TaskService } from '../../../core/services/task-service';
import { Kid } from '../../../types/kid';
import type { Task } from '../../../types/task';
import { DayInCalendar } from './day-in-calendar/day-in-calendar';
import { TaskForm } from '../task-form/task-form';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-calendar',
  imports: [DatePipe, DayInCalendar, TaskForm],
  templateUrl: './calendar.html',
  styleUrl: './calendar.css'
})
export class Calendar implements OnInit {
  @ViewChild('dayCalendarSection') dayCalendarSection!: ElementRef;
  @ViewChild('taskDialog') taskDialog!: ElementRef<HTMLDialogElement>;
  @ViewChild(TaskForm) taskFormComponent?: TaskForm;
  private kidService = inject(KidService);
  private taskService = inject(TaskService);
  private toast = inject(ToastService);
  viewMode = signal<'month' | 'week' | 'day'>('month');
  currentDate = signal(new Date());
  selectedDate = signal(new Date());
  kids = signal<Kid[]>([]);
  selectedKidId = signal<number | null>(null);
  editingTask = signal<Task | null>(null);
  tasks = signal<Task[]>([]);
  isLoadingKids = signal(true);
  isLoadingTasks = signal(true);
  isSyncing = signal(false);

  kidName = computed(() => {
    const id = this.selectedKidId();
    return id ? this.kids().find(k => k.id === id)?.name : undefined;
  });

  monthMatrix = computed(() => {
    const ref = this.currentDate();
    const first = new Date(ref.getFullYear(), ref.getMonth(), 1);
    const start = this.startOfWeek(first);
    const days: Date[] = [];
    for (let i = 0; i < 42; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      days.push(d);
    }
    return days;
  });

  weekDays = computed(() => {
    const start = this.startOfWeek(this.currentDate());
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      return d;
    });
  });

  tasksForDay = (d: Date) => {
    const kidName = this.kidName();
    return this.tasks()
      .filter(task => {
        const taskStart = new Date(task.taskStart);
        const matchesDate = this.isSameDay(taskStart, d);
        const matchesKid = !kidName || task.kidNames.includes(kidName);
        return matchesDate && matchesKid;
      })
      .sort((a, b) => new Date(a.taskEnd).getTime() - new Date(b.taskEnd).getTime());
  };

  tasksCountForDay = (d: Date) => this.tasksForDay(d).length;

  ngOnInit(): void {
    this.isLoadingKids.set(true);
    this.kidService.getKids().pipe(
      timeout(10000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: k => {
        this.kids.set(k);
        this.isLoadingKids.set(false);
      },
      error: err => {
        this.toast.error('Failed to load kids: ' + err.message);
        this.isLoadingKids.set(false);
      }
    });
    this.selectedDate.set(new Date());

    this.loadTasks();
  }

  loadTasks() {
    this.isLoadingTasks.set(true);

    this.taskService.getTasks().pipe(
      timeout(10000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: tasks => {
        this.tasks.set(tasks);
        this.isLoadingTasks.set(false);
      },
      error: err => {
        this.toast.error('Failed to load tasks: ' + err.message);
        this.isLoadingTasks.set(false);
      }
    });
  }

  setView(mode: 'month' | 'week' | 'day') { this.viewMode.set(mode); }
  prev() { this.shift(-1); }
  next() { this.shift(1); }

  private shift(dir: 1 | -1) {
    const d = new Date(this.currentDate());
    if (this.viewMode() === 'month') d.setMonth(d.getMonth() + dir);
    else if (this.viewMode() === 'week') d.setDate(d.getDate() + 7 * dir);
    else {
      d.setDate(d.getDate() + dir);
      const sel = new Date(this.selectedDate());
      sel.setDate(sel.getDate() + dir);
      this.selectedDate.set(sel);
    }
    this.currentDate.set(d);
  }

  selectKid(idStr: string) {
    const id = Number(idStr);
    this.selectedKidId.set(Number.isFinite(id) ? id : null);
  }

  selectDate(d: Date) {
    this.selectedDate.set(new Date(d));

    if (this.dayCalendarSection) {
      this.dayCalendarSection.nativeElement.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    }
  }

  isSameMonth(d: Date) {
    const ref = this.currentDate();
    return d.getFullYear() === ref.getFullYear() && d.getMonth() === ref.getMonth();
  }

  isSameDay(a: Date, b: Date) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  isoWeekOfYear(date: Date) {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const dayNum = d.getUTCDay() || 7;
    d.setUTCDate(d.getUTCDate() + 4 - dayNum);
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  }

  private startOfWeek(d: Date) {
    const day = d.getDay();
    const diff = (day + 6) % 7;
    const res = new Date(d);
    res.setDate(d.getDate() - diff);
    res.setHours(0, 0, 0, 0);
    return res;
  }

  openAddForm() {
    this.editingTask.set(null);
    setTimeout(() => {
      try {
        this.taskDialog?.nativeElement.showModal();
      } catch {

      }
    });
  }

  closeAddForm() {
    try {
      this.taskDialog?.nativeElement.close();
    } catch {

    }
    setTimeout(() => {
      this.taskFormComponent?.resetForm();
      this.editingTask.set(null);
    }, 180);
  }

  openEditForm(task: Task) {
    this.editingTask.set(task);
    setTimeout(() => {
      try {
        this.taskDialog?.nativeElement.showModal();
      } catch {

      }
    });
  }

  deleteTask(task: Task) {
    this.taskService.deleteTask(task.taskId).pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        if (this.editingTask() && this.editingTask()!.taskId === task.taskId) {
          this.closeAddForm();
        }
        this.toast.success('Task deleted successfully');
        this.loadTasks();
      },
      error: (err) => {
        this.toast.error(err?.message || 'Failed to delete task');
      }
    });
  }

  onTaskSubmitted(task: Task) {
    if (!task) {
      this.loadTasks();
      this.closeAddForm();
      return;
    }
    const start = new Date(task.taskStart);
    this.selectedDate.set(new Date(start.getFullYear(), start.getMonth(), start.getDate()));
    this.loadTasks();
    this.closeAddForm();
  }
}
