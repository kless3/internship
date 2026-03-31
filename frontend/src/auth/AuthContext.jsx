import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  clearAuthTokens,
  getAccessToken,
  setAccessToken as persistAccessToken,
  setAuthTokens,
  subscribeToAuthChanges
} from './auth-storage.js';
import { AuthContext } from './auth-context.js';
import { setUnauthorizedHandler } from '../api/axios.js';

function readAuthState() {
  return {
    accessToken: getAccessToken()
  };
}

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(readAuthState);

  useEffect(() => {
    const syncAuthState = () => setAuthState(readAuthState());

    return subscribeToAuthChanges(syncAuthState);
  }, []);

  const login = useCallback((accessToken) => {
    setAuthTokens({ accessToken });
    setAuthState({ accessToken });
  }, []);

  const logout = useCallback(() => {
    clearAuthTokens();
    setAuthState({ accessToken: null });
  }, []);

  const updateAccessToken = useCallback((accessToken) => {
    persistAccessToken(accessToken);
    setAuthState((prev) => ({ ...prev, accessToken }));
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  const value = useMemo(
    () => ({
      accessToken: authState.accessToken,
      isAuthenticated: Boolean(authState.accessToken),
      login,
      logout,
      updateAccessToken
    }),
    [authState.accessToken, login, logout, updateAccessToken]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
