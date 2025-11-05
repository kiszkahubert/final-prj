import { Component, EventEmitter, Output, Input, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { TextInput } from '../../../shared/text-input/text-input';
import { TaskService } from '../../../core/services/task-service';
import { ToastService } from '../../../core/services/toast-service';
import { KidService } from '../../../core/services/kid-service';
import { Kid } from '../../../types/kid';
import { Task } from '../../../types/task';
import { timeout } from 'rxjs';

@Component({
  selector: 'app-task-form',
  imports: [ReactiveFormsModule, TextInput],
  templateUrl: './task-form.html',
  styleUrl: './task-form.css'
})
export class TaskForm {
  @Output() submitted = new EventEmitter<Task>();
  private _task: Task | undefined = undefined;
  @Input() set task(value: Task | undefined) {
    if (value) {
      this._task = value;
      const toLocalInput = (iso: string) => iso.slice(0, 16);
      const selectedKidId = value.kidIds && value.kidIds.length > 0 ? value.kidIds[0] : null;
      this.form.patchValue({
        selectedKidId: selectedKidId,
        title: value.title,
        description: value.description,
        start: toLocalInput(value.taskStart),
        end: toLocalInput(value.taskEnd)
      });
    } else {
      this._task = undefined;
      this.form.reset();
    }
  }
  private taskService = inject(TaskService);
  private kidService = inject(KidService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);
  submitting = signal(false);
  kids = signal<Kid[]>([]);

  private pastDateValidator = (control: AbstractControl) => {
    const value: string | null = control.value;
    if (!value) return null;
    const normalized = value.length === 16 ? value + ':00' : value.slice(0, 19);
    const selected = new Date(normalized);
    if (isNaN(selected.getTime())) return null;
    const now = new Date();
    if (selected.getTime() + 59 * 1000 < now.getTime()) {
      return { pastDate: true };
    }
    return null;
  };

  private dateOrderValidator = (group: AbstractControl) => {
    if (!group.get) return null;
    const startCtrl = group.get('start');
    const endCtrl = group.get('end');
    if (!startCtrl || !endCtrl) return null;
    const startVal: string | null = startCtrl.value;
    const endVal: string | null = endCtrl.value;
    if (!startVal || !endVal) {
      if (endCtrl.hasError('endBeforeStart')) {
        const { endBeforeStart, ...rest } = endCtrl.errors || {};
        endCtrl.setErrors(Object.keys(rest).length ? rest : null);
        endCtrl.updateValueAndValidity({ onlySelf: true, emitEvent: false });
      }
      return null;
    }
    const normalize = (v: string) => v.length === 16 ? v + ':00' : v.slice(0, 19);
    const startDate = new Date(normalize(startVal));
    const endDate = new Date(normalize(endVal));
    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) return null;
    if (endDate.getTime() <= startDate.getTime()) {
      const existing = endCtrl.errors || {};
      endCtrl.setErrors({ ...existing, endBeforeStart: true });
    } else if (endCtrl.hasError('endBeforeStart')) {
      const { endBeforeStart, ...rest } = endCtrl.errors || {};
      endCtrl.setErrors(Object.keys(rest).length ? rest : null);
    }
    return null;
  };

  form = this.fb.nonNullable.group({
    selectedKidId: [null as number | null, [Validators.required]],
    title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.required]],
    start: ['', [Validators.required, this.pastDateValidator]],
    end: ['', [Validators.required, this.pastDateValidator]]
  }, { validators: [this.dateOrderValidator] });

  constructor() {
    this.loadKids();
  }

  private loadKids() {
    this.kidService.getKids().pipe(
      timeout(10000)
    ).subscribe({
      next: kids => this.kids.set(kids),
      error: () => this.toast.error("Failed to load kids")
    })
  }

  resetForm() {
    this.form.reset();
    this._task = undefined;
  }

  onKidSelectionChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    const selectedKidId = select.value ? Number(select.value) : null;

    this.form.patchValue({
      selectedKidId: selectedKidId
    });
  }

  getSelectedKidName() {
    const selectedKidId = this.form.get('selectedKidId')?.value;
    if (!selectedKidId) return '';
    const selectedKid = this.kids().find(kid => kid.id === selectedKidId);
    return selectedKid ? selectedKid.name : '';
  }

  get currentDateTime() {
    const now = new Date();
    const year = now.getFullYear();
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const day = now.getDate().toString().padStart(2, '0');
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  get maxDateTime() {
    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() + 2);
    const year = maxDate.getFullYear();
    const month = (maxDate.getMonth() + 1).toString().padStart(2, '0');
    const day = maxDate.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}T23:59`;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const normalizeLocal = (value: string) => {
      if (!value) return value;
      return value.length === 16 ? `${value}:00` : value.slice(0, 19);
    };
    const taskStart = normalizeLocal(v.start);
    const taskEnd = normalizeLocal(v.end);

    try {
      this.submitting.set(true);
      if (this.task) {
        this.taskService.updateTask(this.task.taskId, {
          kidIds: v.selectedKidId ? [v.selectedKidId] : [],
          title: v.title,
          description: v.description,
          taskStart,
          taskEnd,
          status: this.task.status ?? 'PENDING',
          note: this.task.note ?? ''
        }).pipe(
          timeout(10000)
        ).subscribe({
          next: (updated: Task) => {
            this.toast.success('Task updated successfully');
            this.form.reset();
            this._task = undefined;
            this.submitted.emit(updated);
          },
          error: (error: any) => {
            this.toast.error(error?.message ?? 'Failed to update task');
          },
          complete: () => {
            this.submitting.set(false);
          }
        });
      } else {
        this.taskService.addTask({
          kidIds: v.selectedKidId ? [v.selectedKidId] : [],
          title: v.title,
          description: v.description,
          taskStart,
          taskEnd,
          status: 'PENDING'
        }).subscribe({
          next: (created: Task) => {
            this.toast.success('Task added successfully');
            this.form.reset();
            this._task = undefined;
            this.submitted.emit(created);
          },
          error: (error: any) => {
            this.toast.error(error?.message ?? 'Failed to add task');
          },
          complete: () => {
            this.submitting.set(false);
          }
        });
      }
    } catch (error: any) {
      this.toast.error(error?.message ?? 'Failed to add task');
      this.submitting.set(false);
    }
  }

  get task(): Task | undefined {
    return this._task;
  }
}
