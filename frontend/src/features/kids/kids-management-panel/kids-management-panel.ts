import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Kid, ChildAccessToken } from '../../../types/kid';
import { KidService } from '../../../core/services/kid-service';
import { ToastService } from '../../../core/services/toast-service';
import { AgePipe } from '../../../core/pipes/age-pipe';
import { DeleteButton } from '../../../shared/delete-button/delete-button';
import { CodeModal } from './code-modal/code-modal';
import { EditButton } from '../../../shared/edit-button/edit-button';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  selector: 'app-kids-management-panel',
  imports: [UpperCasePipe, AgePipe, EditButton, DeleteButton, CodeModal, RouterLink],
  templateUrl: './kids-management-panel.html',
  styleUrl: './kids-management-panel.css'
})
export class KidsManagementPanel implements OnInit {
  @ViewChild('codeModal') codeModal!: CodeModal;
  private kidService = inject(KidService);
  private toast = inject(ToastService);
  protected MAX_KIDS = 4;
  protected kids = signal<Kid[]>([]);
  protected accessTokens = signal<ChildAccessToken[]>([]);
  protected isLoading = signal(true);

  ngOnInit() {
    this.loadKids();
    this.loadAccessTokens();
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

  private loadAccessTokens() {
    this.kidService.getChildAccessTokens().subscribe({
      next: tokens => {
        this.accessTokens.set(tokens);
      },
      error: () => {
        this.toast.error('Failed to load access tokens');
      }
    });
  }

  protected deleteKid(kid: Kid) {
    const prev = this.kids();
    this.kids.set(prev.filter(k => k.id !== kid.id));
    this.kidService.deleteKid(kid.id!).pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        this.toast.success(`${kid.name} has been deleted successfully`);
      },
      error: (err) => {
        this.kids.set(prev);
        this.toast.error('Failed to delete ' + kid.name, err.message);
      }
    });
  }

  protected showQr(kid: Kid) {
    const qrHash = this.getQrHashForKid(kid.id!);
    this.codeModal.open('qr', kid, '', qrHash);
  }

  protected showPin(kid: Kid) {
    const pin = this.getPinForKid(kid.id!);
    this.codeModal.open('pin', kid, pin);
  }

  protected getPinForKid(kidId: number): string {
    const token = this.accessTokens().find(t => t.kidId === kidId);
    return token!.pin;
  }

  protected getQrHashForKid(kidId: number): string {
    const token = this.accessTokens().find(t => t.kidId === kidId);
    return token!.qrHash;
  }
}
