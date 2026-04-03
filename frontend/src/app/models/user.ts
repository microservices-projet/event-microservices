export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN' | 'MODERATOR';
  status: 'ACTIVE' | 'SUSPENDED' | 'DISABLED';
  createdAt: string;
  /** Set after local register/login — send as Authorization Bearer via the gateway */
  accessToken?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
