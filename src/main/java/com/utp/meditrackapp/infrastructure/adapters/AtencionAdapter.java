package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.application.dto.DispensacionReportDTO;
import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;
import com.utp.meditrackapp.domain.ports.out.DispensacionRepository;
import com.utp.meditrackapp.domain.services.dispensacion.DispensarMedicamentoUseCase;
import com.utp.meditrackapp.domain.services.dispensacion.EditarAtencionUseCase;
import com.utp.meditrackapp.domain.services.dispensacion.EliminarAtencionUseCase;
import com.utp.meditrackapp.domain.services.paciente.GestionarPacienteUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcAtencionRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcDispensacionRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcLoteRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMovimientoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcPacienteRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcProductoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador para AtencionController.
 * Delega a los casos de uso del dominio.
 */
public class AtencionAdapter {
    private final DispensarMedicamentoUseCase dispensarUseCase;
    private final EditarAtencionUseCase editarAtencionUseCase;
    private final EliminarAtencionUseCase eliminarAtencionUseCase;
    private final GestionarPacienteUseCase pacienteUseCase;
    private final JdbcAtencionRepository atencionRepository;
    private final JdbcDispensacionRepository dispensacionRepository;
    private final JdbcProductoRepository productoRepository;
    private final JdbcLoteRepository loteRepository;

    public AtencionAdapter() {
        this.atencionRepository = new JdbcAtencionRepository();
        JdbcPacienteRepository pacienteRepo = new JdbcPacienteRepository();
        this.dispensarUseCase = new DispensarMedicamentoUseCase(
            atencionRepository,
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
        this.editarAtencionUseCase = new EditarAtencionUseCase(
            atencionRepository,
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
        this.eliminarAtencionUseCase = new EliminarAtencionUseCase(
            atencionRepository,
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
        this.pacienteUseCase = new GestionarPacienteUseCase(pacienteRepo, atencionRepository);
        this.dispensacionRepository = new JdbcDispensacionRepository();
        this.productoRepository = new JdbcProductoRepository();
        this.loteRepository = new JdbcLoteRepository();
    }

    public DispensacionReportDTO[] listarDispensacionesReporte(String sedeId, LocalDate desde, LocalDate hasta) {
        List<?> raw = dispensacionRepository.findByRangoFechas(sedeId, desde, hasta);
        return raw.stream().map(this::toDispensacionDTO).toArray(DispensacionReportDTO[]::new);
    }

    public List<Atencion> buscarHistorialPorReceta(String sedeId, String receta) {
        return dispensarUseCase.buscarHistorialPorReceta(sedeId, receta);
    }

    public List<Atencion> buscarHistorialPorPaciente(String pacienteId) {
        return dispensarUseCase.buscarHistorialPorPaciente(pacienteId);
    }

    public List<AtencionDetalle> sugerirDispensacion(String sedeId, String productoId, int cantidad) {
        return dispensarUseCase.sugerirDispensacion(sedeId, productoId, cantidad);
    }

    public String registrarAtencion(Atencion atencion, List<AtencionDetalle> detalles) {
        return dispensarUseCase.registrarAtencion(atencion, detalles);
    }

    public List<Producto> listarProductosActivos() {
        return com.utp.meditrackapp.core.cache.ReferenceCacheManager.getInstance()
            .get(com.utp.meditrackapp.core.cache.ReferenceCacheManager.CacheType.PRODUCTOS,
                 () -> productoRepository.findActivos());
    }

    public List<Lote> listarLotesConProducto(String sedeId) {
        return loteRepository.findBySede(sedeId);
    }

    public List<Lote> listarLotesFefo(String sedeId, String productoId) {
        return loteRepository.findFefo(sedeId, productoId);
    }

    public List<String> listarMedicosDistinct() {
        return atencionRepository.findMedicosDistinct();
    }

    public String generarNumeroReceta(String sedeId) {
        int year = java.time.Year.now().getValue();
        int count = atencionRepository.countBySedeAndYear(sedeId, year);
        return String.format("REC-%d-%04d", year, count + 1);
    }

    public boolean existeReceta(String sedeId, String numeroReceta) {
        return atencionRepository.existeReceta(sedeId, numeroReceta);
    }

    public List<Paciente> buscarPacientes(String query) {
        return pacienteUseCase.buscarPacientes(query);
    }

    public List<Paciente> buscarPacientesTypeahead(String query) {
        return pacienteUseCase.buscarPacientesTypeahead(query);
    }

    public List<AtencionDetalle> buscarDetallesAtencion(String atencionId) {
        return atencionRepository.findDetallesByAtencionId(atencionId);
    }

    public String editarAtencion(Atencion atencion) {
        atencionRepository.updateAtencion(atencion);
        return "OK";
    }

    public String editarAtencionCompleta(Atencion atencion, List<AtencionDetalle> originales, List<AtencionDetalle> nuevas) {
        return editarAtencionUseCase.editarAtencion(atencion, originales, nuevas);
    }

    public void editarDetalle(String detalleId, String nuevoLoteId, int nuevaCantidad) {
        atencionRepository.updateDetalle(detalleId, nuevoLoteId, nuevaCantidad);
    }

    public String eliminarAtencion(String atencionId) {
        com.utp.meditrackapp.domain.entities.Usuario user = com.utp.meditrackapp.core.config.SessionManager.getInstance().getCurrentUser();
        return eliminarAtencionUseCase.eliminar(atencionId, user.getSedeId(), user.getId());
    }

    private DispensacionReportDTO toDispensacionDTO(Object raw) {
        if (raw instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;
            Object fechaObj = map.get("fecha");
            java.time.LocalDate fecha = fechaObj instanceof java.time.LocalDate ? (java.time.LocalDate) fechaObj :
                fechaObj instanceof java.time.LocalDateTime ? ((java.time.LocalDateTime) fechaObj).toLocalDate() : null;
            return new DispensacionReportDTO(
                fecha,
                (String) map.get("paciente"),
                (String) map.get("numeroReceta"),
                (String) map.get("producto"),
                (String) map.get("lote"),
                ((Number) map.get("cantidad")).intValue()
            );
        }
        return null;
    }
}
