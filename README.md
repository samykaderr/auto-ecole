# Auto-École — Logiciel de gestion

Application web (+ espace élève PWA) qui remplace le cahier, Excel et WhatsApp pour gérer :
planning moniteurs/véhicules, heures de conduite, examens et paiements par tranches.

> Doc de cadrage : [`docs/01-auto-ecole.md`](docs/01-auto-ecole.md)

## Structure

```
auto-ecole/
├─ autoecole-api/     Backend Spring Boot 3 (monolithe modulaire)
│  └─ src/main/java/com/autoecole/
│     ├─ common/      (tenant context, exceptions RFC 7807, config)
│     ├─ security/    (JWT access 15 min + refresh, JwtAuthFilter, BCrypt)
│     ├─ auth/        (login / refresh / logout)
│     ├─ user/        (app_user, rôles)
│     ├─ school/      (tenant)
│     ├─ student/     (CRUD élèves, premier module bout-en-bout)
│     └─ AutoEcoleApiApplication
│  └─ src/main/resources/db/migration/  (Flyway : V1__init.sql, V2__reference_data.sql)
├─ docker-compose.yml (Postgres 16 + api [+ MinIO : --profile files])
├─ docs/              (cadrage + décision)
└─ .github/workflows  (CI : build + tests backend)
```

## Lancer en local

```bash
# 1. Postgres (compose) — nécessite Docker
docker compose up -d db

# 2. API (Java 21+, Maven wrapper inclus)
cd autoecole-api
./mvnw spring-boot:run        # Windows : mvnw.cmd spring-boot:run
```

- Swagger : http://localhost:8080/swagger-ui.html
- Santé : http://localhost:8080/actuator/health
- Compte de démo (seed profil `dev`) : `admin@autoecole.dev` / `owner1234`

Tout-en-un avec Docker :

```bash
docker compose up --build            # db + api
docker compose --profile files up    # + MinIO (documents élèves)
```

## Stack (MVP)

| Couche | Choix |
|---|---|
| Backend | Java 21 + Spring Boot 3 (monolithe modulaire, API REST) |
| Base de données | PostgreSQL 16 + Flyway |
| Auth | Spring Security + JWT (access 15 min + refresh) |
| Frontend | React + TypeScript + Vite (shadcn/ui + Tailwind, FullCalendar, TanStack Query) |
| Fichiers | MinIO / S3 |
| Tests | JUnit 5 + Testcontainers, Vitest + Playwright |
| Infra | Docker + Docker Compose, GitHub Actions, Nginx + Let's Encrypt sur VPS |

## Modules backend

`common` · `auth` · `school` · `student` · `instructor` · `vehicle` · `scheduling` · `billing` · `exam` · `notification` · `reporting`

## Règles métier clés

1. **Zéro chevauchement** de leçon garanti en base (`EXCLUDE USING gist`) → 409 Conflict côté API
2. Le moniteur doit être disponible et habilité pour la catégorie du véhicule
3. Leçon `EFFECTUEE` (et annulation tardive élève, selon paramètre) décompte les heures du forfait
4. Total des tranches = prix du forfait ; paiement jamais supprimé (annulation + motif)
5. Multi-tenant par `school_id` injecté depuis le JWT, jamais depuis le client
6. Journal d'audit sur toute modification sensible

## Roadmap (MVP ≈ 11–16 semaines, développeur seul)

- [ ] **0. Cadrage** — interviews gérants, maquettes, validation du périmètre
- [x] **1. Fondations** — repo ✅, structure monorepo, Flyway (`V1__init.sql`), auth JWT + rôles, multi-tenant (`school_id` injecté depuis le JWT), CRUD élèves de bout en bout. *Reste : valider Docker (pas d'engine local), ajouter les tests d'intégration Testcontainers (anti-chevauchement).*
- [ ] **2. Référentiels** — élèves ✅, moniteurs, véhicules, documents
- [ ] **3. Planning** — calendrier, conflits, disponibilités, statuts
- [ ] **4. Facturation** — forfaits, échéancier, paiements, reçus PDF, caisse
- [ ] **5. Examens + dashboard**
- [ ] **6. Notifications** — emails, jobs de relance, alertes
- [ ] **7. Pilote** — déploiement chez 1 auto-école test
