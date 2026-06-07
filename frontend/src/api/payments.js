import api from './axios.js';

export async function fetchPaymentsByUser(userId, page, size) {
  const { data } = await api.get(`/api/v1/payments/user/${userId}`, { params: { page, size } });
  return data;
}

export async function fetchPaymentsByOrder(orderId) {
  const { data } = await api.get(`/api/v1/payments/order/${orderId}`);
  return Array.isArray(data) ? data : [];
}

export async function fetchPaymentReceipt(paymentId) {
  const { data } = await api.get(`/api/v1/payments/receipt/${paymentId}`, {
    responseType: 'blob',
    headers: {
      Accept: 'application/pdf'
    }
  });
  return data;
}
