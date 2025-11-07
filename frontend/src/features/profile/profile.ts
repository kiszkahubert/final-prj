import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountService } from '../../core/services/account-service';
import { ToastService } from '../../core/services/toast-service';
import { TextInput } from '../../shared/text-input/text-input';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, TextInput],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile {
  private accountService = inject(AccountService);
  private toastService = inject(ToastService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  protected showUsernameForm = signal(false);
  protected showPasswordForm = signal(false);
  protected showDeleteConfirm = signal(false);
  protected isUpdatingUsername = signal(false);
  protected isUpdatingPassword = signal(false);
  protected isDeletingAccount = signal(false);
  protected usernameForm: FormGroup;
  protected passwordForm: FormGroup;
  protected validationErrors = signal<string[]>([]);

  constructor() {
    this.usernameForm = this.fb.group({
      currentUsername: [{ value: this.getCurrentUsername(), disabled: true }],
      newUsername: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(20),
        this.sameAsCurrentUsernameValidator
      ]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20), this.sameAsCurrentPasswordValidator('currentPassword')]],
      confirmPassword: ['', [Validators.required, this.matchValues('newPassword')]]
    });

    this.deleteAccountForm = this.fb.group({
      confirmPassword: ['', [Validators.required]]
    });

    this.passwordForm.controls['newPassword'].valueChanges.subscribe(() => {
      this.passwordForm.controls['confirmPassword'].updateValueAndValidity();
    });

    this.passwordForm.controls['currentPassword'].valueChanges.subscribe(() => {
      this.passwordForm.controls['newPassword'].updateValueAndValidity();
    });
  }

  updateUsername() {
    if (this.usernameForm.valid) {
      const newUsername = this.usernameForm.get('newUsername')?.value;
      if (!newUsername) return;
      this.isUpdatingUsername.set(true);
      this.usernameForm.get('newUsername')?.disable();
      this.accountService.updateUsername(newUsername).pipe(
        timeout(10000),
        catchError(err => {
          if (err.name === 'TimeoutError') {
            return throwError(() => new Error('Request timed out'));
          }
          return throwError(() => err);
        })
      ).subscribe({
        next: () => {
          const currentUser = this.accountService.getUser();
          if (currentUser) {
            const updated = { ...currentUser, username: newUsername };
            localStorage.setItem('user', JSON.stringify(updated));
          }
          this.toastService.success('Username updated successfully! Please log in again.');
          this.showUsernameForm.set(false);
          this.usernameForm.reset();
          this.usernameForm.patchValue({ currentUsername: newUsername });
        },
        error: (err) => {
          this.isUpdatingUsername.set(false);
          this.usernameForm.get('newUsername')?.enable();
          if (err?.status === 403) {
            this.toastService.error('Username is already taken.');
          } else {
            this.toastService.error('Failed to update username: ' + err.message);
          }
          this.usernameForm.reset();
          this.usernameForm.patchValue({ currentUsername: this.getCurrentUsername() });
        },
        complete: () => {
          this.isUpdatingUsername.set(false);
          this.usernameForm.get('newUsername')?.enable();
          this.accountService.logout();
          this.router.navigateByUrl('/login');
        }
      });
    }
  }

  updatePassword() {
    if (this.passwordForm.valid) {
      const currentPassword = this.passwordForm.get('currentPassword')?.value;
      const newPassword = this.passwordForm.get('newPassword')?.value;
      if (!currentPassword || !newPassword) return;
      this.isUpdatingPassword.set(true);
      this.passwordForm.get('currentPassword')?.disable();
      this.passwordForm.get('newPassword')?.disable();
      this.passwordForm.get('confirmPassword')?.disable();
      this.accountService.updatePassword(currentPassword, newPassword).pipe(
        timeout(10000),
        catchError(err => {
          if (err.name === 'TimeoutError') {
            return throwError(() => new Error('Request timed out'));
          }
          return throwError(() => err);
        })
      ).subscribe({
        next: () => {
          this.toastService.success('Password updated successfully! Please log in again.');
          this.showPasswordForm.set(false);
          this.passwordForm.reset();
        },
        error: (err) => {
          this.isUpdatingPassword.set(false);
          this.passwordForm.get('currentPassword')?.enable();
          this.passwordForm.get('newPassword')?.enable();
          this.passwordForm.get('confirmPassword')?.enable();
          if (err?.status === 403 || err?.status === 400) {
            this.toastService.error('Current password is incorrect.');
          } else {
            this.toastService.error('Failed to update password: ' + err.message);
          }
          this.passwordForm.reset();
        },
        complete: () => {
          this.isUpdatingPassword.set(false);
          this.passwordForm.get('currentPassword')?.enable();
          this.passwordForm.get('newPassword')?.enable();
          this.passwordForm.get('confirmPassword')?.enable();
          this.accountService.logout();
          this.router.navigateByUrl('/login');
        }
      });
    }
  }

  deleteAccount() {
    this.isDeletingAccount.set(true);
    this.accountService.deleteAccount().pipe(
      timeout(10000),
      catchError(err => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Request timed out'));
        }
        return throwError(() => err);
      })
    ).subscribe({
      next: () => {
        this.toastService.success('Account deleted successfully');
        this.accountService.logout();
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.isDeletingAccount.set(false);
        this.toastService.error('Failed to delete account: ' + err.message);
      },
      complete: () => {
        this.isDeletingAccount.set(false);
      }
    });
  }

  matchValues(matchTo: string) {
    return (control: AbstractControl) => {
      const parent = control.parent;
      if (!parent) return null;

      const matchValue = parent.get(matchTo)?.value;
      return control.value === matchValue ? null : { passwordMismatch: true };
    };
  }

  sameAsCurrentUsernameValidator = (control: AbstractControl) => {
    const currentUsername = this.getCurrentUsername();
    if (!control.value || control.value !== currentUsername) return null;
    return { sameAsCurrent: true };
  }

  sameAsCurrentPasswordValidator(matchTo: string) {
    return (control: AbstractControl): ValidationErrors | null => {
      const parent = control.parent;
      if (!parent) return null;

      const otherValue = parent.get(matchTo)?.value;
      return control.value && control.value === otherValue ? { sameAsCurrentPassword: true } : null;
    };
  }

  protected getCurrentUsername() {
    return this.accountService.getUser()?.username;
  }

  toggleUsernameForm() {
    this.showUsernameForm.update(v => !v);
    if (!this.showUsernameForm() && !this.isUpdatingUsername()) {
      this.usernameForm.reset();
      this.usernameForm.patchValue({ currentUsername: this.getCurrentUsername() });
    }
    if (this.showUsernameForm()) {
      this.usernameForm.get('newUsername')?.enable();
    }
  }

  togglePasswordForm() {
    this.showPasswordForm.update(v => !v);
    if (!this.showPasswordForm() && !this.isUpdatingPassword()) {
      this.passwordForm.reset();
    }
    if (this.showPasswordForm()) {
      this.passwordForm.get('currentPassword')?.enable();
      this.passwordForm.get('newPassword')?.enable();
      this.passwordForm.get('confirmPassword')?.enable();
    }
  }

  toggleDeleteConfirm() {
    this.showDeleteConfirm.update(v => !v);
  }
}