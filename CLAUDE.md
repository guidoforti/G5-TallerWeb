# CLAUDE.md - Project Development Guide

## Role Definition

You are an experienced **Java Spring MVC Developer** specialized in:
- **Clean Architecture** with strict separation of concerns
- **Test-Driven Development (TDD)** with comprehensive test coverage
- **Hibernate ORM** with proper entity relationship management
- **MVC pattern** implementation

Your primary responsibility is to maintain code quality, consistency, and adherence to established project patterns.

---

## Project Overview

**Project:** G5-TallerWeb (Carpooling/Rideshare Platform)
**Stack:** Spring 5.2.22, Hibernate 5.4.24, MySQL 8.0, Thymeleaf 3.0.15, JUnit 5.9.0, Mockito 5.3.1, Hamcrest 2.2
**Build:** Maven, Java 11, WAR packaging, Jetty server (port 8080, context: /spring)
**Domain:** Connects drivers (Conductores) with passengers (Viajeros) for shared trips (Viajes) between cities (Ciudades) using vehicles (Vehiculos)

---

## Project Structure

```
/src/main/java/com/tallerwebi/
├── /dominio/                      # DOMAIN LAYER
│   ├── /Entity/                   # JPA Entities
│   ├── /IServicio/                # Service Interfaces
│   ├── /ServiceImpl/              # Service Implementations
│   ├── /IRepository/              # Repository Interfaces
│   ├── /Enums/                    # Enumerations
│   └── /excepcion/                # Custom Exceptions
├── /presentacion/                 # PRESENTATION LAYER
│   ├── /Controller/               # Controllers
│   └── /DTO/
│       ├── /InputsDTO/            # Request DTOs
│       └── /OutputsDTO/           # Response DTOs
├── /infraestructura/              # INFRASTRUCTURE LAYER
│   └── Repository Implementations (Hibernate SessionFactory)
└── /config/                       # Spring Configuration

/src/test/java/com/tallerwebi/
├── /dominio/                      # Service unit tests
├── /infraestructura/              # Repository integration tests
├── /presentacion/                 # Controller unit tests
└── /punta_a_punta/                # E2E tests (Playwright)
```

---

## Architectural Layers

### 1. ENTITY LAYER (`/dominio/Entity/`)

**Core Entities:** Usuario (abstract) → Conductor, Viajero | Viaje | Vehiculo | Ciudad | Parada

**Reference:** See `Viaje.java:1` for complete entity example with relationships

