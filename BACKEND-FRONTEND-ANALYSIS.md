# Backend ↔ Frontend – Ce qui fonctionne / ce qui ne fonctionne pas

## 1. Ce qui fonctionne

### 1.1 Routage et URLs

| Élément | Détail |
|--------|--------|
| **Gateway** | Spring Cloud Gateway avec discovery locator **enlève** le préfixe service par défaut (`/user-service/api/users` → `/api/users` en aval). |
| **Frontend → API** | `ApiService` construit les URLs : `/user-service`, `/event-service`, `/ticket-service` + `/api/users`, `/api/events`, `/api/tickets`. En dev, le proxy envoie tout vers le gateway (port 8080). |
| **Backend paths** | User: `@RequestMapping("/api/users")`, Event: `@RequestMapping("/api/events")`, Ticket: `@RequestMapping("/api/tickets")` → cohérents avec le frontend. |

### 1.2 Sécurité

| Élément | Détail |
|--------|--------|
| **Auth** | Keycloak (OIDC, code flow). Frontend : `AuthService` + `initCodeFlow()` / `loadDiscoveryAndTryLogin()`. |
| **JWT** | `JwtInterceptor` ajoute `Authorization: Bearer <token>` à **toutes** les requêtes HTTP. |
| **Backend** | User, Event, Ticket + Gateway (en profil non-`noauth`) : OAuth2 Resource Server (JWT). Le gateway en `noauth` transmet le header tel quel vers les microservices. |

### 1.3 Modèles et endpoints

- **User** : `UserRequest` / `UserResponse` (username, email, password / id, username, email, role, status, createdAt) alignés backend/frontend. Enums `Role` et `Status` identiques.
- **Event** : Champs (title, description, date, place, price, organizerId, imageUrl, nbPlaces, nbLikes, domaines, status, etc.) et verbes HTTP (GET list/id/search/organizer, POST, PUT, DELETE, POST like) cohérents.
- **Ticket** : `idTicket`, `eventId`, `userId`, `nomClient`, `emailClient`, `prix`, `statut`, `typeTicket`, etc. Endpoints (GET list/id/event/user, POST event/{id}, PUT id/statut, PUT id/type, DELETE) alignés. Paramètres `statut` et `typeTicket` en query OK.

### 1.4 Proxy et CORS

- **proxy.conf.json** : `/user-service`, `/event-service`, `/ticket-service` → `http://localhost:8080`. Pas de CORS côté navigateur car les appels sont same-origin (4200).

---

## 2. Ce qui ne fonctionne pas ou est fragile

### 2.1 Utilisateurs : double source (Keycloak vs user-service)

| Problème | Détail |
|----------|--------|
| **Inscription** | L’inscription se fait **uniquement** dans Keycloak (lien “S’inscrire” → `prompt=create`). Aucun appel à `POST /api/users`. |
| **Conséquence** | Les utilisateurs Keycloak **n’existent pas** dans user-service (H2). La liste “Users” (GET /api/users) affiche seulement les utilisateurs créés via user-service, pas ceux inscrits via Keycloak. |
| **Recommandation** | Après login Keycloak (callback), appeler un endpoint “sync” ou `POST /api/users` avec les infos du token (sub, email, username) pour créer/mettre à jour l’utilisateur dans user-service, ou faire en sorte que user-service soit la seule source après sync. |

### 2.2 Achat de ticket : `userId` incorrect

| Fichier | Problème |
|---------|----------|
| **Frontend** `ticket-buy.component.ts` | `userId: this.auth.userInfo?.sub ? 1 : undefined` → **toujours 1** si connecté. Keycloak `sub` est un **UUID** (string), pas un entier. |
| **Backend** | user-service et ticket-service utilisent un **Long id** pour les utilisateurs. |
| **Conséquence** | Le ticket est enregistré avec `userId = 1` au lieu de l’id réel de l’utilisateur dans user-service (ou pas de lien si l’utilisateur n’existe pas en base). |
| **Recommandation** | Soit ne pas envoyer `userId` (laisser le backend à null si autorisé), soit récupérer l’id user-service correspondant au `sub` Keycloak (ex. endpoint GET /api/users/by-sub/{sub} ou sync au login) et envoyer cet id. |

### 2.3 Réponses d’erreur non standard

| Endpoint | Comportement backend | Risque frontend |
|----------|----------------------|------------------|
| **GET /api/tickets/event/{eventId}** | Si l’événement n’existe pas : `400` + **body string** (“Événement inexistant avec l’ID: …”). | Le frontend peut s’attendre à du JSON (`err.error`). `err.error?.message` peut être vide si le body est une string. |
| **Recommandation** | Retourner un objet JSON (ex. `{ "message": "..." }`) ou un `ProblemDetail` pour que le frontend affiche toujours un message propre. |

### 2.4 Event : format de date

- **Backend** : `LocalDateTime` (Event, EventRequest). Jackson sérialise souvent en ISO-8601 ou tableau selon la config.
- **Frontend** : `date: string`, `createdAt: string`, `updatedAt?: string`.
- **Risque** | Si le backend renvoie un tableau de nombres (ancienne config Jackson), le frontend peut casser. À vérifier en dev (Network + affichage des événements). Si tout s’affiche correctement, c’est OK ; sinon, configurer Jackson pour `LocalDateTime` en ISO-8601.

### 2.5 Suppression d’événement (DELETE)

- **Backend** : `EventController.archive()` retourne **void** → HTTP **200** avec body vide.
- **Usage courant** : **204 No Content** pour un DELETE réussi.
- **Impact** | Faible : le frontend gère `Observable<void>`. Pour une API plus standard, préférer `ResponseEntity.noContent()` (204).

---

## 3. Synthèse des actions recommandées

| Priorité | Action |
|----------|--------|
| **Haute** | Corriger `userId` dans l’achat de ticket (ne pas envoyer 1 en dur ; utiliser l’id user-service ou pas d’userId selon le métier). |
| **Haute** | Décider de la stratégie utilisateurs : sync Keycloak → user-service au login (ou à l’inscription) pour que la liste Users et les tickets soient cohérents. |
| **Moyenne** | Retourner des erreurs en JSON pour GET tickets/event/{id} (et autres endpoints susceptibles de renvoyer une string en 4xx). |
| **Basse** | DELETE event : renvoyer 204 No Content. Vérifier le format des dates Event (ISO-8601) si problème d’affichage. |

---

## 4. Vérification rapide (checklist)

- [ ] Docker + Keycloak (hôte **8180** → conteneur 8080) + MySQL + RabbitMQ + Kafka (si utilisés) démarrés.
- [ ] Eureka (8761), Gateway (8080), user-service, event-service, ticket-service démarrés.
- [ ] Keycloak : client `event-web`, redirect URI `http://localhost:4200`, inscription activée (realm).
- [ ] Frontend : `npm start` (avec proxy) → `http://localhost:4200`.
- [ ] Login Keycloak → redirection OK, token reçu, appels API avec Bearer.
- [ ] Liste événements, détail, création/modif/suppression OK.
- [ ] Liste tickets, achat ticket (après correction userId) OK.
- [ ] Liste users : actuellement limitée aux utilisateurs user-service (H2), pas aux seuls Keycloak.

Ce document peut être mis à jour au fur et à mesure des corrections (sync user, userId ticket, erreurs JSON, 204 DELETE).
