package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioUsuarioTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioUsuario repositorioUsuario;
    private Usuario usuarioBase;

    private LocalDate calcularFechaNacimiento(int edad) {
        return LocalDate.now().minusYears(edad).minusDays(1);
    }

    @BeforeEach
    public void setUp() {
        this.repositorioUsuario = new RepositorioUsuarioImpl(this.sessionFactory);

        usuarioBase = new Viajero();
        usuarioBase.setNombre("UsuarioBase");
        usuarioBase.setEmail("base@test.com");
        usuarioBase.setContrasenia("pass123");
        usuarioBase.setRol("VIAJERO");
        usuarioBase.setActivo(true);

        usuarioBase.setFechaNacimiento(calcularFechaNacimiento(30));
        usuarioBase.setFumador(true);

        repositorioUsuario.guardar(usuarioBase);
    }

    @Test
    public void deberiaGuardarUnUsuarioNuevoYAsignarleId() {
        // Arrange
        Usuario nuevoUsuario = new Viajero();
        nuevoUsuario.setEmail("nuevo@guardar.com");
        nuevoUsuario.setContrasenia("clave456");
        nuevoUsuario.setNombre("Nuevo Nombre");
        nuevoUsuario.setRol("VIAJERO");

        nuevoUsuario.setFechaNacimiento(calcularFechaNacimiento(25));
        nuevoUsuario.setDiscapacitado("Auditiva");

        // Act
        Usuario guardado = repositorioUsuario.guardar(nuevoUsuario);

        // Assert
        assertNotNull(guardado.getId(), "El usuario debería tener un ID asignado.");
        assertThat(guardado.getEmail(), is(equalTo("nuevo@guardar.com")));


        Optional<Usuario> recuperado = repositorioUsuario.buscarPorEmail("nuevo@guardar.com");
        assertTrue(recuperado.isPresent());
        assertThat(recuperado.get().getNombre(), is(equalTo("Nuevo Nombre")));

        assertThat(recuperado.get().getEdad(), is(25));
        assertThat(recuperado.get().getDiscapacitado(), is(equalTo("Auditiva")));
    }

    @Test
    public void deberiaEncontrarUsuarioConCredencialesCorrectas() {
        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarUsuario(usuarioBase.getEmail(), usuarioBase.getContrasenia());

        // Assert
        assertTrue(resultado.isPresent(), "Debería encontrar al usuario con email y contraseña correctos.");
        assertThat(resultado.get().getEmail(), is(equalTo(usuarioBase.getEmail())));
    }

    @Test
    public void noDeberiaEncontrarUsuarioSiContraseniaEsIncorrecta() {
        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarUsuario(usuarioBase.getEmail(), "claveMala");

        // Assert
        assertTrue(resultado.isEmpty(), "No debería encontrar al usuario con contraseña incorrecta.");
    }

    @Test
    public void noDeberiaEncontrarUsuarioSiEmailNoExiste() {
        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarUsuario("noexiste@mail.com", usuarioBase.getContrasenia());

        // Assert
        assertTrue(resultado.isEmpty(), "No debería encontrar al usuario si el email no existe.");
    }
    @Test
    public void deberiaEncontrarUsuarioPorEmailExistente() {
        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarPorEmail(usuarioBase.getEmail());

        // Assert
        assertTrue(resultado.isPresent(), "Debería encontrar al usuario con email existente.");
        assertThat(resultado.get().getNombre(), is(equalTo("UsuarioBase")));
    }

    @Test
    public void noDeberiaEncontrarUsuarioPorEmailInexistente() {
        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarPorEmail("otro@mail.com");

        // Assert
        assertTrue(resultado.isEmpty(), "No debería encontrar al usuario si el email no existe.");
    }

    @Test
    public void deberiaModificarLaContraseniaDelUsuario() {
        // Arrange
        String nuevaContrasenia = "newPass456";

        usuarioBase.setContrasenia(nuevaContrasenia);

        // Act
        repositorioUsuario.modificarUsuario(usuarioBase);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // Assert: Intentamos loguearnos con la nueva contraseña
        Optional<Usuario> recuperado = repositorioUsuario.buscarUsuario(usuarioBase.getEmail(), nuevaContrasenia);

        assertTrue(recuperado.isPresent(), "La modificación debería ser persistida y permitir el login con la nueva contraseña.");
        assertEquals(nuevaContrasenia, recuperado.get().getContrasenia());
    }

    @Test
    public void deberiaModificarElNombreDelUsuario() {
        // Arrange
        String nuevoNombre = "Usuario Modificado";
        usuarioBase.setNombre(nuevoNombre);

        // Act
        repositorioUsuario.modificarUsuario(usuarioBase);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // Assert
        Optional<Usuario> recuperado = repositorioUsuario.buscarPorEmail(usuarioBase.getEmail());

        assertTrue(recuperado.isPresent(), "El usuario modificado debería ser encontrado.");
        assertThat(recuperado.get().getNombre(), is(equalTo(nuevoNombre)));
    }

    @Test
    public void deberiaEncontrarUsuarioPorIdExistente() {
        Long idExistente = usuarioBase.getId();

        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarPorId(idExistente);

        // Assert
        assertTrue(resultado.isPresent(), "Debería encontrar un usuario existente por su ID.");
        assertThat(resultado.get().getId(), is(equalTo(idExistente)));
        assertThat(resultado.get().getNombre(), is(equalTo(usuarioBase.getNombre())));

        // Verificamos que el campo booleano se recupera
        assertThat(resultado.get().getFumador(), is(true));
    }

    @Test
    public void noDeberiaEncontrarUsuarioPorIdInexistente() {
        // Arrange
        Long idInexistente = 9999L;

        // Act
        Optional<Usuario> resultado = repositorioUsuario.buscarPorId(idInexistente);

        // Assert
        assertTrue(resultado.isEmpty(), "No debería encontrar ningún usuario con un ID inexistente.");
    }
}