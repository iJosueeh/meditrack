package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Usuarios.
 */
public interface UsuarioRepository {

    Optional<Usuario> findById(String id);

    Usuario findByDocumento(String numeroDocumento);

    Usuario authenticate(String numeroDocumento, String password);

    List<Usuario> findAll();

    List<Usuario> findByRolId(String rolId);

    Usuario save(Usuario usuario, String rawPassword);

    Usuario update(Usuario usuario);

    boolean updatePassword(String usuarioId, String hashedPassword);

    void toggleEstado(String id, int nuevoEstado);

    String getUltimaActividad(String usuarioId);

    List<Rol> findAllRoles();

    List<Sede> findSedesActivas();
}
