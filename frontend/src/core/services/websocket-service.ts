import { Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';
import { MessageReceived, MessageToSent } from '../../types/message';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private client: Client | null = null;
  private isConnected = signal(false);

  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const socket = new SockJS(environment.apiUrl + 'chat-websocket');
      this.client = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        onConnect: () => {
          console.log('WebSocket connected successfully');
          this.isConnected.set(true);
          resolve();
        },
        onStompError: (frame) => {
          console.error('STOMP connection error: ', frame);
          this.isConnected.set(false);
          reject(frame);
        },
        onWebSocketError: (error) => {
          console.error('WebSocket connection error: ', error);
          this.isConnected.set(false);
          reject(error);
        }
      });

      this.client.activate();
    });
  }

  subscribeToFamilyChat(callback: (message: MessageReceived) => void) {
    if (!this.client || !this.isConnected()) {
      throw new Error('WebSocket not connected');
    }

    return this.client.subscribe('/topic/familyChat', (message) => {
      const parsedMessage: MessageReceived = JSON.parse(message.body);
      callback(parsedMessage);
    });
  }

  sendMessage(message: MessageToSent) {
    if (!this.client || !this.isConnected()) {
      throw new Error('WebSocket not connected');
    }

    this.client.publish({
      destination: '/app/sendMessage',
      body: JSON.stringify(message)
    });
  }

  disconnect() {
    if (this.client) {
      console.log('Disconnecting WebSocket...');
      this.client.deactivate();
      this.isConnected.set(false);
    }
  }
}