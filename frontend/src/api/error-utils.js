function asValidationMessage(data) {
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return null;
  }

  const entries = Object.entries(data).filter(([, value]) => typeof value === 'string');
  if (entries.length === 0) {
    return null;
  }

  return entries.map(([field, message]) => `${field}: ${message}`).join('; ');
}

const STATUS_MESSAGES = new Map([
  [400, 'Invalid request data.'],
  [401, 'Unauthorized. Please sign in again.'],
  [403, 'Access denied.'],
  [404, 'Resource not found.']
]);

export function normalizeApiError(error, fallback = 'Request failed.') {
  if (!error) {
    return fallback;
  }

  if (error.code === 'ECONNABORTED') {
    return 'Request timeout. Please try again.';
  }

  if (!error.response) {
    return 'Network error. Check backend availability and CORS settings.';
  }

  const { status, data } = error.response;

  if (typeof data === 'string' && data.trim()) {
    return data;
  }

  const explicitMessage =
    data?.message ||
    data?.detail ||
    data?.error_description ||
    data?.error;

  if (typeof explicitMessage === 'string' && explicitMessage.trim()) {
    return explicitMessage;
  }

  const validationMessage = asValidationMessage(data);
  if (validationMessage) {
    return validationMessage;
  }

  const messageByStatus = STATUS_MESSAGES.get(status);
  if (messageByStatus) return messageByStatus;
  if (status >= 500) return 'Server error. Please try later.';

  return fallback;
}
