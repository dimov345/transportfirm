export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  email: string;
  token: string;
}

export interface FirstLoginRequiredError {
  firstLoginRequired?: boolean;
  message?: string;
}
