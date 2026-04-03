export interface AdminDashboardSummary {
  usersCount: number;
  eventsCount: number;
  reservationsCount: number;
  revenue: number;
  paidReservationsCount: number;
  reservationsByStatus: Record<string, number>;
  ticketsByStatus: Record<string, number>;
  feedbacksByStatus: Record<string, number>;
  reclamationsByStatus: Record<string, number>;
}
