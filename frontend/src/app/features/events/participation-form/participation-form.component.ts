import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-participation-form',
  templateUrl: './participation-form.component.html',
  styleUrls: ['./participation-form.component.css'],
})
export class ParticipationFormComponent implements OnInit {
  eventId!: number;
  unitPrice!: number;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.params['id']);
    this.unitPrice = Number(this.route.snapshot.params['prix']);
  }
}
