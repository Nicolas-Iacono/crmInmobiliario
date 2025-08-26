package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestosGeneralSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.impuestos.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.ReciboRepository;
import com.backend.crmInmobiliario.service.IReciboService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReciboService implements IReciboService {
    private final Logger LOGGER = LoggerFactory.getLogger(ReciboService.class);
    private ModelMapper modelMapper;
    private ReciboRepository reciboRepository;
    private ContratoRepository contratoRepository;
    private InquilinoRepository inquilinoRepository;

    public ReciboService(ModelMapper modelMapper, ReciboRepository reciboRepository, ContratoRepository contratoRepository, InquilinoRepository inquilinoRepository) {
        this.modelMapper = modelMapper;
        this.reciboRepository = reciboRepository;
        this.contratoRepository = contratoRepository;
        this.inquilinoRepository = inquilinoRepository;
        configureMapping();
    }

    private void configureMapping() {

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);



//        // Verifica si ya existe el TypeMap para evitar duplicados
//        if (modelMapper.getTypeMap(PersistentBag.class, List.class) == null) {
//            modelMapper.createTypeMap(PersistentBag.class, List.class).setConverter(context -> {
//                PersistentBag source = (PersistentBag) context.getSource();
//                return source == null ? null : new ArrayList<>(source);
//            });
//        }

        modelMapper.typeMap(ReciboEntradaDto.class, Recibo.class)
                .addMapping(ReciboEntradaDto::getIdContrato, Recibo::setContrato)
                .addMapping(ReciboEntradaDto::getEstado, Recibo::setEstado)
                .addMapping(ReciboEntradaDto::getConcepto, Recibo::setConcepto)
                .addMapping(ReciboEntradaDto::getFechaEmision, Recibo::setFechaEmision)
                .addMapping(ReciboEntradaDto::getFechaVencimiento, Recibo::setFechaVencimiento)
                .addMapping(ReciboEntradaDto::getMontoTotal, Recibo::setMontoTotal)
                .addMapping(ReciboEntradaDto::getPeriodo, Recibo::setPeriodo)
                .addMapping(ReciboEntradaDto::getImpuestos, Recibo::setImpuestos)
                .addMapping(ReciboEntradaDto::getNumeroRecibo, Recibo::setNumeroRecibo);



        modelMapper.typeMap(Recibo.class, ReciboSalidaDto.class)
                .addMapping(Recibo::getId, ReciboSalidaDto::setId)
                .addMapping(Recibo::getContrato, ReciboSalidaDto::setContratoId)
                .addMapping(Recibo::getFechaEmision, ReciboSalidaDto::setFechaEmision)
                .addMapping(Recibo::getFechaVencimiento, ReciboSalidaDto::setFechaVencimiento)
                .addMapping(Recibo::getPeriodo, ReciboSalidaDto::setPeriodo)
                .addMapping(Recibo::getConcepto, ReciboSalidaDto::setConcepto)
                .addMapping(Recibo::getNumeroRecibo, ReciboSalidaDto::setNumeroRecibo);

        modelMapper.typeMap(ReciboModificacionDto.class, ReciboSalidaDto.class)
                .addMapping(ReciboModificacionDto::getEstado, ReciboSalidaDto::setEstado);

    }


    @Override
    @Transactional() // Mejora el rendimiento para operaciones de solo lectura
    public List<ReciboSalidaDto> listarRecibos() {
        LOGGER.info("Iniciando el proceso de listado de recibos");
        List<Recibo> recibos = reciboRepository.findAll();
        if (recibos.isEmpty()) {
            LOGGER.warn("No se encontraron recibos");
            return Collections.emptyList(); // Devuelve una lista vacía en lugar de null
        }
        List<ReciboSalidaDto> recibosSalidaDto = recibos.stream()
                .map(recibo -> modelMapper.map(recibo, ReciboSalidaDto.class))
                .collect(Collectors.toList());
        LOGGER.info("Se encontraron " + recibosSalidaDto.size() + " recibos");
        return recibosSalidaDto;
    }

    @Override
    @Transactional // Importante para la consistencia de la transacción
    public ReciboSalidaDto crearRecibo(ReciboEntradaDto reciboEntradaDto) throws ResourceNotFoundException {
        LOGGER.info("Iniciando el proceso de creación de recibo");

        // 1. Buscar el contrato
        Contrato contrato = contratoRepository.findById(reciboEntradaDto.getIdContrato())
                .orElseThrow(() -> {
                    LOGGER.error("Contrato no encontrado con ID: " + reciboEntradaDto.getIdContrato());
                    return new ResourceNotFoundException("Contrato no encontrado");
                });

        // 2. Crear el recibo
        Recibo recibo = new Recibo();
        recibo.setContrato(contrato);
        recibo.setMontoTotal(reciboEntradaDto.getMontoTotal());
        recibo.setConcepto(reciboEntradaDto.getConcepto());
        recibo.setPeriodo(reciboEntradaDto.getPeriodo());
        recibo.setFechaEmision(reciboEntradaDto.getFechaEmision());
        recibo.setFechaVencimiento(reciboEntradaDto.getFechaVencimiento());
        recibo.setNumeroRecibo(reciboEntradaDto.getNumeroRecibo());
        // Otros campos del recibo

        // 3. Procesar y agregar los impuestos
        if (reciboEntradaDto.getImpuestos() != null && !reciboEntradaDto.getImpuestos().isEmpty()) {
            for (ImpuestoEntradaDto impuestoDTO : reciboEntradaDto.getImpuestos()) {
                Impuesto impuesto = convertToImpuesto(impuestoDTO); // Usar el método convertToImpuesto
                impuesto.setRecibo(recibo); // Establecer la relación inversa
                recibo.getImpuestos().add(impuesto);
                LOGGER.info("Impuesto agregado al recibo: " + impuesto.getTipoImpuesto());
            }
        } else {
            LOGGER.warn("No se proporcionaron impuestos para el recibo");
            // Considerar si lanzar una excepción aquí es apropiado para tu lógica de negocio
        }

        // 4. Guardar el recibo (y los impuestos en cascada)
        Recibo reciboGuardado = reciboRepository.save(recibo);
        LOGGER.info("Recibo guardado con ID: " + reciboGuardado.getId());

        // 5. Mapear a DTO de salida
        ReciboSalidaDto reciboSalidaDto = modelMapper.map(reciboGuardado, ReciboSalidaDto.class);
        LOGGER.info("Proceso de creación de recibo completado exitosamente");
        return reciboSalidaDto;
    }

    // Método para convertir ImpuestoEntradaDto a Impuesto (como se definió anteriormente)
    private Impuesto convertToImpuesto(ImpuestoEntradaDto dto) {
        // Primero, verifica si el tipo de impuesto es nulo
        if (dto.getTipoImpuesto() == null || dto.getTipoImpuesto().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de impuesto no puede ser nulo o vacío");
        }
        LOGGER.info("---------------------------------------------------------------------------------------");
        LOGGER.info("TIPO IMPUESTO: " + dto.getTipoImpuesto());
        LOGGER.info("---------------------------------------------------------------------------------------");

        // Convierte el tipo de impuesto a mayúsculas para la comparación
        String tipoImpuestoMayus = dto.getTipoImpuesto().toUpperCase();
        Impuesto impuesto;

        // Utiliza un switch para crear la instancia del tipo de impuesto correcto
        switch (tipoImpuestoMayus) {
            case "AGUA":
                impuesto = new Agua();
                break;
            case "GAS":
                impuesto = new Gas();
                break;
            case "LUZ":
                impuesto = new Luz();
                break;
            case "MUNICIPAL":
                impuesto = new Municipal();
                break;
            case "EXP_ORD":
                impuesto = new ExpensaOrdinaria();
                break;
            case "EXP_EXT_ORD":
                impuesto = new ExpensaExtraOrdinaria();
                break;
            case "DEUDA_PENDIENTE":
                impuesto = new DeudaPendiente();
                break;
            case "OTRO":
                impuesto = new Otro();
                break;
            default:
                throw new IllegalArgumentException("Tipo de impuesto no soportado: " + dto.getTipoImpuesto());
        }

        // Mapea los campos comunes del DTO al objeto Impuesto
        impuesto.setTipoImpuesto(dto.getTipoImpuesto()); // Asigna el valor original, no la versión en mayúsculas
        impuesto.setDescripcion(dto.getDescripcion());
        impuesto.setEmpresa(dto.getEmpresa());
        impuesto.setPorcentaje(dto.getPorcentaje());
        impuesto.setNumeroCliente(dto.getNumeroCliente());
        impuesto.setNumeroMedidor(dto.getNumeroMedidor());
        impuesto.setMontoAPagar(dto.getMontoAPagar());
        impuesto.setFechaFactura(dto.getFechaFactura());
        impuesto.setEstadoPago(dto.getEstadoPago());

        return impuesto;
    }

    @Transactional // Asegura que la transacción esté abierta
    public ReciboSalidaDto buscarReciboPorId(Long id) throws ResourceNotFoundException {
        Optional<Recibo> reciboOptional = reciboRepository.findReciboByIdWithImpuestos(id);

        if (reciboOptional.isPresent()) {
            Recibo recibo = reciboOptional.get();
            return modelMapper.map(recibo, ReciboSalidaDto.class);
        } else {
            throw new ResourceNotFoundException("Recibo no encontrado con ID: " + id);
        }
}

    @Override
    @Transactional
    public ReciboSalidaDto modificarEstado(ReciboModificacionDto reciboModificacionDto) throws ResourceNotFoundException {
        LOGGER.info("Iniciando la modificación del estado del recibo con ID: " + reciboModificacionDto.getId());

        // Buscar el recibo por ID
        Recibo recibo = reciboRepository.findById(reciboModificacionDto.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Recibo no encontrado con ID: " + reciboModificacionDto.getId());
                    return new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboModificacionDto.getId());
                });

        // Actualizar el estado del recibo
        recibo.setEstado(reciboModificacionDto.getEstado());

        // Guardar el recibo actualizado
        Recibo reciboActualizado = reciboRepository.save(recibo);
        LOGGER.info("Estado del recibo actualizado exitosamente. Nuevo estado: " + reciboActualizado.getEstado());

        // Mapear la entidad actualizada a DTO de salida y retornarlo
        return modelMapper.map(reciboActualizado, ReciboSalidaDto.class);
    }


