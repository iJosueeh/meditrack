package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.services.catalogo.GestionarCatalogoUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcCategoriaRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMotivoMovimientoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcTipoMovimientoRepository;

import java.util.List;

/**
 * Adaptador para controladores de catálogos.
 * Delega al GestionarCatalogoUseCase del dominio.
 */
public class CatalogAdapter {
    private final GestionarCatalogoUseCase useCase;

    public CatalogAdapter() {
        this.useCase = new GestionarCatalogoUseCase(
            new JdbcRolRepository(),
            new JdbcCategoriaRepository(),
            new JdbcTipoMovimientoRepository(),
            new JdbcMotivoMovimientoRepository()
        );
    }

    // === Roles ===
    public List<Rol> listarRoles() { return useCase.listarRoles(); }
    public String crearRol(Rol rol) { return useCase.guardarRol(rol); }
    public void actualizarRol(Rol rol) { useCase.actualizarRol(rol); }
    public String toggleEstadoRol(String id) { return useCase.toggleEstadoRol(id); }
    public int contarUsuariosPorRol(String rolId) {
        return useCase.contarUsuariosPorRol(rolId);
    }
    public String eliminarRol(String id) { return useCase.eliminarRol(id); }

    // === Categorías ===
    public List<Categoria> listarCategorias() { return useCase.listarCategorias(); }
    public String crearCategoria(Categoria cat) { return useCase.guardarCategoria(cat); }
    public void actualizarCategoria(Categoria cat) { useCase.actualizarCategoria(cat); }
    public String toggleEstadoCategoria(String id) { return useCase.toggleEstadoCategoria(id); }
    public String eliminarCategoria(String id) { return useCase.eliminarCategoria(id); }

    // === Tipos de Movimiento ===
    public List<TipoMovimiento> listarTiposMovimiento() { return useCase.listarTiposMovimiento(); }
    public String crearTipoMovimiento(TipoMovimiento tipo) { return useCase.guardarTipoMovimiento(tipo); }
    public void actualizarTipoMovimiento(TipoMovimiento tipo) { useCase.actualizarTipoMovimiento(tipo); }
    public String eliminarTipoMovimiento(String id) { return useCase.eliminarTipoMovimiento(id); }

    // === Motivos de Movimiento ===
    public List<MotivoMovimiento> listarMotivosMovimiento() { return useCase.listarMotivosMovimiento(); }
    public String crearMotivoMovimiento(MotivoMovimiento motivo) { return useCase.guardarMotivoMovimiento(motivo); }
    public void actualizarMotivoMovimiento(MotivoMovimiento motivo) { useCase.actualizarMotivoMovimiento(motivo); }
    public String eliminarMotivoMovimiento(String id) { return useCase.eliminarMotivoMovimiento(id); }
}
