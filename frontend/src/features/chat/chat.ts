import { Component, inject, OnInit, OnDestroy, signal, effect, ViewChild, ElementRef } from '@angular/core';
import { MessageService } from '../../core/services/message-service';
import { MessageReceived, MessageToSent, PeopleInfo } from '../../types/message';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ToastService } from '../../core/services/toast-service';
import { AccountService } from '../../core/services/account-service';
import { timeout, catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { TimeAgoPipe } from '../../core/pipes/time-ago-pipe';

@Component({
  selector: 'app-chat',
  imports: [FormsModule, TimeAgoPipe],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class Chat implements OnInit, OnDestroy {
  @ViewChild('messageEndRef') messageEndRef!: ElementRef;
  private messageService = inject(MessageService);
  private accountService = inject(AccountService);
  private toast = inject(ToastService);
  messages = signal<MessageReceived[]>([]);
  messageContent = signal<string>('');
  isConnecting = signal<boolean>(true);
  isDisconnected = signal<boolean>(false);
  familyPeople = signal<PeopleInfo[]>([]);
  currentUser = signal<string | null>(null);
  connectingDots = signal<string>('.');
  private dotsInterval?: number;

  constructor() {
    effect(() => {
      const currentMessages = this.messages();
      if (currentMessages.length > 0) {
        this.scrollToBottom();
      }
    });

    this.messageService.messages$.pipe(
      takeUntilDestroyed()
    ).subscribe(messages => {
      this.messages.set(messages);
    });
  }

  async ngOnInit() {
    this.startConnectingAnimation();
    await this.initializeChat();
  }

  ngOnDestroy(): void {
    this.stopConnectingAnimation();
    this.messageService.disconnectWebSocket();
  }

  private async initializeChat() {
    try {
      await this.loadMessages();
      await this.loadFamilyPeople();
      await this.messageService.connectWebSocket();
      this.currentUser.set(this.accountService.getCurrentUsername());
      this.stopConnectingAnimation();
      this.isConnecting.set(false);
      this.isDisconnected.set(false);
    } catch (error) {
      this.stopConnectingAnimation();
      this.isConnecting.set(false);
      this.isDisconnected.set(true);
      this.toast.error('Failed to connect to chat');
    }
  }

  private async loadMessages(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.messageService.getAllMessages().pipe(
        timeout(10000),
        catchError(err => {
          if (err.name === 'TimeoutError') {
            return throwError(() => new Error('Connection timeout while loading messages'));
          }
          return throwError(() => err);
        })
      ).subscribe({
        next: messages => {
          this.messageService.setMessages(messages);
          resolve();
        },
        error: (err) => reject(err)
      });
    });
  }

  private async loadFamilyPeople(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.messageService.getFamilyPeople().pipe(
        timeout(10000),
        catchError(err => {
          if (err.name === 'TimeoutError') {
            return throwError(() => new Error('Connection timeout while loading family members'));
          }
          return throwError(() => err);
        })
      ).subscribe({
        next: people => {
          this.familyPeople.set(people);
          resolve();
        },
        error: (err) => reject(err)
      });
    });
  }

  sendMessage() {
    const content = this.messageContent().trim();
    if (content) {
      const dto: MessageToSent = {
        senderType: 'PARENT',
        content
      };

      try {
        this.messageService.sendMessage(dto);
      } catch (error) {
        this.toast.error('Failed to send message');
      }
      this.messageContent.set('');
    }
  }

  scrollToBottom() {
    setTimeout(() => {
      if (this.messageEndRef) {
        this.messageEndRef.nativeElement.scrollIntoView({ behavior: 'smooth' });
      }
    });
  }

  getSenderName(message: MessageReceived) {
    const people = this.familyPeople();
    const person = people.find(p => p.id === message.senderId && p.type === message.senderType);

    if (person) {
      if (message.senderType === 'PARENT' && person.name === this.currentUser()) {
        return 'Me';
      }
      return person.name;
    }

    return message.senderType === 'PARENT' ? 'Me' : 'Kid';
  }

  getSenderInitial(message: MessageReceived) {
    const people = this.familyPeople();
    const person = people.find(p => p.id === message.senderId && p.type === message.senderType);

    if (person) {
      return person.name.charAt(0).toUpperCase();
    }

    return message.senderType === 'PARENT' ? 'P' : 'K';
  }

  private startConnectingAnimation() {
    this.dotsInterval = window.setInterval(() => {
      const current = this.connectingDots();
      if (current === '...') {
        this.connectingDots.set('.');
      } else {
        this.connectingDots.set(current + '.');
      }
    }, 500);
  }

  private stopConnectingAnimation() {
    if (this.dotsInterval) {
      clearInterval(this.dotsInterval);
      this.dotsInterval = undefined;
    }
  }

  async retryConnection() {
    this.isDisconnected.set(false);
    this.isConnecting.set(true);
    this.startConnectingAnimation();
    await this.initializeChat();
  }
}
