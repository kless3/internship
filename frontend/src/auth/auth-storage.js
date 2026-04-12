const ACCESS_TOKEN_KEY = 'accessToken';
const LEGACY_REFRESH_TOKEN_KEY = 'refreshToken';
const AUTH_CHANGED_EVENT = 'auth-changed';

function isBrowser() {
  return typeof window !== 'undefined';
}

function emitAuthChanged() {
  if (isBrowser()) {
    window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
  }
}

function clearLegacyRefreshToken() {
  if (isBrowser()) {
    localStorage.removeItem(LEGACY_REFRESH_TOKEN_KEY);
  }
}

export function getAccessToken() {
  clearLegacyRefreshToken();
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAuthTokens({ accessToken }) {
  clearLegacyRefreshToken();
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  emitAuthChanged();
}

export function setAccessToken(accessToken) {
  clearLegacyRefreshToken();
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    emitAuthChanged();
  }
}

export function clearAuthTokens() {
  clearLegacyRefreshToken();
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  emitAuthChanged();
}

export function subscribeToAuthChanges(listener) {
  if (!isBrowser()) {
    return () => {};
  }

  window.addEventListener(AUTH_CHANGED_EVENT, listener);
  window.addEventListener('storage', listener);

  return () => {
    window.removeEventListener(AUTH_CHANGED_EVENT, listener);
    window.removeEventListener('storage', listener);
  };
}
