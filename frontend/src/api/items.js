import api from './axios.js';

export async function fetchItems(page, size) {
  const { data } = await api.get('/api/v1/items', { params: { page, size } });
  return data;
}

export async function fetchItemPriceHistory(itemId, months = 6) {
  const { data } = await api.get(`/api/v1/items/${itemId}/price/history`, { params: { months } });
  return Array.isArray(data) ? data : [];
}

export async function updateItemPrice(itemId, price) {
  const { data } = await api.put(`/api/v1/items/${itemId}/price`, { price });
  return data;
}
