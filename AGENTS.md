# AGENTS.md — Inventory System

## Stack

- Java 17
- Spring Boot 3.5.11
- Spring Data JPA + Hibernate
- PostgreSQL (via Docker, puerto 5433)
- Flyway (migraciones)
- Lombok
- Bean Validation (`@Valid`, `spring-boot-starter-validation`)
- Spring Security + JWT (jjwt 0.12.6) + Google OAuth
- springdoc-openapi 2.8.9
- AWS SDK S3 (cliente para Cloudflare R2 — imágenes de productos)
- JaCoCo 0.8.11 (cobertura)
- Maven

## Base de datos

```bash
docker-compose up -d
```

PostgreSQL corre en el **puerto 5433** del host (mapeado al 5432 del contenedor).

Las credenciales se configuran en `application.properties` (no se commitea) o variables de entorno. Ver `.env.example`.

## Comandos útiles

```bash
# Compilar sin tests
mvnw.cmd clean package -DskipTests

# Correr la app (requiere Docker y .env configurado)
mvnw.cmd spring-boot:run

# Ejecutar tests + reporte JaCoCo
mvnw.cmd test
# Reporte: target/site/jacoco/index.html

# Iniciar base de datos
docker-compose up -d
```

Variables de entorno requeridas (ver `.env.example`):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`

---

## Idioma

- Respuestas de Claude siempre en **español**.
- Código fuente (clases, métodos, variables, paquetes) y comentarios en el código siempre en **inglés**.

---

## Arquitectura: Monolito Modular + Clean Architecture + CQRS básico

### Módulos de negocio

```
src/main/java/com/miapp/inventory_system/
├── products/       ← productos, categorías, unidades de medida
├── warehouse/      ← almacenes
├── inventory/      ← movimientos de stock (entradas/salidas)
├── suppliers/      ← proveedores
├── users/          ← autenticación y roles (JWT + Google OAuth)
├── assistant/      ← asistente IA (Gemini) con rate-limit y prompt injection guard
└── shared/         ← código transversal (ver sección dedicada abajo)
```

### Estructura de capas por módulo

Todos los módulos siguen exactamente esta estructura:

```
{modulo}/
  domain/
    model/          ← clases Java puras, sin anotaciones JPA/Spring
    repository/     ← interfaces de repositorio (puertos de salida)
  application/
    usecase/        ← un use case por acción (RegisterXUseCase, UpdateXUseCase, DeactivateXUseCase)
    command/        ← objetos de entrada para comandos (records o clases planas)
    query/          ← QueryService de solo lectura, va directo a JPA sin pasar por dominio
  api/
    controller/     ← controladores REST (@RestController)
    dto/            ← request/response DTOs (con @Valid en requests)
    mapper/         ← DTOs → Commands y Domain → Response DTOs
  infrastructure/
    entity/         ← entidades JPA ({Nombre}JpaEntity)
    mapper/         ← Domain ↔ JPA Entity
    repository/     ← implementaciones de los puertos ({Nombre}RepositoryImpl) y Spring Data ({Nombre}JpaRepositorySpring)
```

El módulo `assistant` agrega `application/port/` para las interfaces hacia el proveedor de IA.

### CQRS básico (separación Comandos / Consultas)

```
// Comando — pasa por dominio, valida reglas de negocio
RegisterProductUseCase → ProductRepository (interfaz dominio) → ProductRepositoryImpl → JPA

