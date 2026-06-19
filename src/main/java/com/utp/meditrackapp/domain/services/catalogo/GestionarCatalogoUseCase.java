package com.utp.meditrackapp.domain.services.catalogo;

import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.ports.out.CategoriaRepository;
import com.utp.meditrackapp.domain.ports.out.MotivoMovimientoRepository;
import com.utp.meditrackapp.domain.ports.out.RolRepository;
import com.utp.meditrackapp.domain.ports.out.TipoMovimientoRepository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Caso de uso: Gestionar catálogos (roles, categorías, tipos y motivos de movimiento).
 */
public class GestionarCatalogoUseCase {
    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MotivoMovimientoRepository motivoMovimientoRepository;

    public GestionarCatalogoUseCase(RolRepository rolRepository, CategoriaRepository categoriaRepository,
                                     TipoMovimientoRepository tipoMovimientoRepository, MotivoMovimientoRepository motivoMovimientoRepository) {
        this.rolRepository = rolRepository;
        this.categoriaRepository = categoriaRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.motivoMovimientoRepository = motivoMovimientoRepository;
    }

    // === Roles ===
    public List<Rol> listarRoles() { return rolRepository.findAll(); }
    public Optional<Rol> buscarRolPorId(String id) { return rolRepository.findById(id); }
    public String guardarRol(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) return "El nombre es obligatorio.";
        try { rolRepository.save(rol); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public void actualizarRol(Rol rol) { rolRepository.update(rol); }
    public String toggleEstadoRol(String id) {
        try { rolRepository.toggleEstado(id); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public int contarUsuariosPorRol(String rolId) {
        return rolRepository.countUsersByRole(rolId);
    }
    public String eliminarRol(String id) {
        int users = rolRepository.countUsersByRole(id);
        if (users > 0) return "No se puede eliminar: tiene " + users + " usuario(s) asignado(s).";
        try { rolRepository.delete(id); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    // === Categorías ===
    public List<Categoria> listarCategorias() { return categoriaRepository.findAll(); }
    public Optional<Categoria> buscarCategoriaPorId(String id) { return categoriaRepository.findById(id); }
    public String guardarCategoria(Categoria cat) {
        if (cat.getNombre() == null || cat.getNombre().trim().isEmpty()) return "El nombre es obligatorio.";
        try { categoriaRepository.save(cat); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public void actualizarCategoria(Categoria cat) { categoriaRepository.update(cat); }
    public String toggleEstadoCategoria(String id) {
        try { categoriaRepository.toggleEstado(id); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public String eliminarCategoria(String id) {
        try { categoriaRepository.delete(id); return "OK"; } catch (Exception e) { return traducirErrorFK(e, "categoría", "está siendo usada por uno o más productos"); }
    }

    // === Tipos de Movimiento ===
    public List<TipoMovimiento> listarTiposMovimiento() { return tipoMovimientoRepository.findAll(); }
    public String guardarTipoMovimiento(TipoMovimiento tipo) {
        if (tipo.getNombre() == null || tipo.getNombre().trim().isEmpty()) return "El nombre es obligatorio.";
        try { tipoMovimientoRepository.save(tipo); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public void actualizarTipoMovimiento(TipoMovimiento tipo) { tipoMovimientoRepository.update(tipo); }
    public String eliminarTipoMovimiento(String id) {
        try { tipoMovimientoRepository.delete(id); return "OK"; } catch (Exception e) { return traducirErrorFK(e, "tipo de movimiento", "está asociado a uno o más movimientos"); }
    }

    // === Motivos de Movimiento ===
    public List<MotivoMovimiento> listarMotivosMovimiento() { return motivoMovimientoRepository.findAll(); }
    public String guardarMotivoMovimiento(MotivoMovimiento motivo) {
        if (motivo.getNombre() == null || motivo.getNombre().trim().isEmpty()) return "El nombre es obligatorio.";
        try { motivoMovimientoRepository.save(motivo); return "OK"; } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public void actualizarMotivoMovimiento(MotivoMovimiento motivo) { motivoMovimientoRepository.update(motivo); }
    public String eliminarMotivoMovimiento(String id) {
        try { motivoMovimientoRepository.delete(id); return "OK"; } catch (Exception e) { return traducirErrorFK(e, "motivo de movimiento", "está asociado a uno o más movimientos"); }
    }

    private String traducirErrorFK(Exception e, String entidad, String causa) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("FK_") || msg.contains("REFERENCE") || msg.contains("constraint"))) {
            return "No se puede eliminar: " + entidad + " " + causa + ".";
        }
        return "Error al eliminar " + entidad + ": " + msg;
    }
}
