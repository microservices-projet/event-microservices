export interface Feedback {
  id: number;
  eventId: number;
  userId: number;
  rating: number;
  comment: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'FLAGGED';
  moderatedBy?: number;
  moderationNote?: string;
  flaggedReason?: string;
  createdAt: string;
  updatedAt?: string;
  username?: string;
  eventTitle?: string;
}

export interface FeedbackRequest {
  eventId: number;
  userId: number;
  rating: number;
  comment: string;
}

export interface FeedbackStats {
  eventId: number;
  averageRating: number;
  totalFeedbacks: number;
  ratingDistribution: { [key: number]: number };
}
