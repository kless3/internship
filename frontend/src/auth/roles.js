export function hasRealmRole(tokenParsed, role) {
  const normalizedRole = String(role ?? '').toLowerCase();
  if (!normalizedRole) {
    return false;
  }

  const roles = tokenParsed?.realm_access?.roles;
  if (!Array.isArray(roles)) {
    return false;
  }

  return roles.some((candidateRole) => String(candidateRole).toLowerCase() === normalizedRole);
}

export function isAdminUser(tokenParsed) {
  return hasRealmRole(tokenParsed, 'admin');
}
