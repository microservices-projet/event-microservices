export type EventStatus = 'PUBLISHED' | 'DRAFT' | 'CANCELLED' | 'COMPLETED';

export interface Event {
  id: number;
  title: string;
  description: string;
  date: string;
  place: string;
  price: number;
  organizerId: number;
  imageUrl: string;
  nbPlaces: number;
  nbLikes: number;
  domaines: string[];
  status: EventStatus;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EventRequest {
  title: string;
  description: string;
  date: string;
  place: string;
  price: number;
  organizerId: number;
  imageUrl?: string;
  nbPlaces: number;
  nbLikes?: number;
  domaines?: string[];
  status?: EventStatus;
}
