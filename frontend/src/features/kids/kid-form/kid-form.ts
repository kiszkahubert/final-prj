import { Component, OnInit, inject, signal, effect, DestroyRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { KidService } from '../../../core/services/kid-service';
import { take, timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { ToastService } from '../../../core/services/toast-service';
import { TextInput } from '../../../shared/text-input/text-input';
import { AgePipe } from '../../../core/pipes/age-pipe';

@Component({
  selector: 'app-kid-form',
  imports: [ReactiveFormsModule, TextInput],
  templateUrl: './kid-form.html',
  styleUrl: './kid-form.css'
})
export class KidForm implements OnInit {
  private kidService = inject(KidService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);
  private agePipe = new AgePipe();
  protected kidForm: FormGroup;
  protected isEditMode = signal(false);
  protected kidId = signal<number | null>(null);
  protected maxDate: string;
  protected isSubmitting = signal(false);
  protected isLoading = signal(false);

  constructor() {
    const today = new Date();
    this.maxDate = today.toISOString().split('T')[0];

    this.kidForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      birthDate: ['', [Validators.required, this.ageValidator.bind(this)]]
    });

    effect(() => {
      const id = this.kidId();
      if (id) {
        this.loadKid(id);
      }
    });
  }

  ngOnInit(): void {
    this.route.params.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(params => {
      if (params['id']) {
        this.isEditMode.set(true);
        this.kidId.set(+params['id']);
      } else {
        this.isEditMode.set(false);
        this.kidId.set(null);
      }
    });
  }

  private loadKid(kidId: number) {
    this.isLoading.set(true);
    this.kidService.getKid(kidId).pipe(
      timeout(15000),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (kid) => {
        this.kidForm.patchValue({
          name: kid.name,
          birthDate: this.normalizeBirthDate(kid.birthDate)
        });
        this.isLoading.set(false);
      },
      error: (err) => {
        this.router.navigate(['/kids']);
        this.toast.error(`Failed to load kid data: ${err.message}`);
        this.isLoading.set(false);
      }
    });
  }

  onSubmit() {
    if (this.kidForm.invalid || this.isSubmitting()) {
      this.markFormGroupTouched();
      return;
    }

    const kidData = {
      name: this.kidForm.value.name,
      birthDate: this.normalizeBirthDate(this.kidForm.value.birthDate)
    };
    const kidId = this.kidId();

    if (this.isEditMode()) {
      this.isSubmitting.set(true);
      this.kidService.updateKid(kidId!, kidData).pipe(
        timeout(10000),
        catchError(err => {
          if (err.name === 'TimeoutError') {
            return throwError(() => new Error('Request timed out'));
          }
          return throwError(() => err);
        }),
        takeUntilDestroyed(this.destroyRef)
      ).subscribe({
        next: () => {
          this.toast.success(`${kidData.name} has been updated successfully`);
          this.router.navigate(['/kids']);
        },
        error: err => {
          this.toast.error(`Failed to update kid: ${err.message}`);
          this.isSubmitting.set(false);
        }
      });
      return;
    }

    this.isSubmitting.set(true);
    this.kidService.getKids().pipe(
      take(1), 
      timeout(10000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: kids => {
        if (kids.length >= 4) {
          this.toast.error('Kid limit reached. You already have 4 kids. You cannot add more.');
          this.isSubmitting.set(false);
          return;
        }
        this.kidService.addKid(kidData).pipe(
          timeout(10000),
          catchError(err => {
            if (err.name === 'TimeoutError') {
              return throwError(() => new Error('Request timed out'));
            }
            return throwError(() => err);
          }),
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          next: () => {
            this.toast.success(`${kidData.name} has been added successfully`);
            this.router.navigate(['/kids']);
          },
          error: err => {
            this.toast.error(`Failed to add kid: ${err.message}`);
            this.isSubmitting.set(false);
          }
        });
      },
      error: err => {
        this.toast.error(`Failed to add kid: ${err.message}`);
        this.isSubmitting.set(false);
      }
    });
  }

  onCancel() {
    this.router.navigate(['/kids']);
  }

  private markFormGroupTouched() {
    Object.keys(this.kidForm.controls).forEach(key => {
      const control = this.kidForm.get(key);
      control?.markAsTouched();
    });
  }

  private ageValidator(control: any) {
    if (!control.value) {
      return null;
    }
    
    const actualAge = this.agePipe.transform(control.value);
    
    if (actualAge > 18) {
      return { tooOld: { actualAge, maxAge: 18 } };
    }
    
    if (actualAge < 0) {
      return { futureDate: true };
    }
    
    return null;
  }

  private normalizeBirthDate(date: string) {
    if (!date) return '';
    if (date.includes('T')) {
      const d = new Date(date);
      if (!isNaN(d.getTime())) return d.toISOString().split('T')[0];
    }
    const isoTry = new Date(date);
    if (!isNaN(isoTry.getTime()) && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date;
    }
    const m = date.match(/^(\d{1,2})[.\/-](\d{1,2})[.\/-](\d{4})$/);
    if (m) {
      const dd = parseInt(m[1], 10);
      const mm = parseInt(m[2], 10) - 1;
      const yyyy = parseInt(m[3], 10);
      const d = new Date(yyyy, mm, dd);
      if (!isNaN(d.getTime())) return d.toISOString().split('T')[0];
    }
    return date;
  }

  get pageTitle() {
    return this.isEditMode() ? 'Edit Kid Profile' : 'Add New Kid Profile';
  }

  get submitButtonText() {
    if (this.isSubmitting()) {
      return this.isEditMode() ? 'Updating...' : 'Adding...';
    }
    return this.isEditMode() ? 'Update Kid' : 'Add Kid';
  }
}
