import api from './axios.js';

export async function fetchCurrentUserOrders(page, size) {
  const { data } = await api.get('/api/v1/orders/current', { params: { page, size } });
  return data;
}

export async function createOrder(payload) {
  const { data } = await api.post('/api/v1/orders', payload);
  return data;
}

export async function payOrder(orderId) {
  const { data } = await api.post(`/api/v1/orders/${orderId}/payment`);
  return data;
}

export async function updateOrderShippingAddress(orderId, shippingAddress) {
  const { data } = await api.put(`/api/v1/orders/${orderId}/address`, { shippingAddress });
  return data;
}

export async function fetchOrderHistory(orderId) {
  const { data } = await api.get(`/api/v1/orders/${orderId}/timeline`);
  return Array.isArray(data) ? data : [];
}

export async function fetchOrderPriceAt(orderId, date) {
  const { data } = await api.get(`/api/v1/orders/${orderId}/price`, { params: { date } });
  return data;
}

export async function restoreOrder(orderId, date) {
  const { data } = await api.post(`/api/v1/orders/${orderId}/restoration`, null, {
    params: { date }
  });
  return data;
}

export async function applyDiscount(orderId, discountPercent) {
  const { data } = await api.put(`/api/v1/orders/${orderId}/discount`, { discountPercent });
  return data;
}

export async function removeDiscount(orderId) {
  const { data } = await api.delete(`/api/v1/orders/${orderId}/discount`);
  return data;
}
