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

## Arquitectura: Monolito Modular + Clean Architecture

El proyecto está dividido en módulos de negocio. Cada módulo sigue la misma estructura de capas:

```
{modulo}/
  domain/
    model/          ← modelos de dominio puro (sin anotaciones JPA/Spring)
    repository/     ← interfaces de repositorio (puertos de salida)
  application/
    usecase/        ← un caso de uso por acción
    command/        ← objetos de entrada para los use cases
    query/          ← servicios de consulta (queries)
  api/
    controller/     ← controladores REST (adaptador de entrada)
    dto/            ← request/response DTOs
    mapper/         ← mappers entre DTOs y comandos/domain
  infrastructure/
    entity/         ← entidades JPA
    mapper/         ← mappers entre domain y JPA entity
    repository/     ← implementaciones de repositorio (adaptadores de salida)
```

Módulos actuales: `products`, `suppliers`, `warehouses`, `inventory`, `users`

Código compartido en `shared/`: excepciones globales, `PageResponse`, `security/`.

### Reglas de dependencia

- `domain` no depende de ninguna capa externa.
- `application` solo depende de `domain`.
- `api` e `infrastructure` dependen de `application` y `domain`.
- Las dependencias entre módulos se expresan **solo mediante IDs** (e.g., `productId`, `warehouseId`), nunca mediante referencias a objetos de otro módulo.

---

## Decisiones arquitectónicas clave

### CQRS básico

Se aplica separación entre comandos y consultas dentro de cada módulo:

- **Comandos** (`UseCase`): operaciones de escritura. Pasan por el dominio, usan el repositorio del dominio (interfaz), aplican reglas de negocio.
- **Consultas** (`QueryService`): operaciones de solo lectura. Van **directamente a la capa JPA** (`JpaRepositorySpring`) sin pasar por el dominio. No tienen efecto secundario.

```
// Comando — pasa por dominio
RegisterStockMovementUseCase → StockRepository (interfaz dominio) → StockRepositoryImpl → JPA

// Consulta — va directo a JPA
StockQueryService → StockJpaRepositorySpring → proyección/DTO
```

No se usa CQRS con buses de eventos ni Event Sourcing. Es separación de responsabilidades dentro del mismo proceso.

### Entidades de dominio vs entidades JPA

- Las entidades de dominio son **clases Java puras**, sin anotaciones de Spring ni JPA.
- Las entidades JPA (`*JpaEntity`) son clases separadas con `@Entity`, `@Table`, etc.
- La conversión entre ambas la realizan los **mappers de infraestructura** (`*Mapper` en `infrastructure/mapper/`).
- Los use cases nunca ven entidades JPA. Los controladores nunca ven entidades de dominio directamente (usan DTOs).

### Paginación

Todas las consultas de listas usan paginación. El tipo de retorno es `PageResponse<T>`, definido en `shared/dto/PageResponse.java`. Los endpoints reciben parámetros `page` y `size` como query params con valores por defecto.

### Inmutabilidad y Factory Methods

Los modelos de dominio tienen **constructor privado** y se instancian exclusivamente mediante Factory Methods estáticos:

| Método | Uso |
|---|---|
| `create(...)` | Crear una entidad nueva. Ejecuta validaciones y asigna `createdAt`, `updatedAt`, `active = true`. |
| `reconstitute(...)` | Reconstruir desde la base de datos. Acepta todos los campos incluyendo `id`, sin validaciones de negocio. |

```java
// Correcto
Product product = Product.create(name, description, sku, unitId, categoryId, supplierId, purchasePrice, salePrice);
Product product = Product.reconstitute(id, name, ..., active, createdAt, updatedAt);

// Prohibido — constructor privado
Product p = new Product();
```

La lógica de negocio (validaciones, cambios de estado) vive **dentro del modelo de dominio**, no en los use cases ni en los mappers.

### Relaciones entre módulos

Los módulos no se referencian entre sí mediante objetos. Solo se usan IDs:

```java
// Correcto — solo ID
private final UUID warehouseId;

// Prohibido — referencia entre módulos
private final Warehouse warehouse;
```

Si un módulo necesita validar la existencia de un recurso de otro módulo, se define una **interfaz en el dominio del módulo que necesita** y se implementa en la infraestructura del módulo que provee los datos. Ejemplo: `StockChecker` en `warehouses/domain/`, implementado en `inventory/infrastructure/`.

### Validaciones

- Validaciones de **formato y presencia** de campos: en los DTOs con anotaciones Bean Validation (`@NotBlank`, `@NotNull`, etc.). El controller usa `@Valid`.
- Validaciones de **reglas de negocio** (unicidad, estado, consistencia): en el dominio o en el use case antes de persistir.
- Los errores se lanzan como `IllegalArgumentException` (400), `ResourceNotFoundException` (404), `InsufficientStockException` (422), o `Exception` genérica (500), todos manejados por `GlobalExceptionHandler` en `shared/`.

### Seguridad (módulo `users`)

Autenticación basada en **JWT + Spring Security**. El módulo `users` sigue la misma estructura de capas que los demás módulos, con una subcarpeta adicional en infraestructura:

```
users/
  infrastructure/
    security/     ← UserDetailsServiceImpl (implementa interfaz de Spring Security)
```

Los componentes transversales de seguridad viven en `shared/security/`:

```
shared/
  security/
    JwtService.java         ← genera y valida tokens JWT
    JwtAuthFilter.java      ← OncePerRequestFilter, intercepta cada request
    SecurityConfig.java     ← configuración de rutas públicas y protegidas
```

El dominio `User` no tiene dependencias de Spring Security. `UserDetailsServiceImpl` actúa como adaptador entre Spring Security y el dominio.

Los roles disponibles son `ADMIN` y `OPERATOR`, definidos en `users/domain/model/Role.java`.

---

## Convenciones de nomenclatura

- **Un use case por clase**, nombrado con el verbo de la acción: `RegisterProductUseCase`, `UpdateProductUseCase`, `DeactivateProductUseCase`.
- Los **comandos** son clases planas (puede ser record) que transportan los datos validados desde el controller al use case.
- Los **mappers de API** convierten DTOs → Commands y Domain → Response DTOs.
- Los **mappers de infraestructura** convierten Domain ↔ JPA Entity.
- Las entidades JPA llevan el sufijo `JpaEntity` (e.g., `ProductJpaEntity`).
- Los repositorios Spring Data llevan el sufijo `JpaRepositorySpring` (e.g., `ProductJpaRepositorySpring`).
- Las implementaciones del puerto llevan el sufijo `RepositoryImpl` (e.g., `ProductJpaRepositoryImpl`).

---

## Estándares Java 17

- Usar `Optional<T>` para campos opcionales en el dominio (e.g., `purchasePrice`, `salePrice`).
- Usar `LocalDateTime` para fechas/horas.
- Usar `BigDecimal` para cantidades y precios monetarios.
- Aprovechar records para objetos de valor simples si es necesario.
- No usar `null` donde `Optional` sea más expresivo.
- Los enums de dominio viven en `domain/model/` (e.g., `MovementType`, `Role`).

---

## Migraciones

Flyway gestiona el esquema. Los scripts viven en `src/main/resources/db/migration/` con nomenclatura `V{n}__{descripcion}.sql`. El script inicial es `V1__initial_schema.sql`.

Nunca modificar un script ya aplicado. Cambios posteriores al esquema van en scripts nuevos (`V2__...`, `V3__...`).