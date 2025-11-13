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

---

## E2E Testing with Playwright

### Overview
**Location:** `/src/test/java/com/tallerwebi/punta_a_punta/`
**Framework:** Playwright (Microsoft)
**Browser:** Chrome (mandatory)
**Pattern:** Page Object Model (POM)
**Reference Guide:** See detailed E2E guide in project documentation

### Test Organization

```
/src/test/java/com/tallerwebi/punta_a_punta/
├── VistaConductorCrearViajeE2E.java         # E2E test: Conductor creates vehicle and publishes trip
├── VistaViajeroSolicitarReservaE2E.java     # E2E test: Viajero searches and reserves trip
├── VistaConductorReservasE2E.java           # E2E test: Conductor manages reservations
├── VistaConductorIniciarFinalizarViajeE2E.java  # E2E test: Conductor starts and finalizes trip
├── ReiniciarDB.java                         # Database cleanup utility
└── /vistas/                                 # Page Objects
    ├── VistaWeb.java                        # Base class with common methods
    ├── VistaLogin.java                      # Login page object
    ├── VistaHomeConductor.java              # Conductor home page object
    ├── VistaHomeViajero.java                # Viajero home page object
    ├── VistaRegistrarVehiculo.java          # Vehicle registration page object
    ├── VistaPublicarViaje.java              # Publish travel page object
    ├── VistaBuscarViaje.java                # Search trip page object
    ├── VistaDetalleViaje.java               # Trip detail page object
    └── (other page objects)
```

### Test Structure

**Naming Convention:**
- Test classes: `Vista[Nombre]E2E.java`
- Page objects: `Vista[Nombre].java`
- Test methods: `deberia[Accion]Cuando[Condicion]()`

**Example Test Class:**
```java
public class VistaConductorCrearViajeE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;  // IMPORTANT: page as class property
    VistaLogin vistaLogin;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500)
        );
    }

    @BeforeEach
    void crearContextoYPagina() {
        ReiniciarDB.limpiarBaseDeDatos();  // CRITICAL: Clean DB before each test
        context = browser.newContext();
        page = context.newPage();  // IMPORTANT: Initialize page property
        vistaLogin = new VistaLogin(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }
}
```

### Page Object Model (POM)

**Base Class - VistaWeb.java:**
```java
public class VistaWeb {
    protected Page page;

    public VistaWeb(Page page) {
        this.page = page;
    }

    protected void darClickEnElElemento(String selectorCSS) {
        this.obtenerElemento(selectorCSS).click();
    }

    protected void escribirEnElElemento(String selectorCSS, String texto) {
        this.obtenerElemento(selectorCSS).fill(texto);  // Use fill(), not type()
    }

    protected String obtenerTextoDelElemento(String selectorCSS) {
        return this.obtenerElemento(selectorCSS).textContent();
    }

    private Locator obtenerElemento(String selectorCSS) {
        return page.locator(selectorCSS);
    }
}
```

**Page Object Example:**
```java
public class VistaHomeConductor extends VistaWeb {

    public VistaHomeConductor(Page page) {
        super(page);
        this.page.navigate("localhost:8080/spring/conductor/home");
    }

    public void darClickEnPublicarViaje() {
        darClickEnElElemento("#btn-publicar-viaje");
    }

    public String obtenerNombreConductor() {
        return obtenerTextoDelElemento("h4 span");
    }
}
```

### HTML Semantic IDs

**CRITICAL:** All interactive elements MUST have semantic IDs for testing.

**Naming Convention:**
- Buttons: `#btn-[action]` (e.g., `#btn-login`, `#btn-submit-viaje`)
- Confirmation buttons in modals: `#btn-confirm-[action]` (e.g., `#btn-confirm-iniciar-viaje`)
- Links: `#btn-[action]` or `#link-[destination]`
- Forms: `#form-[name]`
- Messages: `#mensaje-[type]` (e.g., `#mensaje-error`, `#mensaje-exito`)
- Badges/Status: `#badge-[type]` (e.g., `#badge-estado-viaje`)

**Example:**
```html
<button type="submit" id="btn-submit-viaje" class="btn btn-primary">
    Publicar Viaje
</button>
<a th:href="@{/conductor/home}" id="btn-cancelar-viaje" class="text-decoration-none">
    Cancelar
</a>
```

### Database Cleanup - ReiniciarDB.java

**CRITICAL:** Every test MUST start with a clean database state.

