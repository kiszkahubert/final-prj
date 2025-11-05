import { Component, inject, OnInit, signal, HostListener, ElementRef } from '@angular/core';
import { AccountService } from '../../core/services/account-service';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ToastService } from '../../core/services/toast-service';

@Component({
  selector: 'app-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './nav.html',
  styleUrl: './nav.css'
})
export class Nav implements OnInit {
  protected accountService = inject(AccountService);
  protected toast = inject(ToastService);
  private router = inject(Router);
  private elRef = inject(ElementRef<HTMLElement>);
  protected menuOpen = signal(false);

  ngOnInit(): void {
    document.documentElement.setAttribute('data-theme', 'dark');
  }

  handleSelectUserItem() {
    const elem = document.activeElement as HTMLDivElement;
    if (elem) elem.blur();
  }

  logout() {
    this.accountService.logout();
    this.toast.success('Logged out successfully')
    this.router.navigateByUrl('/');
  }

  toggleMenu() {
    this.menuOpen.update(v => !v);
  }

  closeMenu() {
    if (this.menuOpen()) this.menuOpen.set(false);
  }

  @HostListener('window:resize') 
  onResize() {
    if (window.innerWidth >= 768 && this.menuOpen()) this.menuOpen.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(event: MouseEvent) {
    if (!this.menuOpen()) return;
    const target = event.target as HTMLElement | null;
    if (!target) return;
    if (!this.elRef.nativeElement.contains(target)) {
      this.menuOpen.set(false);
    }
  }
}
