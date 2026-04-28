import api from './axios.js';

export async function fetchPaymentsByUser(userId, page, size) {
  const { data } = await api.get(`/api/v1/payments/user/${userId}`, { params: { page, size } });
  return data;
}
