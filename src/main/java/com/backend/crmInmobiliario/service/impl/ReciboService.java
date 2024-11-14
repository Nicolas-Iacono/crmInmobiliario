//package com.backend.crmInmobiliario.service.impl;
//
//import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
//import com.backend.crmInmobiliario.entity.*;
//import com.backend.crmInmobiliario.entity.impuestos.Agua;
//import com.backend.crmInmobiliario.entity.impuestos.Gas;
//import com.backend.crmInmobiliario.entity.impuestos.Luz;
//import com.backend.crmInmobiliario.entity.impuestos.Municipal;
//import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
//import com.backend.crmInmobiliario.repository.ContratoRepository;
//import com.backend.crmInmobiliario.repository.InquilinoRepository;
//import com.backend.crmInmobiliario.repository.ReciboRepository;
//import com.backend.crmInmobiliario.service.IReciboService;
//import jakarta.persistence.EntityNotFoundException;
//import org.modelmapper.ModelMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class ReciboService implements IReciboService {
//    private final Logger LOGGER = LoggerFactory.getLogger(ReciboService.class);
//    private ModelMapper modelMapper;
//    private ReciboRepository reciboRepository;
//    private ContratoRepository contratoRepository;
//    private InquilinoRepository inquilinoRepository;
//
//    public ReciboService(ModelMapper modelMapper, ReciboRepository reciboRepository, ContratoRepository contratoRepository,InquilinoRepository inquilinoRepository) {
//        this.modelMapper = modelMapper;
//        this.reciboRepository = reciboRepository;
//        this.contratoRepository = contratoRepository;
//        this.inquilinoRepository =inquilinoRepository;
//        configureMapping();
//    }
//
//    private void configureMapping() {
//    }
//
//    @Override
//    public List<ReciboSalidaDto> listarRecibos() {
//        List<Recibo> recibos = reciboRepository.findAll();
//        return recibos.stream()
//                .map(recibo -> modelMapper.map(recibo, ReciboSalidaDto.class))
//                .toList();
//    }
//
//    @Override
//    public ReciboSalidaDto crearRecibo(ReciboEntradaDto reciboEntradaDto) throws ResourceNotFoundException {
//        // Buscar el contrato relacionado con el ID proporcionado
//        Contrato contrato = contratoRepository.findById(reciboEntradaDto.getIdContrato())
//                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contrato con el id proporcionado"));
//
//        // Obtener detalles del contrato
//        Inquilino inquilino = contrato.getInquilino();
//        Propietario propietario = contrato.getPropietario();
//        Propiedad propiedad = contrato.getPropiedad();
//        Gas impuestoGas = contrato.getGas();
//        Agua impuestoAgua = contrato.getAgua();
//        Luz impuestoLuz = contrato.getLuz();
//        Municipal impuestoMuni = contrato.getMunicipal();
//
//        // Crear cadenas con la información del propietario, inquilino y propiedad
//        String propietarioNombre = propietario.getNombre() + " " + propietario.getApellido();
//        String inquilinoNombre = inquilino.getNombre() + " " + inquilino.getApellido();
//        String domicilioAlquilado = propiedad.getDireccion() + ", Localidad de " + propiedad.getLocalidad() + ", partido de " + propiedad.getPartido();
//
//        // Obtener los porcentajes y nombres de las empresas de los impuestos
//        Double porcentajeLuz = impuestoLuz.getPorcentaje();
//        String nombreEmpresaLuz = impuestoLuz.getEmpresa();
//
//        Double porcentajeGas = impuestoGas.getPorcentaje();
//        String nombreEmpresaGas = impuestoGas.getEmpresa();
//
//        Double porcentajeAgua = impuestoAgua.getPorcentaje();
//        String nombreEmpresaAgua = impuestoAgua.getEmpresa();
//
//        Double porcentajeMuni = impuestoMuni.getPorcentaje();
//        String nombreEmpresaMuni = impuestoMuni.getEmpresa();
//
//        // Crear el nuevo recibo y establecer los valores
//        Recibo reciboEnCreacion = modelMapper.map(reciboEntradaDto, Recibo.class);
//        reciboEnCreacion.setPeriodo(reciboEntradaDto.getPeriodo());
//        reciboEnCreacion.setMontoTotal(reciboEntradaDto.getMontoTotal());
//        reciboEnCreacion.setContrato(contrato); // Asegúrate de establecer el contrato relacionado
//
//        // Guardar el recibo en la base de datos
//        reciboEnCreacion = reciboRepository.save(reciboEnCreacion);
//
//        // Crear el DTO de salida
//        ReciboSalidaDto reciboSalidaDto = new ReciboSalidaDto();
//        reciboSalidaDto.setId(reciboEnCreacion.getId()); // Establece el ID generado
//        reciboSalidaDto.setFechaEmision(LocalDate.now());
//        reciboSalidaDto.setPeriodo(reciboEnCreacion.getPeriodo());
//        reciboSalidaDto.setMontoTotal(reciboEnCreacion.getMontoTotal());
//
//        // Información del contrato
//        reciboSalidaDto.setPropietarioNombre(propietarioNombre);
//        reciboSalidaDto.setInquilinoNombre(inquilinoNombre);
//        reciboSalidaDto.setPropiedadDireccion(domicilioAlquilado);
//        reciboSalidaDto.setMontoAlquiler(contrato.getMontoAlquiler());
//
//        // Información de los impuestos
//        reciboSalidaDto.setEmpresaGas(nombreEmpresaGas);
//        reciboSalidaDto.setPorcentajeGas(porcentajeGas);
//        reciboSalidaDto.setMontoGasMensual(reciboEntradaDto.getGasServicio());
//
//        reciboSalidaDto.setEmpresaLuz(nombreEmpresaLuz);
//        reciboSalidaDto.setPorcentajeLuz(porcentajeLuz);
//        reciboSalidaDto.setMontoLuzMensual(reciboEntradaDto.getLuzServicio());
//
//        reciboSalidaDto.setEmpresaMuni(nombreEmpresaMuni);
//        reciboSalidaDto.setPorcentajeMuni(porcentajeMuni);
//        reciboSalidaDto.setMontoMuniMensual(reciboEntradaDto.getMunicipalServicio());
//
//        reciboSalidaDto.setEmpresaAgua(nombreEmpresaAgua);
//        reciboSalidaDto.setPorcentajeAgua(porcentajeAgua);
//        reciboSalidaDto.setMontoAguaMensual(reciboEntradaDto.getAguaServicio());
//
//        // Retornar el DTO de salida
//        return reciboSalidaDto;
//    }
//
//
//
//    @Override
//    public ReciboSalidaDto buscarReciboPorId(Long id) throws ResourceNotFoundException {
//        Recibo recibo = reciboRepository.findById(id)
//                .orElseThrow(()-> new EntityNotFoundException("No se encontro el recibo"));
//
//        return modelMapper.map(recibo, ReciboSalidaDto.class);
//    }
//
//    @Override
//    public void eliminarRecibo(Long id) throws ResourceNotFoundException {
//
//    }
//
//    @Override
//    public List<ReciboSalidaDto> buscarRecibosPorNumDeContrato(Long id) throws ResourceNotFoundException {
//        // Verificar si el contrato existe
//        Contrato contrato = contratoRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el contrato con el id proporcionado"));
//
//        // Buscar los recibos asociados al contrato
//        List<Recibo> recibos = reciboRepository.findByContrato(contrato);
//
//        if (recibos.isEmpty()) {
//            throw new ResourceNotFoundException("No se encontraron recibos para el contrato con id " + id);
//        }
//
//        // Mapear la lista de recibos a ReciboSalidaDto
//        return recibos.stream()
//                .map(recibo -> modelMapper.map(recibo, ReciboSalidaDto.class))
//                .collect(Collectors.toList());
//    }
//}
