import { Component, input, Self, signal } from '@angular/core';
import { ControlValueAccessor, FormControl, NgControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-text-input',
  imports: [ReactiveFormsModule],
  templateUrl: './text-input.html',
  styleUrl: './text-input.css'
})
export class TextInput implements ControlValueAccessor {
  label = input<string>('');
  type = input<string>('text');
  showPassword = signal(false);
  minDate = input<string>('');
  maxDate = input<string>('');

  constructor(@Self() public ngControl: NgControl) {
    this.ngControl.valueAccessor = this;
  }

  writeValue(obj: any): void {}
  registerOnChange(fn: any): void {}
  registerOnTouched(fn: any): void {}

  get control(): FormControl {
    return this.ngControl.control as FormControl;
  }

  get inputType() {
    if (this.type() === 'password') {
      return this.showPassword() ? 'text' : 'password';
    }
    return this.type();
  }

  toggleShowPassword() {
    this.showPassword.update(v => !v);
  }
}