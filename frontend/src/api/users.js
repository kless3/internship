import api from './axios.js';

export async function fetchUsers() {
  const { data } = await api.get('/api/v1/users');
  return Array.isArray(data) ? data : [];
}

export async function fetchUserByEmail(email) {
  const { data } = await api.get(`/api/v1/users/email/${encodeURIComponent(email)}`);
  return data;
}

export async function createUser(payload) {
  const { data } = await api.post('/api/v1/users', payload);
  return data;
}
