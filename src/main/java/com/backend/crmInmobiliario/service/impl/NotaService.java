package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.NotaEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.NotaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.impuestos.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.NotaRepository;
import com.backend.crmInmobiliario.service.INotaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Not;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotaService implements INotaService {

    private final Logger LOGGER = LoggerFactory.getLogger(NotaService.class);
    private ModelMapper modelMapper;
    private NotaRepository notaRepository;
    private ContratoRepository contratoRepository;
    public NotaService(ModelMapper modelMapper, NotaRepository notaRepository, ContratoRepository contratoRepository){
        this.modelMapper = modelMapper;
        this.notaRepository = notaRepository;
        this.contratoRepository = contratoRepository;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);
//
//        modelMapper.typeMap(NotaEntradaDto.class, Nota.class)
//                .addMapping(NotaEntradaDto::getIdContrato, Nota::setContrato)
//                .addMapping(NotaEntradaDto::getEstado, Nota::setEstado)
//                .addMapping(NotaEntradaDto::getContenido, Nota::setContenido)
//                .addMapping(NotaEntradaDto::getTipo, Nota::setTipo)
//                .addMapping(NotaEntradaDto::getMotivo, Nota::setMotivo)
//                .addMapping(NotaEntradaDto::getObservaciones, Nota::setObservaciones)
//                .addMapping(NotaEntradaDto::getPrioridad, Nota::setPrioridad);
//
//        modelMapper.typeMap(Nota.class, NotaSalidaDto.class)
//                .addMapping(Nota::getContrato, NotaSalidaDto::setIdContrato)
//                .addMapping(Nota::getEstado, NotaSalidaDto::setEstado)
//                .addMapping(Nota::getContenido, NotaSalidaDto::setContenido)
//                .addMapping(Nota::getTipo, NotaSalidaDto::setTipo)
//                .addMapping(Nota::getMotivo, NotaSalidaDto::setMotivo)
//                .addMapping(Nota::getObservaciones, NotaSalidaDto::setObservaciones)
//                .addMapping(Nota::getPrioridad, NotaSalidaDto::setPrioridad);
//
//        modelMapper.typeMap(NotaModificacionDto.class, NotaSalidaDto.class)
//                .addMapping(NotaModificacionDto::getId, NotaSalidaDto::setIdContrato)
//                .addMapping(NotaModificacionDto::getEstado, NotaSalidaDto::setEstado)
//                .addMapping(NotaModificacionDto::getTipo, NotaSalidaDto::setTipo)
//                .addMapping(NotaModificacionDto::getPrioridad, NotaSalidaDto::setPrioridad);
    }

    @Override
    @Transactional()
    public List<NotaSalidaDto> listarNotas() {
        LOGGER.info("Iniciando el proceso de listado de notas");
        List<Nota> notas = notaRepository.findAll();
        if (notas.isEmpty()) {
            LOGGER.warn("No se encontraron notas");
            return Collections.emptyList(); // Devuelve una lista vacía en lugar de null
        }
        List<NotaSalidaDto> notaSalidaDto = notas.stream()
                .map(nota -> {
                    NotaSalidaDto dto = modelMapper.map(nota, NotaSalidaDto.class);
                    dto.setIdContrato(nota.getContrato() != null ? nota.getContrato().getId_contrato() : null);

                    if (nota.getFechaCreacion() != null) {
                        dto.setFechaCreacion(nota.getFechaCreacion().toLocalDate());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        LOGGER.info("Se encontraron " + notaSalidaDto.size() + " notas");
        return notaSalidaDto;
    }

    @Override
    @Transactional()
    public NotaSalidaDto crearNota(NotaEntradaDto notaEntradaDto) throws ResourceNotFoundException {

        LOGGER.info("Iniciando el proceso de creación de nota");

        // 1. Buscar el contrato
        Contrato contrato = contratoRepository.findById(notaEntradaDto.getIdContrato())
                .orElseThrow(() -> {
                    LOGGER.error("Contrato no encontrado con ID: " + notaEntradaDto.getIdContrato());
                    return new ResourceNotFoundException("Contrato no encontrado");
                });

        // 2. Crear el recibo
        Nota nota = new Nota();
        nota.setContrato(contrato);
        nota.setMotivo(notaEntradaDto.getMotivo());
        nota.setContenido(notaEntradaDto.getContenido());
        nota.setTipo(notaEntradaDto.getTipo());
        nota.setPrioridad(notaEntradaDto.getPrioridad());
        nota.setEstado(notaEntradaDto.getEstado());
        nota.setObservaciones(notaEntradaDto.getObservaciones());

        // Otros campos del recibo
        LOGGER.info("Motivo: " + notaEntradaDto.getMotivo());
        LOGGER.info("Contenido: " + notaEntradaDto.getContenido());
        LOGGER.info("Tipo: " + notaEntradaDto.getTipo());
        LOGGER.info("Prioridad: " + notaEntradaDto.getPrioridad());
        LOGGER.info("Estado: " + notaEntradaDto.getEstado());
        LOGGER.info("Observaciones: " + notaEntradaDto.getObservaciones());
        // 3. Procesar y agregar los impuestos


        // 4. Guardar el recibo (y los impuestos en cascada)
        Nota notaAPersistir = notaRepository.save(nota);
        LOGGER.info("nota guardado con ID: " + notaAPersistir.getId());
        LOGGER.info("Fecha de creación persistida: " + notaAPersistir.getFechaCreacion());

        // 5. Mapear a DTO de salida
        NotaSalidaDto notaSalidaDto = modelMapper.map(notaAPersistir, NotaSalidaDto.class);
        notaSalidaDto.setIdContrato(notaAPersistir.getContrato().getId_contrato());
        notaSalidaDto.setContenido(notaAPersistir.getContenido());
        notaSalidaDto.setFechaCreacion(notaAPersistir.getFechaCreacion().toLocalDate());
        LOGGER.info("Proceso de creación de nota completado exitosamente");
        return notaSalidaDto;
    }





    @Override
    @Transactional
    public NotaSalidaDto buscarNotaPorId(Long id) throws ResourceNotFoundException {
        Optional<Nota> notaOptional = notaRepository.findById(id);

        if (notaOptional.isPresent()) {
            Nota nota = notaOptional.get();
            NotaSalidaDto dto = modelMapper.map(nota, NotaSalidaDto.class);

            dto.setIdContrato(nota.getContrato() != null ? nota.getContrato().getId_contrato() : null);

            if (nota.getFechaCreacion() != null) {
                dto.setFechaCreacion(nota.getFechaCreacion().toLocalDate());
            }

            return dto;
        } else {
            throw new ResourceNotFoundException("Nota no encontrada con ID: " + id);
        }
    }
    @Override
    public void eliminarNota(Long id) throws ResourceNotFoundException {

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada con el id: " + id));

        notaRepository.delete(nota);
    }

    @Override
    @Transactional()
    public NotaSalidaDto modificarEstado(NotaModificacionDto notaModificacionDto) throws ResourceNotFoundException {
        LOGGER.info("Iniciando la modificación del estado de la nota con ID: " + notaModificacionDto.getId());

        // Buscar el recibo por ID
        Nota nota = notaRepository.findById(notaModificacionDto.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Nota no encontrada con ID: " + notaModificacionDto.getId());
                    return new ResourceNotFoundException("Nota no encontrada con ID: " + notaModificacionDto.getId());
                });

        // Actualizar el estado del recibo
        nota.setEstado(notaModificacionDto.getEstado());

        // Guardar el recibo actualizado
        Nota notaActualizada = notaRepository.save(nota);
        LOGGER.info("Estado de la nota actualizado exitosamente. Nuevo estado: " + notaActualizada.getEstado());

        // Mapear la entidad actualizada a DTO de salida y retornarlo
        return modelMapper.map(notaActualizada, NotaSalidaDto.class);
    }
}
