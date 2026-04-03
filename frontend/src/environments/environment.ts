/**
 * Development defaults align with docker-compose: Keycloak host port 8180, API gateway 8080.
 * For Keycloak on host 8080, set `keycloakUrl` to `http://localhost:8080` (e.g. replace in this file or use build-time replacement later).
 */
export const environment = {
  production: false,
  keycloakUrl: 'http://localhost:8180',
  /** Browser calls APIs via Angular dev-server proxy to this target */
  apiGatewayTarget: 'http://localhost:8080',
};
