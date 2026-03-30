# CLAUDE.md — Inventory System

## Stack

- Java 17
- Spring Boot 3.5.11
- Spring Data JPA + Hibernate
- PostgreSQL (via Docker, puerto 5433)
- Flyway (migraciones)
- Lombok
- Bean Validation (`@Valid`, `spring-boot-starter-validation`)
- Maven

## Base de datos
```bash
docker-compose up -d
```

PostgreSQL corre en el **puerto 5433** del host (mapeado al 5432 del contenedor).

Las credenciales de conexión se configuran en `application.properties` (no se commitea) o mediante variables de entorno. Ver `.env.example` en la raíz del proyecto.

## Comandos útiles
```bash
# Compilar sin tests
mvnw.cmd clean package -DskipTests

# Correr la app (requiere Docker y .env configurado)
mvnw.cmd spring-boot:run

# Iniciar base de datos
docker-compose up -d
```

Variables de entorno requeridas (ver `.env.example`):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — conexión PostgreSQL
- `JWT_SECRET`, `JWT_EXPIRATION_MS` — configuración JWT

---

## Idioma

- Las respuestas de Claude siempre en **español**.
- El código fuente (nombres de clases, métodos, variables, paquetes) y los comentarios en el código siempre en **inglés**.

---

## Reglas estrictas

Estas reglas no tienen excepciones:

1. **Nunca instanciar modelos de dominio con `new`** — usar siempre los factory methods (`create(...)` o `reconstitute(...)`). El constructor es privado por diseño.
2. **Nunca referenciar objetos de otro módulo** — las dependencias entre módulos se expresan únicamente mediante IDs primitivos (`Long`, `UUID`). Nunca pasar ni almacenar objetos de dominio de otro módulo.
3. **Nunca commitear `.env`** — el archivo `.env` está en `.gitignore`. Las credenciales se documentan en `.env.example` sin valores reales.
4. **Nunca saltarse el `GlobalExceptionHandler`** — todos los errores deben lanzarse como las excepciones tipadas del proyecto (`IllegalArgumentException`, `ResourceNotFoundException`, `InsufficientStockException`, etc.) y ser capturados por el handler global en `shared/`. No usar bloques `try/catch` en controllers para manejar errores de negocio.

---

## Frontend

Hay un frontend en desarrollo en un repositorio separado: **nicolerol28/inventory-system-frontend**.

- Stack: React + Vite + TailwindCSS
- Consume la API REST de este backend
- Al diseñar o modificar endpoints, considerar la compatibilidad con el frontend
