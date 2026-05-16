package com.utp.meditrackapp.features.auth.Dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Usuario;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioDaoIntegrationTest {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
    }

    @Test
    public void testLoginSuccess() {
        // Credenciales: DNI 12345678, Pass: admin123
        Usuario usuario = usuarioDao.login("12345678", "admin123");
        
        assertNotNull(usuario, "El login debería ser exitoso");
        assertEquals("12345678", usuario.getNumeroDocumento());
        assertEquals("Admin", usuario.getNombres());
        
        // Verificamos que ahora traiga nombres descriptivos
        assertNotNull(usuario.getSedeNombre(), "El nombre de la sede no debe ser nulo");
        assertNotNull(usuario.getRolNombre(), "El nombre del rol no debe ser nulo");
        assertEquals("Administrador", usuario.getRolNombre());
    }

    @Test
    public void testLoginFailureWrongPassword() {
        Usuario usuario = usuarioDao.login("12345678", "pass_erroneo");
        assertNull(usuario, "El login debería fallar");
    }

    @Test
    public void testGetUltimaActividad() {
        // Como es una base limpia, debería devolver el mensaje por defecto
        String actividad = usuarioDao.getUltimaActividad("USR-001");
        assertNotNull(actividad);
    }

    @Test
    public void testUpdateUser() {
        Usuario usuario = usuarioDao.login("12345678", "admin123");
        assertNotNull(usuario);

        String originalNombres = usuario.getNombres();
        usuario.setNombres("Nombre Editado");

        boolean result = usuarioDao.updateUser(usuario);
        assertTrue(result, "La actualización debería ser exitosa");

        Usuario updated = usuarioDao.login("12345678", "admin123");
        assertEquals("Nombre Editado", updated.getNombres());

        // Limpieza: revertir cambios
        usuario.setNombres(originalNombres);
        usuarioDao.updateUser(usuario);
    }

    @Test
    public void testUpdatePassword() {
        Usuario usuario = usuarioDao.login("12345678", "admin123");
        assertNotNull(usuario);

        // Cambiar a una nueva contraseña temporal
        String newPass = "newTempPass123";
        String newHash = com.utp.meditrackapp.core.util.PasswordHasher.hashPassword(newPass);
        
        boolean result = usuarioDao.updatePassword(usuario.getId(), newHash);
        assertTrue(result, "El cambio de contraseña debería ser exitoso");

        // Verificar login con nueva contraseña
        Usuario updated = usuarioDao.login("12345678", newPass);
        assertNotNull(updated, "Debería poder loguearse con la nueva contraseña");

        // Revertir a contraseña original (admin123)
        String originalHash = com.utp.meditrackapp.core.util.PasswordHasher.hashPassword("admin123");
        usuarioDao.updatePassword(usuario.getId(), originalHash);
    }
}
