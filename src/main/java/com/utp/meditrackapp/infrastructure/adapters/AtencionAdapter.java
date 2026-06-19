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
    private final GestionarPacienteUseCase pacienteUseCase;
    private final JdbcDispensacionRepository dispensacionRepository;
    private final JdbcProductoRepository productoRepository;
    private final JdbcLoteRepository loteRepository;

    public AtencionAdapter() {
        AtencionRepository atencionRepo = new JdbcAtencionRepository();
        JdbcPacienteRepository pacienteRepo = new JdbcPacienteRepository();
        this.dispensarUseCase = new DispensarMedicamentoUseCase(
            atencionRepo,
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
        this.pacienteUseCase = new GestionarPacienteUseCase(pacienteRepo, atencionRepo);
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
        return productoRepository.findActivos();
    }

    public List<Lote> listarLotesConProducto(String sedeId) {
        return loteRepository.findBySede(sedeId);
    }

    public List<Paciente> buscarPacientes(String query) {
        return pacienteUseCase.buscarPacientes(query);
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
