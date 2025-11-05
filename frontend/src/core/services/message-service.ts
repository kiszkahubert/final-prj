import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { MessageReceived, MessageToSent, PeopleInfo } from '../../types/message';
import { WebsocketService } from './websocket-service';
import { AccountService } from './account-service';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private webSocketService = inject(WebsocketService);
  private accountService = inject(AccountService);
  private baseUrl = environment.apiUrl;
  private http = inject(HttpClient);

  private messagesSubject = new BehaviorSubject<MessageReceived[]>([]);
  public messages$ = this.messagesSubject.asObservable();

  getAllMessages() {
    return this.http.get<MessageReceived[]>('/api/chat/messages');
  }

  getFamilyPeople() {
    return this.http.get<PeopleInfo[]>('/api/family/people');
  }

  async connectWebSocket() {
    const user = this.accountService.getUser();
    if (!user?.token) {
      throw new Error('No authentication token found');
    }

    await this.webSocketService.connect(user.token);

    this.webSocketService.subscribeToFamilyChat((message) => {
      this.addMessageToList(message);
    });
  }

  sendMessage(dto: MessageToSent): void {
    this.webSocketService.sendMessage(dto);
  }

  setMessages(messages: MessageReceived[]): void {
    this.messagesSubject.next(this.sortMessagesByDate(messages));
  }

  private addMessageToList(message: MessageReceived) {
    const currentMessages = this.messagesSubject.getValue();
    const sortedMessages = this.sortMessagesByDate([...currentMessages, message]);
    this.messagesSubject.next(sortedMessages);
  }

  private sortMessagesByDate(messages: MessageReceived[]) {
    return messages.sort((a, b) => {
      return new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime();
    });
  }

  disconnectWebSocket() {
    this.webSocketService.disconnect();
  }
}
