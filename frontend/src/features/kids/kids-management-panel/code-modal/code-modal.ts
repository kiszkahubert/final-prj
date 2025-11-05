import { Component, ElementRef, ViewChild, inject, output } from '@angular/core';
import { Kid } from '../../../../types/kid';
import QRCode from 'qrcode';
import { ToastService } from '../../../../core/services/toast-service';

@Component({
  selector: 'app-code-modal',
  imports: [],
  templateUrl: './code-modal.html',
  styleUrl: './code-modal.css'
})
export class CodeModal {
  @ViewChild('codeModal') modalRef!: ElementRef<HTMLDialogElement>;
  private toast = inject(ToastService);
  protected selectedKid: Kid | null = null;
  protected modalType: 'qr' | 'pin' | null = null;
  protected pin = '';
  protected qrHash = '';
  protected qrCodeDataUrl = '';
  closeModal = output();

  async open(type: 'qr' | 'pin', kid: Kid, pin?: string, qrHash?: string) {
    this.modalType = type;
    this.selectedKid = kid;
    this.pin = pin!;
    this.qrHash = qrHash!;
    
    if (type === 'qr' && qrHash) {
      await this.generateQRCode(qrHash);
    }
    
    this.modalRef.nativeElement.showModal();
  }

  close() {
    this.modalRef.nativeElement.close();
    setTimeout(() => {
        this.modalType = null;
        this.selectedKid = null;
        this.pin = '';
        this.qrHash = '';
        this.qrCodeDataUrl = '';
        this.closeModal.emit();
    }, 200);  
  }

  private async generateQRCode(qrHash: string) {
    try {
      const loginUrl = 'https://localhost:8080/auth/qr?hash=' + encodeURIComponent(qrHash);
      this.qrCodeDataUrl = await QRCode.toDataURL(loginUrl, {
        width: 200,
        margin: 2,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      });
    } catch (error) {
      this.toast.error('Error generating QR code: ' + error);
      this.qrCodeDataUrl = '';
    }
  }
}
