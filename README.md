# Auto-École — Logiciel de gestion

Application web (+ espace élève PWA) qui remplace le cahier, Excel et WhatsApp pour gérer :
planning moniteurs/véhicules, heures de conduite, examens et paiements par tranches.

> Doc de cadrage : `C:\Users\user\Downloads\01-auto-ecole.md` (à copier dans `docs/`)

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
- [ ] **1. Fondations** — repo ✅, Docker, CI, Flyway, auth, multi-tenant, rôles
- [ ] **2. Référentiels** — élèves, moniteurs, véhicules, documents
- [ ] **3. Planning** — calendrier, conflits, disponibilités, statuts
- [ ] **4. Facturation** — forfaits, échéancier, paiements, reçus PDF, caisse
- [ ] **5. Examens + dashboard**
- [ ] **6. Notifications** — emails, jobs de relance, alertes
- [ ] **7. Pilote** — déploiement chez 1 auto-école test
