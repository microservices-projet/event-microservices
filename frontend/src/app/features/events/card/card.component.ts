import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Event } from '../../../models/event';
import { Router } from '@angular/router';

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.css'],
  standalone: false
})
export class CardComponent {
  @Input() event!: Event;
  @Output() notifLike = new EventEmitter<Event>();
  @Output() notifBuy = new EventEmitter<Event>();

  constructor(private router: Router) {}

  likeEvent(): void {
    this.notifLike.emit(this.event);
  }

  buyEvent(): void {
    this.notifBuy.emit(this.event);
  }

  dateExpired(): boolean {
    return new Date(this.event.date) < new Date();
  }

  participate(): void {
    this.router.navigate(['/events/participate', this.event.id, this.event.price]);
  }
}
