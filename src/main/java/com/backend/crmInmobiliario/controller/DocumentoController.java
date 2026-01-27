package com.backend.crmInmobiliario.controller;


import com.backend.crmInmobiliario.DTO.entrada.documentos.DocumentoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.documentos.DocumentoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.DocumentoService;
import com.backend.crmInmobiliario.service.impl.imgAPdf.PdfService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@AllArgsConstructor
@RequestMapping("api/documentos")
public class DocumentoController {
    private DocumentoService documentoService;
    private AuthUtil authUtil;
    private PdfService pdfService;

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentoSalidaDto> upload(
            @RequestPart("files") List<MultipartFile> files, // ✅ ahora lista
            @RequestPart("data") DocumentoEntradaDto dto) throws Exception {

        DocumentoSalidaDto result = documentoService.subirDocumento(files, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping("/contrato/{contratoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentoSalidaDto>> listarPorContrato(@PathVariable Long contratoId)
            throws ResourceNotFoundException {

        return ResponseEntity.ok(documentoService.listarPorContrato(contratoId));
    }

    @DeleteMapping("/{documentoId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long documentoId)
            throws Exception {

        documentoService.eliminarDocumento(documentoId);
        return ResponseEntity.ok("Documento eliminado correctamente");
    }

    @GetMapping("/inquilino/{inquilinoId}")
    public List<DocumentoSalidaDto> getDocsByInquilino(@PathVariable Long inquilinoId) {
        return documentoService.listarPorInquilino(inquilinoId);
    }

    @GetMapping("/propietario/{propietarioId}")
    public List<DocumentoSalidaDto> getDocsByPropietario(@PathVariable Long propietarioId) {
        return documentoService.listarPorPropietario(propietarioId);
    }

    @GetMapping("/garante/{garanteId}")
    public List<DocumentoSalidaDto> getDocsByGarante(@PathVariable Long garanteId) {
        return documentoService.listarPorGarante(garanteId);
    }

    @GetMapping("/me")
    public ResponseEntity<List<DocumentoSalidaDto>> listarMisDocumentos() {
        List<DocumentoSalidaDto> documentos = documentoService.listarPorUsuario();
        return ResponseEntity.ok(documentos);
    }

    @PostMapping(value = "/imagenes-a-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertir(
            @RequestPart("files") List<MultipartFile> files) {

        try {
            byte[] pdf = pdfService.convertirImagenesAPdf(files);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=imagenes.pdf")
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