```java
public class ReiniciarDB {
    public static void limpiarBaseDeDatos() {
        String sqlCommands =
            "SET FOREIGN_KEY_CHECKS=0;\n" +
            "DELETE FROM reserva;\n" +
            "DELETE FROM parada;\n" +
            "DELETE FROM viaje;\n" +
            "DELETE FROM vehiculo;\n" +
            "DELETE FROM conductor;\n" +
            "DELETE FROM viajero;\n" +
            "DELETE FROM usuario;\n" +
            "ALTER TABLE usuario AUTO_INCREMENT = 1;\n" +
            "ALTER TABLE vehiculo AUTO_INCREMENT = 1;\n" +
            "ALTER TABLE viaje AUTO_INCREMENT = 1;\n" +
            "SET FOREIGN_KEY_CHECKS=1;\n" +
            // Insert test users
            "INSERT INTO usuario(id, email, contrasenia, nombre, rol, activo) " +
            "VALUES(1, 'conductor@test.com', 'test123', 'Test Conductor', 'CONDUCTOR', true);\n" +
            "INSERT INTO conductor(usuario_id, fecha_de_vencimiento_licencia) " +
            "VALUES(1, '2027-12-31');";

        String comando = String.format(
            "docker exec tallerwebi-mysql mysql -u user -puser tallerwebi -e \"%s\"",
            sqlCommands
        );
        // Execute command...
    }
}
```

**Key Points:**
- Use lowercase table names (MySQL is case-sensitive)
- Column is `contrasenia` (NOT `password`)
- Conductor table only has `usuario_id` and `fecha_de_vencimiento_licencia`
- Reference `src/main/resources/data.sql` for correct structure

### Tom Select / Nominatim Autocomplete Handling

**Challenge:** The app uses Tom Select library with Nominatim API for city selection.

**Solution in VistaWeb.java:**
```java
protected void seleccionarEnTomSelect(String selectId, String searchText) {
    // 1. Get Tom Select wrapper specific to this select
    String tomSelectWrapperSelector = "#" + selectId + " + .ts-wrapper";
    String tomSelectInputSelector = tomSelectWrapperSelector + " .ts-control input";

    // 2. Click and type in the input
    page.locator(tomSelectInputSelector).click();
    page.locator(tomSelectInputSelector).fill(searchText);

    // 3. Wait for Nominatim API response
    page.waitForResponse(
        response -> response.url().contains("/nominatim/buscar") && response.status() == 200,
        () -> {}
    );

    // 4. Scope dropdown to this specific wrapper (CRITICAL)
    String dropdownSelector = tomSelectWrapperSelector + " .ts-dropdown";

    // 5. Wait for dropdown and options
    page.locator(dropdownSelector).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
    );
    page.locator(dropdownSelector + " .option[data-selectable]").first().waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
    );

    // 6. Click first option
    page.locator(dropdownSelector + " .option[data-selectable]").first().click();

    // 7. Wait for dropdown to close
    page.locator(dropdownSelector).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
    );
}
```

**Key Points:**
- MUST scope dropdown selector to specific Tom Select wrapper
- Multiple dropdowns on same page cause ambiguity
- Wait for API response before interacting with dropdown
- Use `.fill()` not `.type()` for Tom Select inputs

### Test Method Pattern (Given-When-Then)

**Tests should read like business documentation:**

```java
@Test
void conductorDeberiaCrearVehiculoYPublicarViaje() throws MalformedURLException {
    // GIVEN - Setup
    dadoQueElConductorCargaSusDatosDeLogin("conductor@test.com", "test123");
    cuandoElConductorTocaElBotonDeLogin();
    entoncesDeberiaSerRedirigidoAlHomeConductor();

    // WHEN - Actions
    VistaHomeConductor vistaHome = new VistaHomeConductor(page);
    cuandoElConductorNavegaAPublicarViaje(vistaHome);

    VistaPublicarViaje vistaViaje = new VistaPublicarViaje(page);
    cuandoElConductorSeleccionaOrigen(vistaViaje, "Buenos Aires");
    cuandoElConductorSeleccionaDestino(vistaViaje, "Cordoba");
    cuandoElConductorIngresaFechaSalida(vistaViaje, "2025-12-25T14:00");
    cuandoElConductorEnviaElFormularioDeViaje(vistaViaje);

    // THEN - Assertions
    entoncesDeberiaSerRedirigidoAlHomeConductor();
}

// Private helper methods
private void dadoQueElConductorCargaSusDatosDeLogin(String email, String clave) {
    vistaLogin.escribirEMAIL(email);
    vistaLogin.escribirClave(clave);
}

private void entoncesDeberiaSerRedirigidoAlHomeConductor() throws MalformedURLException {
    URL url = vistaLogin.obtenerURLActual();
    assertThat(url.getPath(), matchesPattern("^/spring/conductor/home(?:;jsessionid=[^/\\s]+)?$"));
}
```

