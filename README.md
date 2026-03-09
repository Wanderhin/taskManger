#  TaskManager API

> API REST de gestion de tâches sécurisée — Spring Boot 3.2 · JWT · Docker · CI/CD

[![CI/CD](https://github.com/<ton-username>/taskmanager/actions/workflows/main.yml/badge.svg)](https://github.com/<ton-username>/taskmanager/actions)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=<SONAR_PROJECT_KEY>&metric=alert_status)](https://sonarcloud.io/project/overview?id=<SONAR_PROJECT_KEY>)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=<SONAR_PROJECT_KEY>&metric=coverage)](https://sonarcloud.io/project/overview?id=<SONAR_PROJECT_KEY>)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=<SONAR_PROJECT_KEY>&metric=security_rating)](https://sonarcloud.io/project/overview?id=<SONAR_PROJECT_KEY>)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)

---

##  Table des matières

- [Présentation](#-présentation)
- [Stack technique](#-stack-technique)
- [Architecture du projet](#-architecture-du-projet)
- [Endpoints API](#-endpoints-api)
- [Authentification JWT](#-authentification-jwt)
- [Lancer le projet](#-lancer-le-projet)
- [Variables d'environnement](#-variables-denvironnement)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Qualité & Sécurité](#-qualité--sécurité)

---

## 🎯 Présentation

**TaskManager** est une API REST permettant de gérer des tâches avec un système de rôles (`SIMPLE_USER` / `ADMIN`). Elle expose des endpoints sécurisés par JWT pour créer, lire, modifier, supprimer et changer le statut des tâches.

**Fonctionnalités clés :**
- Inscription & connexion avec génération de token JWT
- CRUD complet sur les tâches
- Gestion de statuts : `TODO` → `IN_PROGRESS` → `DONE` / `CANCELLED`
- Contrôle d'accès par rôle (`SIMPLE_USER` voit ses tâches, `ADMIN` voit tout)
- Pagination et filtrage par statut
- Documentation Swagger UI intégrée

---

## 🛠 Stack technique

| Couche | Technologie | Version |
|---|---|---|
| Runtime | Java | 17 |
| Framework | Spring Boot | 3.2.3 |
| Sécurité | Spring Security + JWT (jjwt) | 0.11.5 |
| Persistance | Spring Data JPA + Hibernate | — |
| Base de données | MySQL (prod) / H2 (tests) | — |
| Mapping | MapStruct | 1.5.5 |
| Boilerplate | Lombok | 1.18.30 |
| Documentation | SpringDoc OpenAPI (Swagger) | 2.3.0 |
| Build | Maven | 3.x |
| Conteneurisation | Docker + Docker Compose | — |
| CI/CD | GitHub Actions | — |
| Qualité code | SonarCloud | — |
| Sécurité deps | OWASP Dependency-Check | — |
| Scan image | Trivy | — |

---

## Architecture du projet

```
src/
└── main/java/com/deployfast/taskmanager/
    ├── controllers/          # Couche présentation (REST)
    │   ├── AuthController    # /api/v1/auth (register, login)
    │   └── TaskController    # /api/v1/tasks (CRUD + statuts)
    ├── services/
    │   ├── interfaces/       # Contrats de service
    │   └── implementations/  # Logique métier
    ├── entities/             # Entités JPA (User, Task, Role, TaskStatus)
    ├── dtos/                 # Objets de transfert (AuthDtos, TaskDtos)
    ├── repositories/         # Accès données (UserRepository, TaskRepository)
    ├── mappers/              # Conversions Entity ↔ DTO (MapStruct)
    ├── security/
    │   ├── config/           # Configuration Spring Security
    │   └── jwt/              # Filtre JWT + Service de génération
    └── exceptions/           # Gestion centralisée des erreurs
```

---

## 🔌 Endpoints API

### 🔐 Authentification — `/api/v1/auth`

| Méthode | Endpoint | Description | Auth requise |
|---|---|---|---|
| `POST` | `/register` | Créer un compte | ❌ |
| `POST` | `/login` | Connexion → retourne JWT | ❌ |

**Exemple register :**
```json
POST /api/v1/auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Exemple login :**
```json
POST /api/v1/auth/login
{
  "email": "john@example.com",
  "password": "securePassword123"
}

// Réponse :
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

###  Tâches — `/api/v1/tasks`

> Tous les endpoints nécessitent le header : `Authorization: Bearer <token>`

| Méthode | Endpoint | Description | Rôle |
|---|---|---|---|
| `POST` | `/` | Créer une tâche | USER, ADMIN |
| `GET` | `/` | Lister les tâches (paginées) | USER (ses tâches), ADMIN (toutes) |
| `GET` | `/{id}` | Récupérer une tâche | USER, ADMIN |
| `PUT` | `/{id}` | Modifier une tâche | USER, ADMIN |
| `DELETE` | `/{id}` | Supprimer une tâche | USER, ADMIN |
| `PATCH` | `/{id}/status/in-progress` | Passer en cours | USER, ADMIN |
| `PATCH` | `/{id}/status/done` | Terminer la tâche | USER, ADMIN |
| `PATCH` | `/{id}/status/cancelled` | Annuler la tâche | USER, ADMIN |

**Paramètres de pagination :**
```
GET /api/v1/tasks?page=0&size=10&status=IN_PROGRESS
```

---

##  Authentification JWT

Le token JWT doit être inclus dans le header de chaque requête protégée :

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Le token est retourné lors du `/login` et a une durée de validité configurable via `application.properties`.

---

## Lancer le projet

### Prérequis
- Java 17+
- Docker & Docker Compose
- Maven 3.x

### Avec Docker Compose (recommandé)

```bash
# Cloner le projet
git clone https://github.com/<ton-username>/taskmanager.git
cd taskmanager

# Lancer l'API + MySQL
docker-compose up -d

# L'API est disponible sur :
# http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui/index.html
```

### En local (sans Docker)

```bash
# 1. Configurer une base MySQL locale (voir Variables d'environnement)

# 2. Compiler et lancer
mvn clean package -DskipTests
java -jar target/taskmanager-1.0.0.jar

# 3. Ou directement avec Maven
mvn spring-boot:run
```

### Exécuter les tests

```bash
# Tests unitaires + rapport de couverture JaCoCo
mvn clean verify

# Rapport disponible dans :
# target/site/jacoco/index.html
```

---

## Variables d'environnement

Créer un fichier `.env` à la racine (ou configurer `application.properties`) :

```env
# Base de données
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/taskmanager
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your_very_long_secret_key_at_least_256_bits
JWT_EXPIRATION=86400000   # 24h en millisecondes

# Serveur
SERVER_PORT=8080
```

---

## 🔄 CI/CD Pipeline

Le pipeline GitHub Actions s'exécute automatiquement sur chaque push/PR sur `master` :

```
Push/PR ──► Tests + JaCoCo ──► SonarCloud ──► Security Rating Check
                                                        │
                                              OWASP Dependency-Check
                                                        │
                                               Build JAR + Docker
                                                        │
                                           Trivy Scan (CRITICAL/HIGH)
                                                        │
                                            Push → Docker Hub ✅
```

**Secrets GitHub requis :**

| Secret | Description |
|---|---|
| `SONAR_TOKEN` | Token d'accès SonarCloud |
| `SONAR_PROJECT_KEY` | Clé du projet SonarCloud |
| `SONAR_ORGANIZATION` | Slug de l'organisation SonarCloud |
| `DOCKER_USERNAME` | Nom d'utilisateur Docker Hub |
| `DOCKER_PASSWORD` | Token d'accès Docker Hub |

---

## 🛡 Qualité & Sécurité

| Outil | Rôle | Seuil de blocage |
|---|---|---|
| **SonarCloud** | Qualité du code, bugs, dette technique | Quality Gate + Security Rating ≥ B |
| **JaCoCo** | Couverture des tests | Configuré dans Quality Gate |
| **OWASP Dependency-Check** | Vulnérabilités des dépendances Maven | CVSS ≥ 7.0 |
| **Trivy** | Vulnérabilités de l'image Docker | CRITICAL ou HIGH |

Les rapports OWASP et Trivy sont disponibles dans les **artefacts GitHub Actions** (conservés 15 jours).

---

## 📖 Documentation API

Swagger UI disponible après démarrage :

```
http://localhost:8080/swagger-ui/index.html
```

---

## 👤 Auteur

**DeployFast** — `com.deployfast.taskmanager`

> Remplace `<ton-username>` et `<SONAR_PROJECT_KEY>` par tes vraies valeurs dans les badges en haut du fichier.
