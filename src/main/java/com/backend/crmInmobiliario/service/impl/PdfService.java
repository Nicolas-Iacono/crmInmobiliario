//package com.backend.crmInmobiliario.service.impl;
//
//import com.backend.crmInmobiliario.DTO.entrada.ContratoPdfEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ContratoPdfSalidaDto;
//import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
//import com.backend.crmInmobiliario.controller.ContratoController;
//import com.backend.crmInmobiliario.entity.Contrato;
//import com.backend.crmInmobiliario.entity.PdfContrato;
//import com.backend.crmInmobiliario.entity.Propietario;
//import com.backend.crmInmobiliario.entity.Usuario;
//import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
//import com.backend.crmInmobiliario.repository.ContratoRepository;
//import com.backend.crmInmobiliario.repository.MunicipalRepository;
//import com.backend.crmInmobiliario.repository.PdfContratoRepository;
//import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
//import com.backend.crmInmobiliario.service.IPdfService;
//import org.modelmapper.ModelMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class PdfService implements IPdfService {
//    private final Logger LOGGER = LoggerFactory.getLogger(PdfContrato.class);
//    @Autowired
//    private ModelMapper modelMapper;  // Inyección de ModelMapper
//
//    @Autowired
//    private PdfContratoRepository pdfContratoRepository;  // Inyección del repositorio PdfContrato
//
//    @Autowired
//    private ContratoRepository contratoRepository;
//    @Override
//    public ContratoPdfSalidaDto guardarPdf(ContratoPdfEntradaDto contratoPdfEntradaDto) throws ResourceNotFoundException {
//        // Obtener el contrato de la base de datos o lanzar una excepción si no existe
//        Contrato contrato = contratoRepository.findById(contratoPdfEntradaDto.getContrato_id())
//                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
//
//        // Crear el nuevo PDF y enlazar ambas entidades
//        PdfContrato pdfEnCreacion = new PdfContrato();
//        pdfEnCreacion.setParagraph(contratoPdfEntradaDto.getParagraph());
//        pdfEnCreacion.setContrato(contrato);
//
//        // Enlazar el pdf con el contrato
//        contrato.setPdfContrato(pdfEnCreacion);
//
//        // Guardar el contrato, lo cual debería también guardar pdfEnCreacion si está bien configurada la relación
//        Contrato contratoGuardado = contratoRepository.save(contrato);
//
//        // Convertir la entidad guardada a un DTO de salida y retornarlo
//        ContratoPdfSalidaDto pdfSalidaDto = modelMapper.map(contratoGuardado.getPdfContrato(), ContratoPdfSalidaDto.class);
//        return pdfSalidaDto;
//    }
//
//
//
//    @Override
//    public void eliminarPdf(Long id) throws ResourceNotFoundException {
//            PdfContrato pdf = pdfContratoRepository.findById(id)
//                    .orElseThrow(()->new ResourceNotFoundException("No se encontro el pdf con el id proporcionado!!"));
//            pdfContratoRepository.delete(pdf);
//    }
//
//    @Override
//    public List<ContratoPdfSalidaDto> listarPdf() {
//      List<PdfContrato> PDFs = pdfContratoRepository.findAll();
//        return PDFs.stream()
//                .map(pdf -> modelMapper.map(pdf, ContratoPdfSalidaDto.class))
//                .toList();
//    }
//
//
//}
