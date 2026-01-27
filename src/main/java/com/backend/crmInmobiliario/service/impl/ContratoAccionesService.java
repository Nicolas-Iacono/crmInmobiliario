package com.backend.crmInmobiliario.service.impl;
import com.backend.crmInmobiliario.DTO.entrada.renovaciones.RenovarContratoRequest;

import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.mapper.ContratoMapper;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoAccionesService {

    private final ContratoRepository contratoRepository;
    private final GaranteRepository garanteRepository;
    private final NotificacionRepository notificacionRepository;
    private final ContratoMapper contratoMapper;

    @Transactional
    public void finalizar(Long contratoId) {
        Contrato c = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        c.setEstado(EstadoContrato.FINALIZADO);
        c.setActivo(false);

        if (c.getEstados() != null) {
            c.getEstados().add(EstadoContrato.FINALIZADO);
        }

        resolverNotifsPendientes(contratoId);

        contratoRepository.save(c);
    }

    // ✅ Método que usa el controller: recibe record
    @Transactional
    public ContratoSalidaDto renovarContrato(Long contratoId, RenovarContratoRequest req) {
        return renovarContrato(
                contratoId,
                req.getFechaInicio(),
                req.getFechaFin(),
                req.getGarantesIds(),
                req
        );
    }

    // ✅ Método real
    @Transactional
    public ContratoSalidaDto renovarContrato(
            Long contratoId,
            LocalDate nuevaFechaInicio,
            LocalDate nuevaFechaFin,
            List<Long> garantesIds,
            RenovarContratoRequest req
    ) {

        Contrato contratoViejo = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        // 1) Viejo -> RENOVADO
        contratoViejo.setEstado(EstadoContrato.RENOVADO);
        contratoViejo.setActivo(false);

        if (contratoViejo.getEstados() != null) {
            contratoViejo.getEstados().add(EstadoContrato.RENOVADO);
        }

        contratoRepository.save(contratoViejo);

        // 2) Nuevo contrato
        Contrato nuevo = new Contrato();

        // Copia base
        nuevo.setNombreContrato(contratoViejo.getNombreContrato());
        nuevo.setUsuario(contratoViejo.getUsuario());
        nuevo.setPdfContratoTexto(contratoViejo.getPdfContratoTexto());

        nuevo.setPropietario(contratoViejo.getPropietario());
        nuevo.setInquilino(contratoViejo.getInquilino());
        nuevo.setPropiedad(contratoViejo.getPropiedad());

        // Servicios (si vienen null y querés copiar del viejo, decímelo; acá dejo lo que viene del request)
        nuevo.setAguaEmpresa(req.getAguaEmpresa());
        nuevo.setAguaPorcentaje(req.getAguaPorcentaje());
        nuevo.setLuzEmpresa(req.getLuzEmpresa());
        nuevo.setLuzPorcentaje(req.getLuzPorcentaje());
        nuevo.setGasEmpresa(req.getGasEmpresa());
        nuevo.setGasPorcentaje(req.getGasPorcentaje());
        nuevo.setMunicipalEmpresa(req.getMunicipalEmpresa());
        nuevo.setMunicipalPorcentaje(req.getAguaPorcentaje());

        // Campos actualizables (fallback al viejo si viene null)
        nuevo.setTipoGarantia(req.getTipoGarantia() != null ? req.getTipoGarantia() : contratoViejo.getTipoGarantia());
        nuevo.setActualizacion(req.getActualizacion() != null ? req.getActualizacion() : contratoViejo.getActualizacion());
        nuevo.setMontoAlquiler(req.getMontoAlquiler() != null ? req.getMontoAlquiler() : contratoViejo.getMontoAlquiler());
        nuevo.setMontoAlquilerLetras(req.getMontoAlquilerLetras() != null ? req.getMontoAlquilerLetras() : contratoViejo.getMontoAlquilerLetras());
        nuevo.setMultaXDia(req.getMultaXDia() != null ? req.getMultaXDia() : contratoViejo.getMultaXDia());
        nuevo.setIndiceAjuste(req.getIndiceAjuste() != null ? req.getIndiceAjuste() : contratoViejo.getIndiceAjuste());
        nuevo.setDuracion(req.getDuracion() != null ? req.getDuracion() : contratoViejo.getDuracion());
        nuevo.setDestino(req.getDestino() != null ? req.getDestino() : contratoViejo.getDestino());

        // Comisiones (fallback al viejo si viene null)
        nuevo.setComisionContratoPorc(req.getComisionContratoPorc() != null ? req.getComisionContratoPorc() : contratoViejo.getComisionContratoPorc());
        nuevo.setComisionMensualPorc(req.getComisionMensualPor() != null ? req.getComisionContratoPorc() : contratoViejo.getComisionMensualPorc());

        // Fechas (con fallback como vos comentaste)
        LocalDate inicio = (nuevaFechaInicio != null) ? nuevaFechaInicio : contratoViejo.getFecha_inicio();
        LocalDate fin = (nuevaFechaFin != null) ? nuevaFechaFin : contratoViejo.getFecha_fin().plusDays(1);

        nuevo.setFecha_inicio(inicio);
        nuevo.setFecha_fin(fin);

        // Estado nuevo
        nuevo.setEstado(EstadoContrato.ACTIVO);
        nuevo.setActivo(true);

        if (nuevo.getEstados() != null) {
            nuevo.getEstados().add(EstadoContrato.ACTIVO);
        }

        // 3) Garantes según semántica del request
        nuevo.setGarantes(resolverGarantes(garantesIds, contratoViejo, nuevo));

        // 4) Guardar y devolver
        Contrato guardado = contratoRepository.save(nuevo);
        return contratoMapper.toContratoSalidaDto(guardado);
    }

    /**
     * garantesIds:
     * - null  => clonar garantes viejos
     * - []    => sin garantes
     * - [ids] => buscar esos ids y clonarlos
     */
    private List<Garante> resolverGarantes(List<Long> garantesIds, Contrato contratoViejo, Contrato contratoNuevo) {
        if (garantesIds == null) {
            return duplicarGarantes(contratoViejo.getGarantes(), contratoNuevo);
        }

        if (garantesIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Garante> seleccionados = new ArrayList<>();
        for (Long id : garantesIds) {
            Garante g = garanteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Garante no encontrado: " + id));

            seleccionados.add(clonarGarante(g, contratoNuevo));
        }
        return seleccionados;
    }

    private List<Garante> duplicarGarantes(List<Garante> garantesViejos, Contrato contratoNuevo) {
        List<Garante> garantes = new ArrayList<>();
        if (garantesViejos == null) return garantes;

        for (Garante g : garantesViejos) {
            garantes.add(clonarGarante(g, contratoNuevo));
        }
        return garantes;
    }

    private Garante clonarGarante(Garante g, Contrato contratoNuevo) {
        Garante ng = new Garante();

        ng.setPronombre(g.getPronombre());
        ng.setNombre(g.getNombre());
        ng.setApellido(g.getApellido());
        ng.setTelefono(g.getTelefono());
        ng.setEmail(g.getEmail());
        ng.setDni(g.getDni());
        ng.setCuit(g.getCuit());
        ng.setDireccionResidencial(g.getDireccionResidencial());
        ng.setNacionalidad(g.getNacionalidad());
        ng.setEstadoCivil(g.getEstadoCivil());

        ng.setTipoGarantia(g.getTipoGarantia());
        ng.setNombreEmpresa(g.getNombreEmpresa());
        ng.setSectorActual(g.getSectorActual());
        ng.setCargoActual(g.getCargoActual());
        ng.setLegajo(g.getLegajo());
        ng.setCuitEmpresa(g.getCuitEmpresa());

        ng.setPartidaInmobiliaria(g.getPartidaInmobiliaria());
        ng.setDireccion(g.getDireccion());
        ng.setInfoCatastral(g.getInfoCatastral());
        ng.setEstadoOcupacion(g.getEstadoOcupacion());
        ng.setTipoPropiedad(g.getTipoPropiedad());
        ng.setInformeDominio(g.getInformeDominio());
        ng.setInformeInhibicion(g.getInformeInhibicion());

        ng.setContrato(contratoNuevo);

        // si querés conservar el usuario del garante:
        ng.setUsuario(g.getUsuario());

        return ng;
    }

    private void resolverNotifsPendientes(Long contratoId) {
        List<Notificacion> all = notificacionRepository.findAll();

        for (Notificacion n : all) {
            if (n.getContrato() != null
                    && n.getContrato().getId().equals(contratoId)
                    && n.getEstado() == EstadoNotificacion.PENDIENTE) {

                n.setEstado(EstadoNotificacion.RESUELTA);
                notificacionRepository.save(n);
            }
        }
    }
}