**Naming Conventions:**
- `dadoQue...()` - Preconditions
- `cuando...()` - Actions
- `entonces...()` - Assertions

### Common Patterns

**1. Navigation with Wait:**
```java
private void cuandoElConductorEnviaElFormularioDeViaje(VistaPublicarViaje vistaViaje) {
    vistaViaje.darClickEnSubmit();
    // Wait for redirect
    page.waitForURL("**/conductor/home**");
}
```

**2. Date Input:**
```java
// Use fill() for datetime-local inputs
vistaViaje.escribirFechaHoraSalida("2025-12-25T14:00");

// In VistaWeb: MUST use fill(), not type()
protected void escribirEnElElemento(String selectorCSS, String texto) {
    this.obtenerElemento(selectorCSS).fill(texto);  // ✓ CORRECT
    // this.obtenerElemento(selectorCSS).type(texto);  // ✗ WRONG for dates
}
```

**3. Select Dropdown:**
```java
// Standard select (NOT Tom Select)
public void seleccionarVehiculo(String valorVehiculo) {
    page.locator("#idVehiculo").selectOption(valorVehiculo);
}
```

**4. URL Assertions with Session ID:**
```java
// Use regex to handle optional jsessionid
assertThat(url.getPath(), matchesPattern("^/spring/conductor/home(?:;jsessionid=[^/\\s]+)?$"));
```

**5. Bootstrap Modal Confirmations:**
```java
// IMPORTANT: Use Bootstrap modals instead of JavaScript confirm() dialogs
// Pattern: Click button → Wait for modal → Click confirmation in modal

private void cuandoElConductorIniciaElViaje(VistaDetalleViaje vistaDetalle) {
    // Click button to open modal
    vistaDetalle.darClickEnIniciarViaje();

    // Wait for modal to appear
    page.waitForSelector("#confirmStartModal", new Page.WaitForSelectorOptions().setTimeout(2000));

    // Click confirmation button in modal
    vistaDetalle.darClickEnConfirmarIniciarViaje();

    // Wait for navigation/reload
    page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
}

// Page Object methods needed:
public void darClickEnIniciarViaje() {
    darClickEnElElemento("#btn-iniciar-viaje");  // Opens modal
}

public void darClickEnConfirmarIniciarViaje() {
    darClickEnElElemento("#btn-confirm-iniciar-viaje");  // Confirms in modal
}
```

### E2E Test Development Workflow

1. **Identify Core Workflow:** Choose user journey to test
2. **Prepare HTML:** Add semantic IDs to all interactive elements
3. **Create Page Objects:**
   - Create class extending `VistaWeb`
   - Add methods for each user action (using business language)
   - Reference existing page objects for patterns
4. **Update ReiniciarDB:** Ensure test user(s) are created
5. **Write Test:**
   - Use Given-When-Then pattern
   - Create private helper methods for readability
   - Add assertions at key checkpoints
   - **CRITICAL:** Use unique vehicle patentes and unique trip routes to avoid conflicts with ReiniciarDB.java
6. **Run Test:**
   - Run with `setHeadless(false)` and `setSlowMo(500)` for debugging
   - Use `mvn test -Dtest=VistaConductorCrearViajeE2E`
7. **Iterate:** Fix issues, refine selectors, improve waits

