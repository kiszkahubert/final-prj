import { Component, inject,signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountFormCreds } from '../../../types/user';
import { AccountService } from '../../../core/services/account-service';
import { Router, RouterLink } from '@angular/router';
import { TextInput } from '../../../shared/text-input/text-input';
import { ToastService } from '../../../core/services/toast-service';
import { timeout } from 'rxjs/operators';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, TextInput, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  private accountService = inject(AccountService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  protected creds = {} as AccountFormCreds;
  protected credentialsForm: FormGroup;
  protected validationErrors = signal<string[]>([]);
  protected isRegistering = signal(false);

  constructor() {
    this.credentialsForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(20)]],
      confirmPassword: ['', [Validators.required, this.matchValues('password')]]
    });

    this.credentialsForm.controls['password'].valueChanges.subscribe(() => {
      this.credentialsForm.controls['confirmPassword'].updateValueAndValidity();
    });
  }

  register() {
    if (this.credentialsForm.valid) {
      this.isRegistering.set(true);
      const formData = this.credentialsForm.value;

      this.accountService.register(formData).pipe(
        timeout(10000),
      ).subscribe({
        next: () => {
          this.router.navigateByUrl('/home');
          this.toast.success('Signed up successfully');
          this.isRegistering.set(false);
        },
        error: error => {
          console.log(error);
          this.isRegistering.set(false);
          if (error?.status === 409) {
            this.toast.error('Username already taken');
          } else {
            this.toast.error('Failed to register');
          }
          this.validationErrors.set(error);
        }
      });
    }
  }

  matchValues(matchTo: string) {
    return (control: AbstractControl) => {
      const parent = control.parent;
      if (!parent) return null;

      const matchValue = parent.get(matchTo)?.value;
      return control.value === matchValue ? null : { passwordMismatch: true };
    };
  }
}
