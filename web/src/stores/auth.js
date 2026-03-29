import { defineStore } from 'pinia';
import { api } from '../api';

const TOKEN_KEY = 'ce_token';
const USER_KEY = 'ce_user';

function parseJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return decoded;
  } catch (err) {
    return null;
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
    role: ''
  }),
  actions: {
    async login(account, password) {
      const res = await api.login({ account, password });
      this.token = res.data.token;
      this.user = res.data.user;
      this.role = res.data.user?.role || parseJwt(this.token)?.role || '';
      localStorage.setItem(TOKEN_KEY, this.token);
      localStorage.setItem(USER_KEY, JSON.stringify(this.user));
    },
    async register(payload) {
      await api.register(payload);
    },
    async refresh() {
      const res = await api.refresh();
      this.token = res.data.token;
      localStorage.setItem(TOKEN_KEY, this.token);
    },
    logout() {
      this.token = '';
      this.user = null;
      this.role = '';
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },
    restore() {
      const payload = parseJwt(this.token);
      if (payload?.role) {
        this.role = payload.role;
      }
    }
  }
});