**IMPORTANT - Test Data Uniqueness:**
- Each test should create unique data to avoid conflicts
- Vehicle patentes must be unique (e.g., Test #1 uses "DEF456", ReiniciarDB uses "ABC123")
- Trip routes must be unique (e.g., Buenos Aires → Mendoza vs Buenos Aires → Córdoba)
- Check ReiniciarDB.java to see what data already exists before creating test data

### Running E2E Tests

**Before Running:**
```bash
# 1. Build application
mvn clean package -DskipTests

# 2. Start Docker containers
docker compose down
docker compose up -d --build

# 3. Wait for app to start (15-20 seconds)
```

**Run Tests:**
```bash
# Run specific E2E test
mvn test -Dtest=VistaConductorCrearViajeE2E
mvn test -Dtest=VistaViajeroSolicitarReservaE2E
mvn test -Dtest=VistaConductorReservasE2E
mvn test -Dtest=VistaConductorIniciarFinalizarViajeE2E

# Run all E2E tests
mvn test -Dtest="*E2E"
```

### Debugging Tips

**1. Visual Debugging:**
```java
@BeforeAll
static void abrirNavegador() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(false)  // See browser
            .setSlowMo(500)      // Slow down actions
    );
}
```

**2. Screenshot on Failure:**
```java
@AfterEach
void cerrarContexto() {
    if (testFailed) {
        context.pages().get(0).screenshot(
            new Page.ScreenshotOptions().setPath(Paths.get("screenshots/failure.png"))
        );
    }
    context.close();
}
```

**3. Check Element Existence:**
```java
boolean exists = page.locator("#elemento").count() > 0;
System.out.println("Element exists: " + exists);
```

### Common Issues & Solutions

**Issue:** "Timeout waiting for locator"
- **Cause:** Selector doesn't match any element
- **Solution:** Verify ID in HTML, check if element is visible

**Issue:** "Strict mode violation: locator resolved to 2 elements"
- **Cause:** Selector matches multiple elements (e.g., multiple `.ts-dropdown`)
- **Solution:** Scope selector to specific parent (e.g., `#selectId + .ts-wrapper .ts-dropdown`)

**Issue:** "Database cleanup fails"
- **Cause:** Incorrect table/column names (MySQL is case-sensitive)
- **Solution:** Use lowercase table names, reference `data.sql`

**Issue:** "Form submission doesn't redirect"
- **Cause:** Date input not filled correctly
- **Solution:** Use `.fill()` instead of `.type()` for datetime-local inputs

**Issue:** "Tom Select dropdown doesn't open"
- **Cause:** Not waiting for JavaScript to initialize
- **Solution:** Add small wait or ensure Tom Select library is loaded

**Issue:** "JavaScript confirm() dialogs not working in E2E tests"
- **Cause:** JavaScript confirm() dialogs require special handling in Playwright
- **Solution:** Use Bootstrap modals instead for better UX and easier E2E testing. Add semantic IDs to modal buttons (`#btn-confirm-[action]`)

### E2E Test Checklist

- [ ] `Page page;` declared as class property (NOT local variable)
- [ ] All Page Objects instantiated with `page` (NOT `context.pages().get(0)`)
- [ ] All interactive elements have semantic IDs
- [ ] Page Objects created for all views
- [ ] Database cleanup works correctly
- [ ] Test uses Given-When-Then pattern
- [ ] Test methods have business-friendly names
- [ ] Waits properly for async operations (API calls, redirects)
- [ ] Date inputs use `.fill()` not `.type()`
- [ ] Tom Select dropdowns properly scoped
- [ ] Bootstrap modals used instead of JavaScript confirm() dialogs
- [ ] Assertions check key outcomes
- [ ] Test runs successfully with headless mode

### Reference Tests

**Existing E2E Tests:**
- `VistaConductorCrearViajeE2E.conductorDeberiaCrearVehiculoYPublicarViaje()` - Complete driver workflow: create vehicle and publish trip
- `VistaViajeroSolicitarReservaE2E.viajeroDeberiaBuscarViajeYSolicitarReserva()` - Passenger workflow: search trip and request reservation
- `VistaConductorReservasE2E` - Driver manages reservations: accept/reject passenger requests
- `VistaConductorIniciarFinalizarViajeE2E.conductorDeberiaIniciarYFinalizarViaje()` - Driver manages trip lifecycle: start and finalize trip

**Key Files:**
- Base class: `src/test/java/com/tallerwebi/punta_a_punta/vistas/VistaWeb.java`
- DB cleanup: `src/test/java/com/tallerwebi/punta_a_punta/ReiniciarDB.java`
- Test examples:
  - `src/test/java/com/tallerwebi/punta_a_punta/VistaConductorCrearViajeE2E.java`
  - `src/test/java/com/tallerwebi/punta_a_punta/VistaViajeroSolicitarReservaE2E.java`
  - `src/test/java/com/tallerwebi/punta_a_punta/VistaConductorIniciarFinalizarViajeE2E.java`
- Always redeploy the application with mvn clean package and docker compose commands when changing HTML views for E2E tests