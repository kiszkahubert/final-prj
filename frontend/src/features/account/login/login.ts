import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { AccountService } from '../../../core/services/account-service';
import { Router, RouterLink } from '@angular/router';
import { TextInput } from '../../../shared/text-input/text-input';
import { ToastService } from '../../../core/services/toast-service';
import { timeout } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, TextInput, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private accountService = inject(AccountService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  protected validationErrors = signal<string[]>([]);
  protected isLoggingIn = signal(false);
  protected credentialsForm: FormGroup = this.fb.group({
    username: [''],
    password: ['']
  });

  login() {
    const formData = this.credentialsForm.value;
    this.isLoggingIn.set(true);
    this.accountService.login(formData).pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        this.router.navigateByUrl('/home');
        this.toast.success('Signed in successfully');
        this.isLoggingIn.set(false);
      },
      error: () => {
        this.toast.error('Failed to sign in');
        this.isLoggingIn.set(false);
        const active = document.activeElement as HTMLElement | null;
        active?.blur();
        this.credentialsForm.reset();
        Object.values(this.credentialsForm.controls).forEach(c => {
          c.markAsPristine();
          c.markAsUntouched();
        });
      }
    });
  }
}