// Consulta — va directo a JPA, sin dominio, sin efectos secundarios
ProductQueryService → ProductJpaRepositorySpring → DTO/proyección
```

No se usan buses de eventos ni Event Sourcing. Es separación de responsabilidades dentro del mismo proceso.

### Reglas de dependencia entre capas

- `domain` no depende de ninguna capa externa.
- `application` solo depende de `domain`.
- `api` e `infrastructure` dependen de `application` y `domain`.
- Dependencias entre módulos: **solo mediante IDs primitivos** (`Long`, `UUID`). Nunca objetos de dominio de otro módulo.
- Si un módulo necesita validar existencia en otro módulo, define una interfaz en su propio `domain/` e implementa en `infrastructure/` del módulo proveedor. Ejemplo: `StockChecker` en `warehouses/domain/`.

### Módulo `shared/` — componentes transversales

```
shared/
  config/
    AppConfig.java              ← beans generales (cliente R2/S3, etc.)
    OpenApiConfig.java          ← configuración springdoc / Swagger UI
  dto/
    PageResponse.java           ← record genérico de paginación
  exception/
    GlobalExceptionHandler.java ← @RestControllerAdvice central
    ErrorResponse.java          ← estructura del body de error
    ResourceNotFoundException.java
    InsufficientStockException.java
  gateway/
    StorageGateway.java         ← interfaz para subir archivos (uploadFile)
  guard/
    AssistantGuard.java         ← rate-limit (10 req/min) + detección de prompt injection
  infrastructure/storage/
    R2StorageClient.java        ← implementa StorageGateway sobre Cloudflare R2
  job/
    DemoResetJob.java           ← reset diario de BD demo a las 07:40 AM
  security/
    JwtService.java             ← genera y valida tokens JWT
    JwtAuthFilter.java          ← OncePerRequestFilter por request
    SecurityConfig.java         ← rutas públicas y protegidas
```

> El storage usa **Cloudflare R2** (API compatible con S3). El SDK de AWS S3 en `pom.xml` es solo el cliente HTTP.

### Paginación — `PageResponse<T>`

Todas las consultas de listas devuelven `PageResponse<T>`:

```java
public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {}
```

Los endpoints reciben `page` y `size` como query params con valores por defecto. Los `QueryService` construyen el `PageResponse` a partir del `Page<T>` de Spring Data.

### Catálogo de excepciones y códigos HTTP

| Excepción | HTTP | Cuándo lanzarla |
|-----------|------|-----------------|
| `MethodArgumentNotValidException` | 400 | Automática — Bean Validation en DTO con `@Valid` |
| `IllegalArgumentException` | 400 | Regla de negocio violada (duplicado, estado inválido, formato incorrecto) |
| `ResourceNotFoundException` | 404 | Entidad no encontrada por ID |
| `InsufficientStockException` | 422 | Stock insuficiente para una salida de inventario |
| `AuthenticationException` | 401 | Token inválido o ausente (lanzada por Spring Security) |
| `Exception` (genérica) | 500 | Error inesperado no manejado explícitamente |

**Regla:** nunca usar `try/catch` en controllers para convertir excepciones en respuestas HTTP. El `GlobalExceptionHandler` lo hace automáticamente.

---

## Reglas estrictas

Sin excepciones:

1. **Nunca instanciar modelos de dominio con `new`** — usar siempre `create(...)` o `reconstitute(...)`. El constructor es privado por diseño.
2. **Nunca referenciar objetos de otro módulo** — solo IDs primitivos (`Long`, `UUID`).
3. **Nunca commitear `.env`** — documentar credenciales en `.env.example` sin valores reales.
4. **Nunca saltarse el `GlobalExceptionHandler`** — lanzar las excepciones tipadas del proyecto y dejar que el handler global las capture. Sin `try/catch` en controllers para errores de negocio.
5. **`@Transactional` solo en la capa `application`** — obligatorio en cada write use case (`*UseCase`), a nivel de clase o en el método `execute(...)`. Nunca en controllers, domain models ni mappers.
6. **La lógica de negocio vive en el dominio** — validaciones, cambios de estado e invariantes van dentro del modelo de dominio. Los use cases solo orquestan: llaman métodos del dominio, persisten, devuelven resultado. Los mappers son transformación pura de datos, sin lógica.

---

## Convenciones críticas

### Factory Methods: `create` y `reconstitute`

```java
// create — entidad nueva: valida, asigna createdAt/updatedAt, active=true
Product product = Product.create(name, description, sku, unitId, categoryId,
                                  supplierId, purchasePrice, salePrice);

// reconstitute — rehidratación desde BD: acepta todos los campos incluyendo id, sin validaciones
Product product = Product.reconstitute(id, name, description, sku, unitId,
                                        categoryId, supplierId, purchasePrice,
                                        salePrice, active, createdAt, updatedAt);

