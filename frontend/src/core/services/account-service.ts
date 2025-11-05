import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AccountFormCreds, LoginResponse, User } from '../../types/user';
import { map, switchMap } from 'rxjs';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;
  private readonly STORAGE_KEY = 'user';

  register(creds: AccountFormCreds) {
    return this.http.post<string>(this.baseUrl + 'auth/signup', creds, { responseType: 'text' as 'json' }).pipe(
      switchMap(() => this.login(creds))
    );
  }

  login(creds: AccountFormCreds) {
    return this.http.post<LoginResponse>(this.baseUrl + 'auth/login', creds).pipe(
      map(response => {
        const decoded = this.decodeJWT(response.token);
        if (!decoded?.username) {
          throw new Error('Invalid token: username not found');
        }
        const user: User = {
          username: decoded.username,
          token: response.token,
          expiresIn: response.expiresIn,
          expiryAt: Date.now() + response.expiresIn
        };
        this.setUser(user);
        return user;
      })
    );
  }

  updateUsername(newUsername: string) {
    return this.http.put(this.baseUrl + 'api/parent/username', null, { params: { newUsername } });
  }

  updatePassword(oldPassword: string, newPassword: string) {
    return this.http.put(this.baseUrl + 'api/parent/password', null, { params: { oldPassword, newPassword } });
  }

  deleteAccount() {
    return this.http.delete(this.baseUrl + 'api/parent');
  }

logout() {
  localStorage.removeItem(this.STORAGE_KEY);
}

  private setUser(user: User) {
  localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
}

getUser() {
  const user = localStorage.getItem(this.STORAGE_KEY);
  if (!user) return null;
  try {
    const parsed = JSON.parse(user) as User;
    if (Date.now() >= (parsed.expiryAt || 0)) {
      this.logout();
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

getCurrentUsername() {
  return this.getUser()?.username || null;
}

isAuthenticated() {
  return !!this.getUser();
}

  private decodeJWT(token: string) {
  try {
    const payload = token.split('.')[1];
    const decodedJson = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    const decoded = JSON.parse(decodedJson);
    return { username: decoded.sub };
  } catch {
    return null;
  }
}
}
