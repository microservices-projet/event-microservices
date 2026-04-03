export class Participation {
  id!: number;
  userId: number;
  eventId: number;
  emailParticipant: string;
  nbPlaces: number;
  status: 'confirmed' | 'pending' | 'cancelled';
  registrationDate: Date;
  constructor(
    userId: number,
    eventId: number,
    emailParticipant: string,
    nbPlaces: number,
    status: 'confirmed' | 'pending' | 'cancelled'
  ) {
    this.userId = userId;
    this.eventId = eventId;
    this.emailParticipant = emailParticipant;
    this.nbPlaces = nbPlaces;
    this.status = status;
    this.registrationDate = new Date();
  }
}