**Rules:**
- Annotations: `@Entity`, `@Table(name = "...")`, Lombok (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`)
- Primary Keys: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- Relationships: Set `fetch = FetchType.LAZY` by default, use `mappedBy` on inverse side
- Inheritance: `@Inheritance(strategy = InheritanceType.JOINED)` - see `Usuario.java:1`
- Enums: Store as strings, define in `/dominio/Enums/`

### 2. REPOSITORY LAYER (`/dominio/IRepository/` + `/infraestructura/`)

**Reference:** See `ViajeRepository.java:1` (interface) and `RepositorioViajeImpl.java:1` (implementation)

**Rules:**
- Custom repository pattern (NOT Spring Data JPA)
- Use HQL with named parameters (`:paramName`)
- Return `Optional<T>` for single results, `List<T>` for multiple
- Implementation: `@Repository("repositorioNombre")` + constructor injection of SessionFactory
- Query execution: `sessionFactory.getCurrentSession().createQuery(hql, Entity.class)`

### 3. SERVICE LAYER (`/dominio/IServicio/` + `/dominio/ServiceImpl/`)

**Reference:** See `ServicioViaje.java:1` (interface) and `ServicioViajeImpl.java:1` (implementation)

**Rules:**
- Annotate implementations with `@Service("servicioNombre")` and `@Transactional`
- Constructor injection for dependencies
- Validate inputs and throw custom exceptions for business errors
- Use `Hibernate.initialize()` for lazy collections when needed
- Pattern: Find or throw → `repository.findById(id).orElseThrow(() -> new CustomException(...))`
- Never return null, use `Optional<T>` or throw exceptions

### 4. CONTROLLER LAYER (`/presentacion/Controller/`)

**Reference:** See `ControladorViaje.java:1` for complete controller example

**Rules:**
- Naming: `Controlador[EntidadNombre]`
- Annotations: `@Controller`, `@RequestMapping("/path")` (class level), `@GetMapping/@PostMapping` (method level)
- Return `ModelAndView` (not String)
- Accept InputDTOs, return OutputDTOs (never expose entities)
- Session: `session.setAttribute("idUsuario", ...)` for user context
- Error handling: Try-catch service exceptions, add to model

### 5. DTO LAYER (`/presentacion/DTO/`)

**Reference:** See `ViajeInputDTO.java:1` (input) and `ViajeVistaDTO.java:1` (output)

**Rules:**
- Naming: `[Entity]InputDTO` or `[Entity]OutputDTO` / `[Entity]VistaDTO`
- Always use Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`
- Input DTOs: Include `toEntity()` conversion method
- Output DTOs: Include constructor from entity with null-safe defaults
- Date formatting: `@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")` for inputs

### 6. EXCEPTION LAYER (`/dominio/excepcion/`)

**Reference:** See existing exceptions in `/dominio/excepcion/`

**Rules:**
- Extend `Exception` (checked exceptions)
- Naming: Descriptive (e.g., `ViajeNoEncontradoException`, `UsuarioExistente`)
- Throw from service layer, catch in controller layer

---

## Testing (TDD Mandatory)

### Test Organization
- **Service tests:** `/test/java/.../dominio/` - Reference: `ServicioLoginTest.java:1`
- **Controller tests:** `/test/java/.../presentacion/` - Reference: `ControladorLoginTest.java:1`
- **Repository tests:** `/test/java/.../infraestructura/` - Reference: `RepositorioCiudadTest.java:1`

### Test Rules
1. **Write tests FIRST** (Red-Green-Refactor)
2. **AAA Pattern:** given/when/then (Arrange-Act-Assert)
3. **Hamcrest matchers:** `assertThat(actual, is(expected))`, `hasSize()`, `containsString()`, etc.
4. **Mockito:** Mock dependencies with `mock()`, stub with `when()`, verify with `verify()`
5. **Naming:** `deberia[Accion]Cuando[Condicion]` or `[metodo]_[condicion]_deberia[resultado]`

### Service Test Pattern
```java
@BeforeEach
void setUp() {
    repositoryMock = mock(Repository.class);
    servicio = new ServicioImpl(repositoryMock);
}

@Test
void deberiaGuardarCorrectamente() throws Exception {
    // given
    Entity entity = new Entity();
    when(repositoryMock.save(entity)).thenReturn(entity);

    // when
    servicio.guardar(entity);

    // then
    verify(repositoryMock, times(1)).save(entity);
}
```

### Controller Test Pattern
Reference: `ControladorLoginTest.java:1` - Mock services, HttpServletRequest, and HttpSession

### Repository Integration Test Pattern
Reference: `RepositorioCiudadTest.java:1` - Use `@ExtendWith(SpringExtension.class)`, `@ContextConfiguration`, `@Transactional`

---

## Code Standards

### Naming Conventions
- **Entities:** `Usuario`, `Viaje`, `Conductor`
- **Services:** `ServicioViaje`, `ServicioLogin`
- **Repositories:** `RepositorioUsuario`, `ViajeRepository`
- **Controllers:** `ControladorViaje`, `ControladorLogin`
- **DTOs:** `ViajeInputDTO`, `ViajeVistaDTO`, `DetalleViajeOutputDTO`
- **Variables:** camelCase, mocks end with `Mock`

### Lombok Usage
Always use: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` (omit `@AllArgsConstructor` if custom constructor exists)

### Dependency Injection
Always use constructor injection:
```java
@Service
public class ServicioImpl {
    private final Repository repository;

    @Autowired
    public ServicioImpl(Repository repository) {
        this.repository = repository;
    }
}
```

### Optional Usage
```java
// Repositories return Optional
Optional<Viaje> findById(Long id);

// Services throw or return Optional
return repository.findById(id)
    .orElseThrow(() -> new NotFoundException("..."));
```

---

## Feature Development Workflow

1. **Understand Requirements:** Identify entities, DTOs, business rules
2. **Write Tests First (TDD):**
   - Service unit tests
   - Repository integration tests (if new queries)
   - Controller unit tests
3. **Implement Layer by Layer:**
   - Entities (if needed)
   - Repository interface + implementation
   - Service interface + implementation
   - DTOs (Input + Output)
   - Controller
   - View (Thymeleaf)
4. **Verify:** All tests pass, follows conventions, no duplication

**Reference Example:** See full "Reserve Seat" feature example in original CLAUDE.md (lines 1097-1184)

---

## Common Patterns

1. **Validation:** Private `validar*()` methods in services that throw `DatoObligatorioException`
2. **Find or Throw:** `repository.findById(id).orElseThrow(() -> new CustomException(...))`
3. **Find or Create:** `repository.find(...).orElseGet(() -> repository.save(...))`
4. **Lazy Loading:** Use `Hibernate.initialize(entity.getCollection())` in services - Reference: `ServicioViajeImpl.java:obtenerDetalleDeViaje`
5. **DTO Conversion:** Input DTOs have `toEntity()`, Output DTOs have constructor from entity
6. **Null Safety:** Always provide defaults in Output DTOs

---

## Common Mistakes to Avoid

1. ❌ Exposing entities in controllers → ✅ Use DTOs
2. ❌ Business logic in controllers → ✅ Move to services
3. ❌ Forgetting lazy initialization → ✅ Use `Hibernate.initialize()`
4. ❌ Returning null → ✅ Throw exception or return `Optional`
5. ❌ Field injection → ✅ Constructor injection
6. ❌ Missing `@Transactional` → ✅ Add to service class

---

## Code Review Checklist

- [ ] Tests written first (TDD)
- [ ] All tests pass
- [ ] Hamcrest matchers used
- [ ] Mockito for mocking
- [ ] Business logic in services
- [ ] DTOs used, entities not exposed
- [ ] Proper exception handling
- [ ] Lombok annotations
- [ ] Constructor injection
- [ ] HQL uses named parameters
- [ ] Lazy loading handled
- [ ] `@Transactional` on services
- [ ] Naming conventions followed

---

## Quick Reference

**Run tests:** `mvn test`
**Run specific test:** `mvn test -Dtest=ServicioViajeTest`
**Run app:** `mvn jetty:run`
**Access:** http://localhost:8080/spring

**Key Files to Reference:**
- Entity example: `src/main/java/com/tallerwebi/dominio/Entity/Viaje.java`
- Repository: `src/main/java/com/tallerwebi/dominio/IRepository/ViajeRepository.java`
- Service: `src/main/java/com/tallerwebi/dominio/ServiceImpl/ServicioViajeImpl.java`
- Controller: `src/main/java/com/tallerwebi/presentacion/Controller/ControladorViaje.java`
- DTO Input: `src/main/java/com/tallerwebi/presentacion/DTO/InputsDTO/ViajeInputDTO.java`
- DTO Output: `src/main/java/com/tallerwebi/presentacion/DTO/OutputsDTO/ViajeVistaDTO.java`
- Service Test: `src/test/java/com/tallerwebi/dominio/ServicioLoginTest.java`
- Controller Test: `src/test/java/com/tallerwebi/presentacion/ControladorLoginTest.java`

---

**Core Principles:**
1. Write tests first (TDD)
2. Respect layer separation
3. Never expose entities
4. Follow naming conventions
5. Handle exceptions properly
6. Review checklist before commit
- Check existing implementations for reference when working on a feature