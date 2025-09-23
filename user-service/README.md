# User Service

A microservice for managing users and their bank cards, developed with Spring Boot using PostgreSQL and Redis.

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.9**
- **PostgreSQL 15** (database)
- **Redis 7** (caching)
- **Liquibase** (database migrations)
- **MapStruct** (DTO mapping)
- **Testcontainers** (integration testing)
- **Docker & Docker Compose** (containerization)

## Features

### User Management
- Create, read, update, delete users
- Find users by ID, email
- Check user existence

### Bank Card Management
- Create, read, update, delete cards
- Link cards to users
- Find cards by number, user ID
- Check card existence

## API endpoints
### Users (/api/users)

| Method | Endpoint | Описание                      |
|--------|----------|-------------------------------|
| GET    | `/api/users` | Get all users                 |
| GET    | `/api/users/{id}` | Get user by id                |
| GET    | `/api/users/email?email={email}` | Get user by email             |
| GET    | `/api/users/byIds?ids=1,2,3` | Get users by ids              |
| GET    | `/api/users/{id}/exists` | Check if the user exists      |
| GET    | `/api/users/email/{email}/exists` | Проверить существование email |
| POST   | `/api/users` | Create new user               |
| PUT    | `/api/users/{id}/upd` | Upadte user                   |
| DELETE | `/api/users/{id}` | Delete user                   |
### Bank Cards (/api/cards)

| Method | Endpoint | Описание                                |
|--------|----------|-----------------------------------------|
| GET    | `/api/cards` | Get all bank cards                      |
| GET    | `/api/cards/{id}` | Get bank card by id                     |
| GET    | `/api/cards/number?number={number}` | Get bank card by id                     |
| GET    | `/api/cards/byIds?ids=1,2,3` | Get bank cards by ids                   |
| GET    | `/api/cards/user/{userId}` | Get bank cards by user                  |
| GET    | `/api/cards/{id}/exists` | Check if the card exists by id          |
| GET    | `/api/cards/number/{number}/exists` | Check if the bank card exists by number |
| POST   | `/api/cards/user/{userId}` | Create bank card for user               |
| PUT    | `/api/cards/{id}` | Update bank card                        |
| DELETE | `/api/cards/{id}` | Delete bank card                        |

### Request Examples

```
User request body example:
{
  "name": "Ryhor",
  "surname": "Kisly",
  "birthDate": "2002-01-01",
  "email": "ryhor.kisly@innowise.com"
}

Bank Card request body example:
{
  "number": "2281111177711111",
  "holder": "RYHOR KISLY",
  "expirationDate": "2027-12-31"
}

