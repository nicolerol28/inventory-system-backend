Analiza el estado del módulo `$ARGUMENTS` del proyecto inventory-system y genera un reporte detallado en español clasificado por capa.

## Pasos a seguir

### 1. Localizar el módulo
Busca la carpeta del módulo en:
```
src/main/java/com/inventory/system/$ARGUMENTS/
```
Si no existe, informa que el módulo no fue encontrado y detén el análisis.

### 2. Revisar cada capa en orden

Para cada capa, lista los archivos encontrados y evalúa su conformidad con las convenciones del proyecto.

---

#### Capa: `domain/`

**Archivos esperados:**
- `domain/model/` — una o más clases de dominio puro (sin anotaciones JPA ni Spring)
- `domain/repository/` — una o más interfaces de repositorio (puertos de salida)

**Verificar en cada clase de dominio (`domain/model/`):**
- [ ] No tiene anotaciones `@Entity`, `@Table`, `@Column`, `@Component`, `@Service`, ni ninguna de Spring/JPA
- [ ] El constructor es `private`
- [ ] Tiene factory method `create(...)` estático
- [ ] Tiene factory method `reconstitute(...)` estático
- [ ] Los campos opcionales usan `Optional<T>`, no `null`
- [ ] Las fechas usan `LocalDateTime`
- [ ] Los montos/cantidades usan `BigDecimal`
- [ ] La lógica de negocio (validaciones, cambios de estado) vive aquí, no en los use cases

**Verificar en cada interfaz de repositorio (`domain/repository/`):**
- [ ] Es una `interface`, no una clase
- [ ] No importa nada de `org.springframework.data` ni de JPA
- [ ] Los métodos retornan tipos de dominio o `Optional<T>`, nunca entidades JPA

---

#### Capa: `application/`

**Archivos esperados:**
- `application/usecase/` — un archivo por caso de uso (operaciones de escritura)
- `application/command/` — un comando por use case
- `application/query/` — servicios de consulta (operaciones de solo lectura)

**Verificar en cada use case (`application/usecase/`):**
- [ ] Nombre sigue el patrón `{Verbo}{Entidad}UseCase` (e.g., `RegisterProductUseCase`)
- [ ] Tiene `@Transactional` (a nivel de clase o en el método `execute(...)`)
- [ ] Solo depende de interfaces de dominio (`domain/repository/`), nunca de JPA directamente
- [ ] No recibe ni retorna entidades JPA
- [ ] No contiene lógica de negocio propia — orquesta llamadas al dominio

**Verificar en cada comando (`application/command/`):**
- [ ] Nombre sigue el patrón `{Verbo}{Entidad}Command`
- [ ] Es una clase plana o `record` sin lógica de negocio

**Verificar en cada query service (`application/query/`):**
- [ ] Va directamente a `*JpaRepositorySpring`, nunca a través del repositorio de dominio
- [ ] No tiene efectos secundarios (solo lectura)

---

#### Capa: `api/`

**Archivos esperados:**
- `api/controller/` — controladores REST
- `api/dto/` — clases de request y response
- `api/mapper/` — mappers entre DTOs y comandos/dominio

**Verificar en cada controller (`api/controller/`):**
- [ ] Anotado con `@RestController` y `@RequestMapping`
- [ ] Usa `@Valid` en parámetros de request body
- [ ] No contiene lógica de negocio
- [ ] No retorna objetos de dominio directamente — siempre DTOs
- [ ] No tiene `@Transactional`
- [ ] No captura excepciones de negocio con `try/catch` — las delega al `GlobalExceptionHandler`

**Verificar en cada DTO (`api/dto/`):**
- [ ] Los DTOs de request tienen anotaciones de validación (`@NotBlank`, `@NotNull`, `@Size`, etc.)
- [ ] Los DTOs de response no exponen entidades JPA ni objetos de dominio

**Verificar en cada mapper de API (`api/mapper/`):**
- [ ] Convierte DTOs → Commands (entrada) y Domain → Response DTOs (salida)
- [ ] No contiene lógica de negocio

---

#### Capa: `infrastructure/`

**Archivos esperados:**
- `infrastructure/entity/` — entidades JPA (`*JpaEntity`)
- `infrastructure/mapper/` — mappers entre dominio y entidad JPA
- `infrastructure/repository/` — implementaciones de repositorio (`*RepositoryImpl`, **sin** `Jpa`) y repositorios Spring Data (`*JpaRepositorySpring`)

**Verificar en cada entidad JPA (`infrastructure/entity/`):**
- [ ] Nombre sigue el patrón `{Entidad}JpaEntity` (e.g., `ProductJpaEntity`)
- [ ] Anotada con `@Entity` y `@Table`
- [ ] No contiene lógica de negocio

**Verificar en cada mapper de infraestructura (`infrastructure/mapper/`):**
- [ ] Convierte Domain ↔ JpaEntity en ambas direcciones
- [ ] No contiene lógica de negocio

**Verificar en cada repositorio (`infrastructure/repository/`):**
- [ ] La interfaz Spring Data sigue el patrón `{Entidad}JpaRepositorySpring` y extiende `JpaRepository` (e.g., `ProductJpaRepositorySpring`)
- [ ] La implementación sigue el patrón `{Entidad}RepositoryImpl` — **sin** el segmento `Jpa` — e implementa la interfaz de dominio (e.g., `ProductRepositoryImpl`, `CategoryRepositoryImpl`, `UnitRepositoryImpl`)

---

### 3. Verificar dependencias inter-módulo

Lee los archivos del módulo y busca imports de otros módulos (`products`, `suppliers`, `warehouses`, `inventory`, `users`).

- [ ] No hay referencias a objetos de dominio de otro módulo (solo IDs primitivos: `Long`, `UUID`)
- [ ] Si hay validación cruzada, se hace mediante una interfaz definida en `domain/` de este módulo

---

### 4. Generar el reporte

Produce el reporte con esta estructura:

```
# Estado del módulo: $ARGUMENTS

## Resumen
<estado general: completo / incompleto / con violaciones>
Capas encontradas: X/4
Archivos analizados: N

---

## domain/
**Estado:** ✓ Completo | ⚠ Incompleto | ✗ Violaciones

### Archivos encontrados
- `NombreClase.java` — [descripción breve]

### Faltantes
- <lista de archivos o elementos esperados que no se encontraron>

### Violaciones
- `Archivo.java` — <descripción de la violación y qué regla rompe>

---

## application/
[misma estructura]

---

## api/
[misma estructura]

---

## infrastructure/
[misma estructura]

---

## Dependencias inter-módulo
**Estado:** ✓ Sin violaciones | ✗ Violaciones detectadas
- <detalle si hay imports cruzados indebidos>

---

## Acciones recomendadas
1. <acción concreta para resolver el problema más crítico>
2. <siguiente acción>
...
```

Usa estos iconos de estado:
- `✓` — cumple las convenciones
- `⚠` — faltan archivos esperados pero no hay violaciones activas
- `✗` — hay al menos una violación de arquitectura o nomenclatura
