import { Component, OnInit } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { futurDateValidator } from '../../../shared/validators/futur-date.validator';
import { DataService } from '../../../shared/services/data.service';
import { AuthService } from '../../../core/services/auth.service';
import { EventRequest } from '../../../models/event';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css'],
  standalone: false
})
export class AddEventComponent implements OnInit {
  eventForm!: FormGroup;
  loading = false;
  error = '';
  editMode = false;
  editId?: number;

  constructor(
    private eventService: DataService,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.editId = this.route.snapshot.params['id'] ? Number(this.route.snapshot.params['id']) : undefined;
    this.editMode = !!this.editId;

    this.eventForm = new FormGroup({
      title: new FormControl('', [Validators.required, Validators.minLength(3)]),
      description: new FormControl('', [Validators.required, Validators.minLength(10)]),
      date: new FormControl('', [Validators.required, ...(this.editMode ? [] : [futurDateValidator(1)])]),
      prix: new FormControl('', [Validators.required, Validators.pattern('^\\d+(\\.\\d+)?$')]),
      nbrPlace: new FormControl('', [Validators.required, Validators.min(1)]),
      lieu: new FormControl('', [Validators.required]),
      urlImage: new FormControl(''),
      domaines: new FormArray([new FormControl('')]),
    });

    if (this.editMode && this.editId) {
      this.loadEvent(this.editId);
    }
  }

  get title() { return this.eventForm.get('title'); }
  get description() { return this.eventForm.get('description'); }
  get date() { return this.eventForm.get('date'); }
  get domaines() { return this.eventForm.get('domaines') as FormArray; }

  addDomain(): void {
    this.domaines.push(new FormControl('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]));
  }

  removeDomain(index: number): void {
    this.domaines.removeAt(index);
  }

  private loadEvent(id: number): void {
    this.loading = true;
    this.eventService.getById(id).subscribe({
      next: (event) => {
        const dateStr = event.date ? event.date.substring(0, 10) : '';
        this.eventForm.patchValue({
          title: event.title,
          description: event.description,
          date: dateStr,
          prix: event.price,
          nbrPlace: event.nbPlaces,
          lieu: event.place,
          urlImage: event.imageUrl || '',
        });

        this.domaines.clear();
        if (event.domaines && event.domaines.length > 0) {
          event.domaines.forEach(d => this.domaines.push(new FormControl(d)));
        } else {
          this.domaines.push(new FormControl(''));
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger l\'evenement.';
        this.loading = false;
      },
    });
  }

  onSubmit(): void {
    if (this.eventForm.invalid || this.loading) return;
    const organizerId = this.auth.currentUser?.id;
    if (!organizerId || organizerId < 1) {
      this.error = 'Connectez-vous (compte local avec identifiant valide) pour publier un evenement en tant qu\'organisateur.';
      return;
    }
    this.loading = true;
    this.error = '';

    const f = this.eventForm.value;
    const payload: EventRequest = {
      title: f.title,
      description: f.description,
      date: f.date + 'T00:00:00',
      price: Number(f.prix),
      nbPlaces: Number(f.nbrPlace),
      place: f.lieu,
      imageUrl: f.urlImage || '',
      nbLikes: 0,
      organizerId,
      domaines: f.domaines.filter((d: string) => d && d.trim()),
      status: 'PUBLISHED',
    };

    const request$ = this.editMode && this.editId
      ? this.eventService.updateEvent(this.editId, payload)
      : this.eventService.createEvent(payload);

    request$.subscribe({
      next: () => this.router.navigate(['/events']),
      error: (err) => {
        this.error = err?.error?.message || 'Erreur lors de la sauvegarde.';
        this.loading = false;
      },
    });
  }
}
