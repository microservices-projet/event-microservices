import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserResponse } from '../../models/user';

@Injectable({ providedIn: 'root' })
export class UserLookupService {
  constructor(private http: HttpClient) {}

  getById(id: number): Observable<UserResponse | null> {
    if (!id || id < 1) return of(null);
    return this.http.get<UserResponse>(`/api/users/${id}`).pipe(catchError(() => of(null)));
  }
}
