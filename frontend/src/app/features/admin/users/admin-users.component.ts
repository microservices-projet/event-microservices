import { animate, style, transition, trigger } from '@angular/animations';
import { Component, OnInit } from '@angular/core';
import { UserResponse } from '../../../models/user';
import { AdminReadService } from '../services/admin-read.service';
import { AdminWriteService } from '../services/admin-write.service';

type SortKey = 'id' | 'username' | 'email' | 'role' | 'status' | 'createdAt';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  animations: [
    trigger('detailExpand', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('200ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1 })),
      ]),
      transition(':leave', [animate('140ms ease', style({ opacity: 0 }))]),
    ]),
  ],
})
export class AdminUsersComponent implements OnInit {
  users: UserResponse[] = [];
  loading = true;
  query = '';
  role = '';
  status = '';
  sortKey: SortKey = 'id';
  sortDir: 1 | -1 = 1;
  expandedUserId: number | null = null;

  constructor(
    private readonly read: AdminReadService,
    private readonly write: AdminWriteService,
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  get sortedUsers(): UserResponse[] {
    const key = this.sortKey;
    const dir = this.sortDir;
    return [...this.users].sort((a, b) => {
      const va = a[key];
      const vb = b[key];
      if (va === vb) return 0;
      if (va == null) return 1;
      if (vb == null) return -1;
      if (typeof va === 'number' && typeof vb === 'number') {
        return va < vb ? -dir : dir;
      }
      return String(va).localeCompare(String(vb), undefined, { sensitivity: 'base' }) * dir;
    });
  }

  toggleSort(key: SortKey): void {
    if (this.sortKey === key) {
      this.sortDir = this.sortDir === 1 ? -1 : 1;
    } else {
      this.sortKey = key;
      this.sortDir = 1;
    }
  }

  sortIndicator(key: SortKey): string {
    if (this.sortKey !== key) return '';
    return this.sortDir === 1 ? '▲' : '▼';
  }

  toggleExpand(user: UserResponse): void {
    this.expandedUserId = this.expandedUserId === user.id ? null : user.id;
  }

  onApply(): void {
    if (this.loading) return;
    this.reload();
  }

  clearFilters(): void {
    this.query = '';
    this.role = '';
    this.status = '';
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.expandedUserId = null;
    this.read.getUsers({ query: this.query, role: this.role, status: this.status }).subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  setRole(user: UserResponse, r: 'USER' | 'ADMIN' | 'MODERATOR'): void {
    this.write.updateUserRole(user.id, r).subscribe({ next: () => this.reload() });
  }

  setStatus(user: UserResponse, s: 'ACTIVE' | 'SUSPENDED' | 'DISABLED'): void {
    this.write.updateUserStatus(user.id, s).subscribe({ next: () => this.reload() });
  }
}
