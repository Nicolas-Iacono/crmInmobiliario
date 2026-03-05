package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.visita.VisitaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.visita.VisitaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.visita.VisitaSalidaDto;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Prospecto;
import com.backend.crmInmobiliario.entity.Visita;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.ProspectoRepository;
import com.backend.crmInmobiliario.repository.VisitaRepository;
import com.backend.crmInmobiliario.service.IVisitaService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitaService implements IVisitaService {

    private final VisitaRepository visitaRepository;
    private final PropiedadRepository propiedadRepository;
    private final ProspectoRepository prospectoRepository;
    private final AuthUtil authUtil;

    public VisitaService(VisitaRepository visitaRepository, PropiedadRepository propiedadRepository, ProspectoRepository prospectoRepository, AuthUtil authUtil) {
        this.visitaRepository = visitaRepository;
        this.propiedadRepository = propiedadRepository;
        this.prospectoRepository = prospectoRepository;
        this.authUtil = authUtil;
    }

    @Override
    @Transactional
    public VisitaSalidaDto crearVisita(VisitaEntradaDto dto) throws ResourceNotFoundException {
        Long userId = authUtil.extractUserId();
        Propiedad propiedad = propiedadRepository.findById(dto.getPropiedadId())
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        validarPropiedadDelUsuario(propiedad, userId);

        Visita visita = new Visita();
        visita.setPropiedad(propiedad);
        visita.setTitulo(dto.getTitulo());
        visita.setFecha(dto.getFecha());
        visita.setHora(dto.getHora());
        visita.setAclaracion(dto.getAclaracion());
        visita.setNombreCorredor(dto.getNombreCorredor());

        cargarVisitante(visita, dto.getProspectoId(), dto.getVisitanteNombre(), dto.getVisitanteApellido(), dto.getVisitanteTelefono());

        return mapSalida(visitaRepository.save(visita));
    }

    @Override
    @Transactional
    public VisitaSalidaDto actualizarVisita(Long visitaId, VisitaModificacionDto dto) throws ResourceNotFoundException {
        Long userId = authUtil.extractUserId();

        Visita visita = visitaRepository.findByIdAndPropiedadUsuarioId(visitaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));

        if (dto.getTitulo() != null) visita.setTitulo(dto.getTitulo());
        if (dto.getAclaracion() != null) visita.setAclaracion(dto.getAclaracion());
        if (dto.getNombreCorredor() != null) visita.setNombreCorredor(dto.getNombreCorredor());

        if (dto.getProspectoId() != null || dto.getVisitanteNombre() != null || dto.getVisitanteApellido() != null || dto.getVisitanteTelefono() != null) {
            cargarVisitante(visita, dto.getProspectoId(), dto.getVisitanteNombre(), dto.getVisitanteApellido(), dto.getVisitanteTelefono());
        }

        return mapSalida(visitaRepository.save(visita));
    }

    @Override
    public VisitaSalidaDto buscarVisita(Long visitaId) throws ResourceNotFoundException {
        Long userId = authUtil.extractUserId();
        Visita visita = visitaRepository.findByIdAndPropiedadUsuarioId(visitaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
        return mapSalida(visita);
    }

    @Override
    public List<VisitaSalidaDto> listarVisitasPorPropiedad(Long propiedadId) {
        Long userId = authUtil.extractUserId();

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        validarPropiedadDelUsuario(propiedad, userId);

        return visitaRepository.findByPropiedadId_propiedadOrderByFechaDescHoraDesc(propiedadId)
                .stream()
                .map(this::mapSalida)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarVisita(Long visitaId) throws ResourceNotFoundException {
        Long userId = authUtil.extractUserId();
        Visita visita = visitaRepository.findByIdAndPropiedadUsuarioId(visitaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
        visitaRepository.delete(visita);
    }

    private void cargarVisitante(Visita visita, Long prospectoId, String nombre, String apellido, String telefono) {
        if (prospectoId != null) {
            Prospecto prospecto = prospectoRepository.findById(prospectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
            visita.setProspectoVisitante(prospecto);
            visita.setVisitanteNombre(null);
            visita.setVisitanteApellido(null);
            visita.setVisitanteTelefono(null);
            return;
        }

        if (isBlank(nombre) || isBlank(apellido) || isBlank(telefono)) {
            throw new IllegalArgumentException("Debes elegir un prospecto o informar nombre, apellido y teléfono del visitante");
        }

        visita.setProspectoVisitante(null);
        visita.setVisitanteNombre(nombre);
        visita.setVisitanteApellido(apellido);
        visita.setVisitanteTelefono(telefono);
    }

    private void validarPropiedadDelUsuario(Propiedad propiedad, Long userId) {
        if (propiedad.getUsuario() == null || !propiedad.getUsuario().getId().equals(userId)) {
            throw new AccessDeniedException("No tenés permisos para gestionar visitas de esta propiedad");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private VisitaSalidaDto mapSalida(Visita visita) {
        VisitaSalidaDto salida = new VisitaSalidaDto();
        salida.setId(visita.getId());
        salida.setPropiedadId(visita.getPropiedad().getId_propiedad());
        salida.setTitulo(visita.getTitulo());
        salida.setFecha(visita.getFecha());
        salida.setHora(visita.getHora());
        salida.setAclaracion(visita.getAclaracion());
        salida.setNombreCorredor(visita.getNombreCorredor());
        salida.setVisitanteNombre(visita.getVisitanteNombre());
        salida.setVisitanteApellido(visita.getVisitanteApellido());
        salida.setVisitanteTelefono(visita.getVisitanteTelefono());

        if (visita.getProspectoVisitante() != null) {
            salida.setProspectoId(visita.getProspectoVisitante().getId());
            salida.setProspectoNombreCompleto(
                    visita.getProspectoVisitante().getNombre() + " " + visita.getProspectoVisitante().getApellido()
            );
        }

        return salida;
    }
}