// PROHIBIDO — constructor privado
Product p = new Product();
```

### Precios: `purchase_price` y `sale_price`

Los precios son opcionales. Se representan siempre como `Optional<BigDecimal>`:

```java
private Optional<BigDecimal> purchasePrice;  // precio de compra
private Optional<BigDecimal> salePrice;       // precio de venta
```

En BD: columnas `purchase_price` y `sale_price` (nullable). En DTOs: `BigDecimal` nullable.  
Nunca llamarlos `price` o `unit_price`. Nunca usar `double` ni `float` para valores monetarios.

### Nomenclatura de clases

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Use Case | `{Verbo}{Entidad}UseCase` | `RegisterProductUseCase` |
| Command | `{Verbo}{Entidad}Command` | `RegisterProductCommand` |
| Query Service | `{Entidad}QueryService` | `ProductQueryService` |
| JPA Entity | `{Entidad}JpaEntity` | `ProductJpaEntity` |
| Spring Data Repo | `{Entidad}JpaRepositorySpring` | `ProductJpaRepositorySpring` |
| Port Impl | `{Entidad}RepositoryImpl` | `ProductRepositoryImpl` |

### DemoResetJob

`shared/job/DemoResetJob.java` resetea la BD demo diariamente a las **07:40 AM**.  
Borra todos los datos, reinicia secuencias y re-ejecuta los scripts Flyway de seed (V2–V8).  
**No modificar sin entender el impacto en el entorno demo.**

### AssistantGuard

`shared/guard/AssistantGuard.java` protege el módulo `assistant` con:
- **Rate limiting:** máximo 10 requests/minuto por IP (ventana deslizante en memoria con `ConcurrentHashMap`)
- **Detección de prompt injection:** lista `INJECTION_PATTERNS` con patrones como `"ignore previous"`, `"jailbreak"`, `"DAN"`, etc.

Lanza `IllegalArgumentException` (→ 400) ante injection detectada, e `IllegalStateException` ante rate limit.  
Para agregar nuevos patrones, añadirlos a `INJECTION_PATTERNS` dentro de esta clase.

### Módulo `assistant` y `GeminiGateway`

Módulo de asistente IA que usa Google Gemini como proveedor. Tiene `application/port/` adicional:

```
assistant/
  api/controller/       ← endpoint REST del chat
  api/dto/              ← request/response del asistente
  api/mapper/
  application/
    command/            ← comando de mensaje
    port/               ← interfaces hacia el proveedor IA (GeminiGateway)
    query/
    usecase/
  infrastructure/       ← implementación de GeminiGateway
```

En tests, `GeminiGateway` se mockea igual que cualquier otro puerto: `@Mock GeminiGateway geminiGateway`.

### Deuda técnica conocida

El módulo `users` tiene violaciones arquitectónicas menores documentadas como deuda técnica intencional. Al escribir tests para `users`, hacer que coincidan con la implementación real, no con una versión idealizada.

---

## Testing Standards

### Mínimo obligatorio: 90% de cobertura

- Herramientas: **JUnit 5 + Mockito + AssertJ + JaCoCo**
- Metodología: **BDD — Given / When / Then**
- Ubicación: espejo exacto de `src/main/java` en `src/test/java`
- Todas las dependencias ya están en `pom.xml` vía `spring-boot-starter-test` — no agregar JUnit, Mockito ni AssertJ por separado

### Estado actual de cobertura por módulo

| Módulo | Estado | Cobertura |
|--------|--------|-----------|
| `products` | ✅ Completo | 100% |
| `warehouse` | ✅ Completo | 100% |
| `inventory` | ✅ Completo | 100% |
| `suppliers` | ✅ Completo | 100% |
| `users` | ✅ Completo | 100% |
| `assistant` | ✅ Completo | 100% |
| `shared.guard` | ✅ Completo | 97% |
| `shared.exception` | ✅ Completo | cubierto |
| **Total** | ✅ | **94% instructions / 96% branches** |

### Tests de dominio (modelo puro)

Los modelos de dominio tienen sus propios tests en `{modulo}/domain/model/`:

```java
class ProductTest {

