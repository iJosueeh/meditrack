package com.utp.meditrackapp.features.attentions.service;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.features.attentions.repository.AtencionRepository;
import com.utp.meditrackapp.features.attentions.repository.AtencionRepositoryImpl;

import java.util.List;

/**
 * Servicio para orquestar el registro de Atenciones con transaccionalidad.
 */
public class AtencionService {
    private final AtencionRepository atencionRepository;
    private final SessionManager sessionManager;

    public AtencionService() {
        this.atencionRepository = new AtencionRepositoryImpl();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Registra una atención médica validando la integridad de los datos y permisos de rol.
     */
    public String registrarAtencion(Atencion atencion, List<AtencionDetalle> detalles) {
        // 1. Validación de Sesión y Roles
        if (!sessionManager.isLoggedIn()) {
            return "Sesión no iniciada.";
        }

        Usuario user = sessionManager.getCurrentUser();
        // Validar que el rol sea apto para dispensación (Admin, Químico o Técnico)
        boolean isAuthorized = sessionManager.isAdmin() || sessionManager.isQuimico() || sessionManager.isTecnico();
        
        if (!isAuthorized) {
            return "No tiene permisos para realizar esta operación. Su rol: " + user.getRolNombre();
        }

        // 2. Autocompletar datos de la sesión
        atencion.setUsuarioId(user.getId());
        atencion.setSedeId(user.getSedeId());

        // 3. Validaciones de negocio
        if (atencion.getPacienteId() == null || atencion.getPacienteId().isEmpty()) {
            return "El paciente es obligatorio.";
        }
        if (detalles == null || detalles.isEmpty()) {
            return "Debe agregar al menos un medicamento a la atención.";
        }
        if (atencion.getNumeroReceta() == null || atencion.getNumeroReceta().trim().isEmpty()) {
            return "El número de receta es obligatorio.";
        }

        // 4. Ejecutar transacción ACID
        boolean exito = atencionRepository.registrarAtencionCompleta(atencion, detalles);

        return exito ? "OK" : "Error al registrar la atención. Verifique stock de los lotes seleccionados.";
    }

    public List<Atencion> listarHistorial() {
        return atencionRepository.findAll();
    }
}
