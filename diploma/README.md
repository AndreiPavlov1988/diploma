# Diploma — платформа перепродажи вещей (Ads-Online)

Дипломный проект курса SkyPro «Java-разработчик».  
Backend-сервис для платформы объявлений: пользователи, объявления, комментарии, изображения.

## 🛠 Технологический стек

- **Java 17**, Spring Boot 3.2
- Spring Web, Spring Security (Basic Auth, BCrypt)
- Spring Data JPA (Hibernate), PostgreSQL 15 (Docker)
- **Liquibase** — миграции схемы БД (`ddl-auto=none`)
- MapStruct (DTO ↔ Entity), Lombok, Bean Validation
- SpringDoc OpenAPI / Swagger UI
- Тесты: JUnit 5, Mockito, MockMvc, spring-security-test

## ✨ Реализованный функционал

- Регистрация и вход (пароли хранятся в виде BCrypt-хэшей)
- Роли **USER** / **ADMIN**: администратор управляет любыми ресурсами, пользователь — только своими
- CRUD объявлений с загрузкой изображений (файлы на диске, в БД — путь)
- Комментарии к объявлениям
- **Soft-delete** (`is_active`) с фильтрацией удалённых записей во всех списках
- Публичный просмотр объявлений без авторизации
- Единая обработка ошибок с корректными HTTP-кодами (400/401/403/404/409/500)
- Интеграция с фронтендом (CORS, маршруты `/login` и `/register`)

## 🚀 Запуск локально

### 1. База данных (Docker)

```bash
docker run --name diploma-postgres \
  -e POSTGRES_DB=diploma_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 -d postgres:15
```

> ⚠️ Порт в `application.properties` (`spring.datasource.url`) должен совпадать с портом контейнера. По умолчанию используется **5433**.

### 2. Backend

Запуск из IDEA (▶ Run) или:

```bash
./mvnw spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Frontend (Docker)

```bash
docker run -p 3000:3000 --rm ghcr.io/dmitry-bizin/front-react-avito:v1.21
```

- UI: http://localhost:3000

## 🧪 Тесты

```bash
./mvnw test
```

Покрыто:
- Регистрация и конфликты email (409)
- Разграничение ролей (USER / ADMIN)
- Запрет изменения/удаления чужих данных (403)
- Фильтрация soft-deleted записей
- Публичные маршруты (`/ads`, `/ads/{id}`)
- Неверный текущий пароль при смене пароля (400, не 500)

## 📁 Структура проекта

```
src/main/java/com/skypro/diploma/
├── controller/   — REST-контроллеры (тонкие, только HTTP)
├── service/      — бизнес-логика и проверка прав
├── repository/   — Spring Data JPA
├── entity/       — JPA-сущности
├── dto/          — DTO запросов/ответов
├── mapper/       — MapStruct мапперы
├── security/     — SecurityConfig, UserDetailsServiceImpl
└── exception/    — кастомные исключения + GlobalExceptionHandler

src/main/resources/db/changelog/ — Liquibase-миграции (users, ads, comments)
openapi.yaml — спецификация API
```

## 🔐 Модель безопасности

- **Basic Auth**, stateless (без сессий)
- Публично: `GET /ads`, `GET /ads/{id}`, `GET /images/**`, `/login`, `/register`, Swagger
- Остальное — только для авторизованных
- Изменение/удаление — только **автор** или **ADMIN**
- Пароли хранятся в виде **BCrypt-хэшей**, открытый пароль никогда не сохраняется

## 🗂 Эндпоинты (кратко)

| Метод | URL | Доступ |
|---|---|---|
| POST | `/login`, `/register` | Публичный |
| GET | `/ads`, `/ads/{id}`, `/images/**` | Публичный |
| GET | `/ads/me` | Авторизован |
| POST/PATCH/DELETE | `/ads/**` | Автор + ADMIN |
| GET/POST/PATCH/DELETE | `/ads/*/comments/**` | Авторизован (автор + ADMIN для PATCH/DELETE) |
| GET/PATCH/POST | `/users/me/**` | Авторизован |

## 📝 Примечания к реализации

- Миграции **Liquibase** описывают схему БД, Hibernate `ddl-auto=none`
- Изображения хранятся **на диске** в папке `images/{type}/{id}/`, в БД только путь
- Soft-delete используется для сохранения целостности (комментарии и связи)
- Контроллеры «тонкие» — вся бизнес-логика в сервисах
- Глобальный обработчик исключений `GlobalExceptionHandler` превращает исключения в правильные HTTP-коды