    @Test
    void create_should_set_active_true_and_timestamps() {
        // given / when
        Product product = Product.create(name, description, sku, unitId, categoryId,
                                          supplierId, purchasePrice, salePrice);
        // then
        assertThat(product.isActive()).isTrue();
        assertThat(product.getCreatedAt()).isNotNull();
    }

    @Test
    void create_should_throw_when_name_is_blank() {
        assertThatThrownBy(() -> Product.create("", description, sku, ...))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstitute_should_preserve_all_fields_without_validation() {
        Product product = Product.reconstitute(1L, name, description, sku, ...);
        assertThat(product.getId()).isEqualTo(1L);
    }
}
```

### Tests de use case (unit tests)

Cada use case debe cubrir tres escenarios obligatorios:

1. **Happy path** — flujo exitoso, verifica resultado y llamadas al repositorio
2. **Error de dominio** — regla de negocio violada (duplicado, estado inválido, no encontrado)
3. **Cortocircuito** — verificar con `verify(..., never())` que operaciones costosas no ocurren tras fallo temprano

```java
@ExtendWith(MockitoExtension.class)
class RegisterProductUseCaseTest {

    @Mock private ProductRepository productRepository;
    @Mock private StorageGateway storageGateway;

    @InjectMocks private RegisterProductUseCase useCase;

