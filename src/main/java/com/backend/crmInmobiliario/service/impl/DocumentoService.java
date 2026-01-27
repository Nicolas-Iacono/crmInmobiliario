package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.documentos.DocumentoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.documentos.DocumentoSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.*;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.imgAPdf.PdfService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ContratoRepository contratoRepository;
    private final ModelMapper mapper;
    private final ImagenService imagenService;
    private final InquilinoRepository inquilinoRepository;
    private final PropietarioRepository propietarioRepository;
    private final GaranteRepository garanteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthUtil authUtil;
    private final PdfService pdfService;



    private static final String SUPABASE_URL = "https://kksdxwqcgrbemlpgjifr.supabase.co";
    private static final String BUCKET = "documentos";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtrc2R4d3FjZ3JiZW1scGdqaWZyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4MzM4NDEsImV4cCI6MjA2MDQwOTg0MX0.1VxlR2YSdnwvVkAZ0df8eZo3PBiMr90sbr9PgTQhQ-U";

    @Transactional
    public DocumentoSalidaDto subirDocumento(List<MultipartFile> archivos, DocumentoEntradaDto dto) throws Exception {

        if (archivos == null || archivos.isEmpty())
            throw new IllegalArgumentException("Debes subir al menos un archivo");

        // ✅ Obtener usuario desde JWT
        Long userId = authUtil.extractUserId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // ✅ Determinar nombre base del archivo
        String nombreBase = (dto.getNombreArchivo() != null && !dto.getNombreArchivo().isBlank())
                ? dto.getNombreArchivo()
                : "documento";

        String nombreFinalArchivo = UUID.randomUUID() + "_" + nombreBase + ".pdf";

        // ✅ Convertir todo lo subido en un único PDF
        MultipartFile pdfFinal = combinarEnUnSoloPdf(archivos, nombreFinalArchivo);

        // ✅ Subir PDF a Supabase
        String url = subirPdfASupabase(pdfFinal, nombreFinalArchivo);

        // ✅ Crear entidad documento
        Documento documento = new Documento();
        documento.setNombreArchivo(nombreFinalArchivo);
        documento.setUrlArchivo(url);
        documento.setFechaSubida(LocalDateTime.now());
        documento.setTipo(dto.getTipo());
        documento.setUsuario(usuario);

        // ✅ Asociación
        if (dto.getContratoId() != null) {
            documento.setContrato(contratoRepository.findById(dto.getContratoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado")));
        } else if (dto.getInquilinoId() != null) {
            documento.setInquilino(inquilinoRepository.findById(dto.getInquilinoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inquilino no encontrado")));
        } else if (dto.getPropietarioId() != null) {
            documento.setPropietario(propietarioRepository.findById(dto.getPropietarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado")));
        } else if (dto.getGaranteId() != null) {
            documento.setGarante(garanteRepository.findById(dto.getGaranteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garante no encontrado")));
        } else {
            throw new IllegalArgumentException("Debe asociar el documento a contrato / inquilino / propietario / garante");
        }

        Documento guardado = documentoRepository.save(documento);

        return mapper.map(guardado, DocumentoSalidaDto.class);
    }
    @Transactional
    public String subirPdfASupabase(MultipartFile archivo, String nombreArchivo) throws Exception {

        String STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + nombreArchivo;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setBearerAuth(API_KEY);

        HttpEntity<byte[]> request = new HttpEntity<>(archivo.getBytes(), headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                STORAGE_ENDPOINT, HttpMethod.PUT, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + nombreArchivo;
        } else {
            throw new Exception("Error subiendo PDF: " + response.getStatusCode());
        }
    }




    @Transactional
    public void eliminarDocumento(Long id) throws Exception {
        Long userId = authUtil.extractUserId();

        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

        if (!doc.getUsuario().getId().equals(userId)) {
            throw new AccessDeniedException("No tienes permisos para eliminar este documento");
        }

        imagenService.eliminarDeStorageSupabase(doc.getUrlArchivo());; // ✅ Reutilizamos método que ya tenés
        documentoRepository.delete(doc);
    }

    @Transactional
    public List<DocumentoSalidaDto> listarPorContrato(Long contratoId) {
        Long userId = authUtil.extractUserId();

        return documentoRepository.buscarPorContrato(contratoId).stream()
                .filter(doc -> doc.getUsuario().getId().equals(userId))
                .map(doc -> mapper.map(doc, DocumentoSalidaDto.class))
                .toList();
    }

    @Transactional
    public List<DocumentoSalidaDto> listarPorInquilino(Long inquilinoId) {
        Long userId = authUtil.extractUserId();

        return documentoRepository.findByInquilinoId(inquilinoId).stream()
                .filter(doc -> doc.getUsuario().getId().equals(userId))
                .map(doc -> mapper.map(doc, DocumentoSalidaDto.class))
                .toList();
    }

    @Transactional
    public List<DocumentoSalidaDto> listarPorPropietario(Long propietarioId) {
        Long userId = authUtil.extractUserId();

        return documentoRepository.findByPropietarioId(propietarioId).stream()
                .filter(doc -> doc.getUsuario().getId().equals(userId))
                .map(doc -> mapper.map(doc, DocumentoSalidaDto.class))
                .toList();
    }

    @Transactional
    public List<DocumentoSalidaDto> listarPorGarante(Long garanteId) {
        Long userId = authUtil.extractUserId();

        return documentoRepository.findByGaranteId(garanteId).stream()
                .filter(doc -> doc.getUsuario().getId().equals(userId))
                .map(doc -> mapper.map(doc, DocumentoSalidaDto.class))
                .toList();
    }
    @Transactional
    public List<DocumentoSalidaDto> listarPorUsuario() {

        Long userId = authUtil.extractUserId();

        return documentoRepository.findAll().stream()
                .filter(doc ->
                        doc.getUsuario() != null &&
                                doc.getUsuario().getId().equals(userId)
                )
                .map(doc -> mapper.map(doc, DocumentoSalidaDto.class))
                .toList();
    }
    private MultipartFile convertirImagenesAPdf(List<MultipartFile> imagenes, String nombrePdf) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(pdfDoc, baos);

        pdfDoc.open();

        for (MultipartFile imagen : imagenes) {
            com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imagen.getBytes());
            img.scaleToFit(550, 750);
            img.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
            pdfDoc.add(img);
            pdfDoc.newPage(); // ✅ siguiente imagen en nueva página
        }

        pdfDoc.close();

        return new MockMultipartFile(
                "file",
                nombrePdf,
                MediaType.APPLICATION_PDF_VALUE,
                baos.toByteArray()
        );
    }


    private MultipartFile convertirImagenAPdf(MultipartFile imagen) throws Exception {

        String nombrePdf = UUID.randomUUID() + "_convertido.pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(pdfDoc, baos);

        pdfDoc.open();

        com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imagen.getBytes());
        img.scaleToFit(550, 750);
        img.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);

        pdfDoc.add(img);
        pdfDoc.close();

        return new MockMultipartFile(
                "file",
                nombrePdf,
                MediaType.APPLICATION_PDF_VALUE,
                baos.toByteArray()
        );
    }

    private MultipartFile combinarEnUnSoloPdf(List<MultipartFile> archivos, String nombreFinal) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        PdfCopy copy = new PdfCopy(pdfDoc, baos);
        pdfDoc.open();

        for (MultipartFile file : archivos) {
            String tipo = file.getContentType();

            if (tipo != null && tipo.startsWith("image")) {
                // 👉 Convertir imagen a PDF temporal y unir
                MultipartFile pdfImg = convertirImagenAPdf(file);
                agregarPdfAlCopy(pdfImg, copy);
            } else if (tipo != null && tipo.equals("application/pdf")) {
                agregarPdfAlCopy(file, copy);
            } else {
                throw new IllegalArgumentException("Solo se aceptan imágenes o PDFs");
            }
        }

        pdfDoc.close();

        return new MockMultipartFile(
                "file",
                nombreFinal,
                "application/pdf",
                baos.toByteArray()
        );
    }

    private void agregarPdfAlCopy(MultipartFile pdfFile, PdfCopy copy) throws Exception {
        PdfReader reader = new PdfReader(pdfFile.getBytes());
        int numPages = reader.getNumberOfPages();

        for (int i = 1; i <= numPages; i++) {
            copy.addPage(copy.getImportedPage(reader, i));
        }

        copy.freeReader(reader);
        reader.close(); // ✅ Se cierra acá, no el copy
    }
}
