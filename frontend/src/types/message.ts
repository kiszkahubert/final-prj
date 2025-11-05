export type MessageReceived = {
  id: string;
  senderType: string;
  senderId: number;
  content: string;
  sentAt: string;
}

export type MessageToSent = {
  senderType: string;
  content: string;
}

export type PeopleInfo = {
  id: number;
  name: string;
  type: string;
}