    @Test
    @DisplayName("execute registers product successfully when no imageUrl is provided")
    void should_register_product_successfully_without_image_url() {
        // given
        RegisterProductCommand command = buildCommand();
        when(productRepository.existsByName(command.name())).thenReturn(false);
        when(productRepository.existsBySku(command.sku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Product result = useCase.execute(command);

        // then
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(result.isActive()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("execute throws when product name already exists")
    void should_throw_when_product_name_already_exists() {
        // given
        when(productRepository.existsByName(any())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(PRODUCT_NAME);
    }

    @Test
    @DisplayName("execute never checks SKU or calls save when name is already taken")
    void should_not_check_sku_or_save_when_name_already_exists() {
        // given
        when(productRepository.existsByName(any())).thenReturn(true);

        // when
        assertThatThrownBy(() -> useCase.execute(buildCommand()))
            .isInstanceOf(IllegalArgumentException.class);

        // then — cortocircuito
        verify(productRepository, never()).existsBySku(any());
        verify(productRepository, never()).save(any());
    }
}
```

### `@ParameterizedTest` para validaciones de dominio

Cuando múltiples casos prueban la **misma operación** con distintos inputs inválidos y esperan el mismo tipo de excepción, usar `@ParameterizedTest` en lugar de tests repetidos:

```java
static Stream<Arguments> invalidFieldsForCreate() {
    return Stream.of(
        Arguments.of(null,   VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
        Arguments.of("   ",  VALID_SYMBOL, "nombre de la unidad no puede estar vacío"),
        Arguments.of(VALID_NAME, null,     "símbolo de la unidad no puede estar vacío"),
        Arguments.of(VALID_NAME, "   ",    "símbolo de la unidad no puede estar vacío")
    );
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("invalidFieldsForCreate")
@DisplayName("create throws IllegalArgumentException for each invalid field")
void should_throw_when_fields_are_invalid_on_create(
        String name, String symbol, String expectedMessage) {

    assertThatThrownBy(() -> Unit.create(name, symbol))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(expectedMessage);
}
```

**Regla:** si hay 3+ tests que difieren solo en los valores de entrada, reemplazarlos por `@ParameterizedTest`.

### Integration tests con Testcontainers

Para repositorios JPA (`infrastructure/repository/`) y queries nativas que H2 no soporta:

**Dependencias a agregar en `pom.xml`** (la primera vez que se escriban integration tests):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

**Clase base compartida** (crear una sola vez en `src/test/java/.../shared/`):

```java
// src/test/java/com/miapp/inventory_system/shared/BaseIntegrationTest.java
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");
}
```

`@ServiceConnection` configura el datasource automáticamente. No se necesita `@DynamicPropertySource`.

**Estructura de un integration test:**

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductJpaRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductJpaRepositorySpring repository;

    @Test
    void should_find_only_active_products() {
        // given — persistir directamente o con @Sql
        // when
        var result = repository.findAllByActiveTrue(Pageable.unpaged());
        // then
        assertThat(result).allMatch(e -> e.isActive());
    }
}
```

**Cuándo usar `@DataJpaTest` vs `@SpringBootTest`:**

| Escenario | Anotación |
|-----------|-----------|
| Repositorio JPA en aislamiento | `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` |
| Servicio que necesita contexto completo (security, gateways) | `@SpringBootTest` |

**Reglas para integration tests:**
- Siempre extender `BaseIntegrationTest` (un solo contenedor compartido)
- Nunca depender de los datos de los scripts Flyway de seed (V2–V8) — los tests son autocontenidos
- No agregar `@Transactional` por defecto en `@SpringBootTest` — oculta comportamiento real de commit

### Naming convention de tests

- Clase: `{ClaseBajoPrueba}Test` (unit) / `{ClaseBajoPrueba}IntegrationTest` (integration)
- Método: `should_{comportamiento_esperado}[_when_{condicion}]()`
- `@DisplayName`: descripción legible en inglés
- Agrupar tests relacionados con comentarios de sección: `// GROUP 1 — Happy path`

---

## Agentes del proyecto (`.claude/agents/`)

Estos tres agentes están configurados específicamente para este proyecto con sus convenciones:

### `code-reviewer` — revisor de arquitectura

**Cuándo usarlo:** después de implementar cualquier use case, modelo de dominio, controller o mapper.

Verifica:
- Factory methods (`create`/`reconstitute`) — nunca `new` en modelos de dominio
- Separación de capas — use cases no reciben/retornan JPA entities; controllers solo DTOs
- `@Transactional` presente en write use cases, ausente en controllers y mappers
- Dependencias inter-módulo — solo IDs primitivos
- Nomenclatura: `*UseCase`, `*Command`, `*JpaEntity`, `*JpaRepositorySpring`, `*RepositoryImpl`
- Validaciones en el lugar correcto: Bean Validation en DTOs, reglas de negocio en el dominio
- Lógica de negocio en el dominio, no en use cases ni mappers

Reporta en tres niveles: **Bloqueante** (viola regla arquitectónica), **Importante** (nombrado, acceso de capa incorrecto), **Sugerencia** (estilo).

### `migration-writer` — escritor de migraciones Flyway

**Cuándo usarlo:** al necesitar cambios en el esquema de BD (nuevas tablas, columnas, índices, constraints, seed data).

Reglas que aplica:
- Nunca modifica scripts existentes — siempre crea el siguiente `V{n}__desc.sql`
- Lee `src/main/resources/db/migration/` para determinar el número de versión correcto
- PostgreSQL con convenciones del proyecto: `BIGSERIAL`, `NUMERIC(10,2)`, `snake_case`, `CONSTRAINT fk_*`, `CONSTRAINT uq_*`
- No usa `CREATE TABLE IF NOT EXISTS` (Flyway garantiza ejecución única)
- No incluye `BEGIN`/`COMMIT` (Flyway maneja transacciones)

### `test-writer` — escritor de tests

**Cuándo usarlo:** para generar tests de use cases, modelos de dominio, guards, gateways o repositorios JPA.

Produce:
- **Unit tests** (JUnit 5 + Mockito) para use cases, modelos de dominio, `AssistantGuard`, `StorageGateway`
- **Integration tests** (Testcontainers + PostgreSQL real) para repositorios JPA y queries nativas
- Estructura BDD con Given/When/Then, `@ParameterizedTest` para variantes de inputs inválidos
- Clase `BaseIntegrationTest` con `@ServiceConnection` si es la primera vez

---

## Agentes globales (`~/.claude/agents/`)

Agentes de uso general disponibles en todos los proyectos:

| Agente | Cuándo usarlo en este proyecto |
|--------|-------------------------------|
| `java-reviewer` | Revisión adicional de Spring Boot (N+1, `@Transactional(readOnly)`, inyección de dependencias) |
| `tdd-guide` | Al comenzar una funcionalidad nueva — guía el ciclo RED → GREEN → REFACTOR |
| `security-reviewer` | Obligatorio ante cambios en `shared/security/`, `users/`, `AssistantGuard`, o cualquier endpoint con input de usuario |
| `build-error-resolver` | Cuando `mvnw.cmd clean package` o `mvnw.cmd test` falla |
| `planner` | Para funcionalidades complejas que abarcan múltiples módulos o capas |
| `architect` | Para decisiones que afectan la arquitectura del monolito (nuevos módulos, cambios en `shared/`) |
| `database-reviewer` | Para revisar queries JPA complejas, índices y rendimiento |

### Flujo recomendado para nueva funcionalidad

```
1. planner          → OpenSpec: diseño de capas, reglas de negocio, endpoint
2. tdd-guide        → escribir tests primero (RED)
3. [implementar]    → hasta GREEN
4. code-reviewer    → validar convenciones del proyecto (agente local)
5. test-writer      → completar cobertura si falta (agente local)
6. migration-writer → si hay cambios de esquema (agente local)
7. security-reviewer → si se tocan auth, JWT, input de usuario
```

---

## Comando `/module-status`

Analiza el estado de un módulo completo layer by layer y genera un reporte detallado.

```bash
/module-status products
/module-status warehouse
/module-status inventory
```

El reporte verifica por cada capa (`domain/`, `application/`, `api/`, `infrastructure/`):
- Archivos presentes vs esperados
- Violaciones de convenciones (factory methods, nomenclatura, dependencias inter-módulo, `@Transactional`)
- Estado: `✓` cumple / `⚠` faltan archivos / `✗` violación activa
- Acciones recomendadas priorizadas

---

## OpenSpec: cómo planear nuevas funcionalidades

Antes de implementar cualquier funcionalidad no trivial, describir el plan en este formato:

```
## OpenSpec: {NombreDelCasoDeUso}

### Módulo
{products | warehouse | inventory | suppliers | users | assistant}

### Capas afectadas
- [ ] domain/model
- [ ] domain/repository
- [ ] application/command
- [ ] application/usecase
- [ ] application/query
- [ ] api/controller
- [ ] api/dto
- [ ] api/mapper
- [ ] infrastructure/entity
- [ ] infrastructure/mapper
- [ ] infrastructure/repository
- [ ] db/migration (nuevo script Flyway — invocar migration-writer)

### Reglas de negocio
1. {Qué valida el dominio — qué IllegalArgumentException lanza}
2. {Qué ResourceNotFoundException puede lanzar}

### Endpoint REST
METHOD /api/{recurso}
Request: {campos del DTO con tipos y validaciones Bean Validation}
Response: {campos del response DTO con tipos}
Errores: 400 (validación), 404 (no encontrado), 422 (regla de negocio)

### Dependencias entre módulos
{IDs de otros módulos que se necesitan, ej: productId: Long, warehouseId: Long}

### Tests requeridos (invocar test-writer)
- [ ] {Modelo}Test: create (happy path + campos inválidos con @ParameterizedTest), reconstitute
- [ ] {UseCase}Test: happy path, error de dominio, cortocircuito
- [ ] {Repositorio}IntegrationTest: si hay queries nativas
```

---

## Seguridad y roles

- Roles disponibles: `ADMIN` y `OPERATOR` (`users/domain/model/Role.java`)
- Autenticación: **JWT** (`JwtAuthFilter`) + **Google OAuth**
- `SecurityConfig` en `shared/security/` define rutas públicas vs protegidas
- El dominio `User` no tiene dependencias de Spring Security; `UserDetailsServiceImpl` actúa como adaptador entre Spring Security y el dominio

### Cuándo invocar `security-reviewer` (obligatorio)

- Cambios en `shared/security/` (JwtService, JwtAuthFilter, SecurityConfig)
- Cualquier capa del módulo `users/`
- `shared/guard/AssistantGuard.java`
- Endpoints nuevos que reciben input de usuario sin pasar por validación previa

---

## Frontend

Repositorio separado: **nicolerol28/inventory-system-frontend**

- Stack: React + Vite + TailwindCSS
- Consume la API REST de este backend
- Al diseñar o modificar endpoints, considerar la compatibilidad con el frontend
