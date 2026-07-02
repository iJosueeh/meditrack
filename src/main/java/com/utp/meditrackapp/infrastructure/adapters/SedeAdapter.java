package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.services.sede.GestionarSedeUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcSedeRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcUsuarioRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador para SedeController.
 * Delega al GestionarSedeUseCase del dominio.
 */
public class SedeAdapter {
    private final GestionarSedeUseCase useCase;
    private final JdbcSedeRepository sedeRepository;

    public SedeAdapter() {
        this.sedeRepository = new JdbcSedeRepository();
        this.useCase = new GestionarSedeUseCase(
            this.sedeRepository,
            new JdbcUsuarioRepository()
        );
    }

    public List<Sede> listarSedes() {
        List<Sede> sedes = useCase.listarSedes();
        sedes.forEach(s -> s.setTotalEmpleados(sedeRepository.countEmployeesBySede(s.getId())));
        return sedes;
    }

    public List<Sede> listarSedesActivas() {
        return useCase.listarSedesActivas();
    }

    public Optional<Sede> buscarPorId(String id) {
        return useCase.obtenerPorId(id);
    }

    public String guardarSede(Sede sede) {
        return useCase.guardarSede(sede);
    }

    public String toggleEstado(String id) {
        return useCase.toggleEstado(id);
    }

    public String bloquear(String id, String motivo) {
        sedeRepository.bloquear(id, motivo);
        return "OK";
    }

    public String desbloquear(String id) {
        sedeRepository.desbloquear(id);
        return "OK";
    }

    public String asignarUsuarioASede(String usuarioId, String sedeId, String rolId) {
        return useCase.asignarUsuarioASede(usuarioId, sedeId, rolId);
    }

    public List<Usuario> obtenerStaffPorSede(String sedeId) {
        return useCase.obtenerStaffPorSede(sedeId);
    }

    public int contarEmpleadosGlobales() {
        return useCase.contarEmpleadosGlobales();
    }

    public List<Usuario> obtenerAdministradoresDisponibles() {
        return useCase.obtenerAdministradoresDisponibles();
    }
}