//    @Override
//    @Transactional
//    public void eliminarRecibo(Long id) throws ResourceNotFoundException {
//        LOGGER.info("Iniciando eliminación del recibo con ID: {}", id);
//
//        // 1. Buscar el recibo
//        Recibo recibo = reciboRepository.findById(id)
//                .orElseThrow(() -> {
//                    LOGGER.warn("Recibo no encontrado con ID: {}", id);
//                    return new ResourceNotFoundException("Recibo no encontrado con el ID: " + id);
//                });
//
//        // 2. Eliminar (en cascada se borran también los impuestos)
//        reciboRepository.delete(recibo);
//        LOGGER.info("Recibo eliminado correctamente con ID: {}", id);
//    }
//    @Override
//    @Transactional
//    public void eliminarRecibosPorContratoId(Long contratoId) throws ResourceNotFoundException {
//        LOGGER.info("Iniciando eliminación de todos los recibos del contrato con ID: {}", contratoId);
//
////         1. Verificamos que el contrato exista
//        Contrato contrato = contratoRepository.findById(contratoId)
//                .orElseThrow(() -> {
//                    LOGGER.warn("Contrato no encontrado con ID: {}", contratoId);
//                    return new ResourceNotFoundException("Contrato no encontrado con ID: " + contratoId);
//                });
//
//        // 2. Verificamos si tiene recibos asociados
//        List<Recibo> recibos = reciboRepository.findByContratoId(contratoId);
//        if (recibos.isEmpty()) {
//            LOGGER.warn("No se encontraron recibos para el contrato con ID: {}", contratoId);
//            return;
//        }
//
//        // 3. Eliminamos los recibos (en cascada se eliminan los impuestos también)
//        reciboRepository.deleteAll(recibos);
//        LOGGER.info("Se eliminaron {} recibos del contrato con ID: {}", recibos.size(), contratoId);
//    }

}