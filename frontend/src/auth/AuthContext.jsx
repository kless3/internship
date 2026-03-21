import { useEffect, useMemo, useState } from 'react';
import keycloak from './keycloak.js';

import { AuthContext } from './AuthContextValue.js';

export function AuthProvider({ children }) {
  const [initialized, setInitialized] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    let active = true;

    const init = async () => {
      try {
        const isAuthenticated = await keycloak.init({
          onLoad: 'check-sso',
          checkLoginIframe: false,
          silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
          pkceMethod: 'S256'
        });

        if (!active) {
          return;
        }

        setAuthenticated(isAuthenticated);
      } catch {
        if (active) {
          setAuthenticated(false);
        }
      } finally {
        if (active) {
          setInitialized(true);
        }
      }
    };

    keycloak.onAuthSuccess = () => {
      if (active) {
        setAuthenticated(true);
      }
    };

    keycloak.onAuthLogout = () => {
      if (active) {
        setAuthenticated(false);
      }
    };

    keycloak.onTokenExpired = async () => {
      try {
        await keycloak.updateToken(30);
      } catch {
        await keycloak.logout({ redirectUri: `${window.location.origin}/login` });
      }
    };

    init();

    return () => {
      active = false;
    };
  }, []);

  const value = useMemo(
    () => ({
      initialized,
      authenticated,
      token: keycloak.token,
      tokenParsed: keycloak.tokenParsed,
      login: () => keycloak.login({ redirectUri: `${window.location.origin}/orders` }),
      register: () =>
        keycloak.register({
          redirectUri: `${window.location.origin}/orders`
        }),
      loginWithGoogle: () =>
        keycloak.login({
          redirectUri: `${window.location.origin}/orders`,
          idpHint: 'google'
        }),
      logout: () =>
        keycloak.logout({
          redirectUri: `${window.location.origin}/login`
        }),
      refresh: (minValidity = 30) => keycloak.updateToken(minValidity)
    }),
    [initialized, authenticated]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
