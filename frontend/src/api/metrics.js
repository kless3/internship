import api from './axios.js';

export async function fetchAverageDurationMetric(userId) {
  const { data } = await api.get(`/api/v1/metrics/customers/${userId}/averageDuration`);
  return data;
}

export async function fetchShippingAddressChangeFrequencyMetric(userId) {
  const { data } = await api.get(`/api/v1/metrics/customers/${userId}/shippingAddressChangeFrequency`);
  return data;
}
