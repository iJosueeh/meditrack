package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.core.cache.ReferenceCacheManager;
import com.utp.meditrackapp.core.cache.ReferenceCacheManager.CacheType;
import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.services.catalogo.GestionarCatalogoUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcCategoriaRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMotivoMovimientoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcPermisoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcTipoMovimientoRepository;

import java.util.List;

public class CatalogAdapter {
    private final GestionarCatalogoUseCase useCase;
    private final JdbcPermisoRepository permisoRepository;
    private final ReferenceCacheManager cache = ReferenceCacheManager.getInstance();

    public CatalogAdapter() {
        this.useCase = new GestionarCatalogoUseCase(
            new JdbcRolRepository(),
            new JdbcCategoriaRepository(),
            new JdbcTipoMovimientoRepository(),
            new JdbcMotivoMovimientoRepository()
        );
        this.permisoRepository = new JdbcPermisoRepository();
    }

    public List<Rol> listarRoles() { return useCase.listarRoles(); }
    public String crearRol(Rol rol) { return useCase.guardarRol(rol); }
    public void actualizarRol(Rol rol) { useCase.actualizarRol(rol); }
    public String toggleEstadoRol(String id) { return useCase.toggleEstadoRol(id); }
    public int contarUsuariosPorRol(String rolId) {
        return useCase.contarUsuariosPorRol(rolId);
    }
    public String eliminarRol(String id) { return useCase.eliminarRol(id); }

    public void guardarPermisosRol(String rolId, List<String> permisoIds) {
        permisoRepository.saveRolPermisos(rolId, permisoIds);
    }

    public List<Categoria> listarCategorias() {
        return cache.get(CacheType.CATEGORIAS, () -> useCase.listarCategorias());
    }
    public String crearCategoria(Categoria cat) {
        String r = useCase.guardarCategoria(cat);
        if ("OK".equals(r)) cache.invalidate(CacheType.CATEGORIAS);
        return r;
    }
    public void actualizarCategoria(Categoria cat) {
        useCase.actualizarCategoria(cat);
        cache.invalidate(CacheType.CATEGORIAS);
    }
    public String toggleEstadoCategoria(String id) {
        String r = useCase.toggleEstadoCategoria(id);
        cache.invalidate(CacheType.CATEGORIAS);
        return r;
    }
    public String eliminarCategoria(String id) {
        String r = useCase.eliminarCategoria(id);
        if ("OK".equals(r)) cache.invalidate(CacheType.CATEGORIAS);
        return r;
    }

    public List<TipoMovimiento> listarTiposMovimiento() {
        return cache.get(CacheType.TIPOS_MOVIMIENTO, () -> useCase.listarTiposMovimiento());
    }
    public String crearTipoMovimiento(TipoMovimiento tipo) {
        String r = useCase.guardarTipoMovimiento(tipo);
        if ("OK".equals(r)) cache.invalidate(CacheType.TIPOS_MOVIMIENTO);
        return r;
    }
    public void actualizarTipoMovimiento(TipoMovimiento tipo) {
        useCase.actualizarTipoMovimiento(tipo);
        cache.invalidate(CacheType.TIPOS_MOVIMIENTO);
    }
    public String eliminarTipoMovimiento(String id) {
        String r = useCase.eliminarTipoMovimiento(id);
        if ("OK".equals(r)) cache.invalidate(CacheType.TIPOS_MOVIMIENTO);
        return r;
    }

    public List<MotivoMovimiento> listarMotivosMovimiento() {
        return cache.get(CacheType.MOTIVOS_MOVIMIENTO, () -> useCase.listarMotivosMovimiento());
    }
    public String crearMotivoMovimiento(MotivoMovimiento motivo) {
        String r = useCase.guardarMotivoMovimiento(motivo);
        if ("OK".equals(r)) cache.invalidate(CacheType.MOTIVOS_MOVIMIENTO);
        return r;
    }
    public void actualizarMotivoMovimiento(MotivoMovimiento motivo) {
        useCase.actualizarMotivoMovimiento(motivo);
        cache.invalidate(CacheType.MOTIVOS_MOVIMIENTO);
    }
    public String eliminarMotivoMovimiento(String id) {
        String r = useCase.eliminarMotivoMovimiento(id);
        if ("OK".equals(r)) cache.invalidate(CacheType.MOTIVOS_MOVIMIENTO);
        return r;
    }
}
