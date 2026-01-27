package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.NotaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.NotaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.NotaCreadaEvent;
import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.NotaRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.INotaService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class NotaService implements INotaService {
    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;
    private final Logger LOGGER = LoggerFactory.getLogger(NotaService.class);
    private ModelMapper modelMapper;
    private UsuarioRepository usuarioRepository;
    private NotaRepository notaRepository;
    private ContratoRepository contratoRepository;
    private ContratoService contratoService;
    private EmbeddingService embeddingService;
    private ImagenService imagenService;
    private NotaSyncService notaSyncService;
    private final ApplicationEventPublisher publisher;

    public NotaService(UsuarioRepository usuarioRepository, NotaSyncService notaSyncService, ImagenService imagenService, EmbeddingService embeddingService, ContratoService contratoService, ModelMapper modelMapper, NotaRepository notaRepository, ContratoRepository contratoRepository, ApplicationEventPublisher publisher) {
        this.modelMapper = modelMapper;
        this.notaRepository = notaRepository;
        this.contratoRepository = contratoRepository;
        this.contratoService = contratoService;
        this.embeddingService = embeddingService;
        this.imagenService = imagenService;
        this.notaSyncService = notaSyncService;
        this.usuarioRepository = usuarioRepository;
        this.publisher = publisher;
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
                    dto.setIdContrato(nota.getContrato() != null ? nota.getContrato().getId() : null);

                    if (nota.getFechaCreacion() != null) {
                        dto.setFechaCreacion(nota.getFechaCreacion().toLocalDate());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        LOGGER.info("Se encontraron " + notaSalidaDto.size() + " notas");
        return notaSalidaDto;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private void validarAccesoContrato(Long userId, Authentication auth, Contrato contrato) {
        // Inquilino
        if (hasRole(auth, "ROLE_" + RolesCostantes.INQUILINO_USER)) {
            if (contrato.getInquilino() == null ||
                    contrato.getInquilino().getUsuarioCuentaInquilino() == null ||
                    !contrato.getInquilino().getUsuarioCuentaInquilino().getId().equals(userId)) {
                throw new RuntimeException("No tenés permiso sobre este contrato (inquilino).");
            }
            return;
        }

        if (hasRole(auth, "ROLE_" + RolesCostantes.PROPIETARIO_USER)) {
            if (contrato.getPropietario() == null ||
                    contrato.getPropietario().getUsuarioCuentaPropietario() == null ||
                    !contrato.getPropietario().getUsuarioCuentaPropietario().getId().equals(userId)) {
                throw new RuntimeException("No tenés permiso sobre este contrato (propietario).");
            }
            return;
        }
        if (contrato.getUsuario() == null || !contrato.getUsuario().getId().equals(userId)) {
            throw new RuntimeException("No tenés permiso sobre este contrato (inmobiliaria).");
        }
    }

    private NotaSalidaDto mapToDto(Nota n) {
        NotaSalidaDto dto = modelMapper.map(n, NotaSalidaDto.class);
        dto.setIdContrato(n.getContrato() != null ? n.getContrato().getId() : null);
        if (n.getFechaCreacion() != null) dto.setFechaCreacion(n.getFechaCreacion().toLocalDate());
        // imágenes: si tu ModelMapper no las mapea bien, las seteás manual acá.
        return dto;
    }

    @Transactional
    public List<NotaSalidaDto> listarMisNotas(Long userId, Authentication auth) {
        List<Nota> notas;

        if (hasRole(auth, "ROLE_" + RolesCostantes.INQUILINO_USER)) {
            notas = notaRepository.findNotasParaInquilino(userId);

        } else if (hasRole(auth, "ROLE_" + RolesCostantes.PROPIETARIO_USER)) {
            notas = notaRepository.findNotasParaPropietario(userId);

        } else {
            notas = notaRepository.findNotasParaInmobiliaria(userId);
        }

        return notas.stream().map(this::mapToDto).toList();
    }

    // ✅ Notas por contrato (para la pantalla del contrato)
    @Transactional
    public List<NotaSalidaDto> listarNotasPorContrato(Long userId, Authentication auth, Long contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        validarAccesoContrato(userId, auth, contrato);

        return notaRepository.findByContratoId(contratoId).stream()
                .map(this::mapToDto)
                .toList();
    }


    @Override
    @Transactional()
    public NotaSalidaDto crearNota(NotaEntradaDto notaEntradaDto) throws ResourceNotFoundException, IOException {

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


        // 4. Guardar el recibo (y los impuestos en cascada)
        Nota notaAPersistir = notaRepository.save(nota);
        contrato = nota.getContrato();
        try {
            contratoService.actualizarContratoEnSupabasePorId(contrato.getId());

        } catch (Exception e) {
            LOGGER.error("⚠️ Error al actualizar contrato en Supabase: {}", e.getMessage());
        }

        // 5. Mapear a DTO de salida
        NotaSalidaDto notaSalidaDto = modelMapper.map(notaAPersistir, NotaSalidaDto.class);
        notaSalidaDto.setIdContrato(notaAPersistir.getContrato().getId());
        notaSalidaDto.setContenido(notaAPersistir.getContenido());
        notaSalidaDto.setFechaCreacion(notaAPersistir.getFechaCreacion().toLocalDate());
        LOGGER.info("Proceso de creación de nota completado exitosamente");

        CompletableFuture.runAsync(() -> {
            try {
                guardarNotaEnSupabase(notaAPersistir);
                upsertNotaEmbedding(notaAPersistir);
            } catch (Exception ex) {
                LOGGER.error("⚠️ Error sincronizando nota con Supabase: {}", ex.getMessage());
            }
        });
        return notaSalidaDto;
    }

    private AutorNotaTipo resolverAutorTipo(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return AutorNotaTipo.DESCONOCIDO;

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_INQUILINO_USER")) return AutorNotaTipo.INQUILINO;
        if (roles.contains("ROLE_PROPIETARIO_USER")) return AutorNotaTipo.PROPIETARIO;
        if (roles.contains("ROLE_SUPER_ADMIN") || roles.contains("ROLE_ADMIN")) return AutorNotaTipo.INMOBILIARIA;

        return AutorNotaTipo.DESCONOCIDO;
    }

    @Transactional
    public NotaSalidaDto crearNotaConImagenes(Long userId,
                                              Authentication auth,
                                              NotaEntradaDto notaEntradaDto,
                                              MultipartFile[] imagenes)
            throws ResourceNotFoundException, IOException {

        Contrato contrato = contratoRepository.findById(notaEntradaDto.getIdContrato())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        validarAccesoContrato(userId, auth, contrato);

        Nota nota = new Nota();
        nota.setContrato(contrato);
        nota.setMotivo(notaEntradaDto.getMotivo());
        nota.setContenido(notaEntradaDto.getContenido());
        nota.setTipo(notaEntradaDto.getTipo());
        nota.setPrioridad(notaEntradaDto.getPrioridad());
        nota.setEstado(notaEntradaDto.getEstado());
        nota.setObservaciones(notaEntradaDto.getObservaciones());
        nota.setAutorTipo(resolverAutorTipo(auth));

        Usuario autor = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autor no encontrado"));
        nota.setAutorUsuario(autor);

        Nota notaPersistida = notaRepository.save(nota);

        Long autorUserId = autor.getId();
        publisher.publishEvent(new NotaCreadaEvent(notaPersistida.getId(), contrato.getId(), autorUserId));

        List<ImgUrlSalidaDto> nuevasImgs = new ArrayList<>();
        if (imagenes != null && imagenes.length > 0) {
            nuevasImgs = imagenService.subirImagenesYAsociarANota(notaPersistida.getId(), imagenes);
        }

        NotaSalidaDto salida = new NotaSalidaDto();
        salida.setId(notaPersistida.getId());
        salida.setIdContrato(contrato.getId());
        salida.setMotivo(notaPersistida.getMotivo());
        salida.setContenido(notaPersistida.getContenido());
        salida.setTipo(notaPersistida.getTipo());
        salida.setPrioridad(notaPersistida.getPrioridad());
        salida.setEstado(notaPersistida.getEstado());
        salida.setObservaciones(notaPersistida.getObservaciones());
        salida.setAutor(resolverAutorTipo(auth)); // <-- acá corregido
        salida.setFechaCreacion(notaPersistida.getFechaCreacion().toLocalDate());
        salida.setImagenes(nuevasImgs);

        Long notaId = notaPersistida.getId();
        Long contratoId = contrato.getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {

                CompletableFuture.runAsync(() -> {
                    try {
                        notaSyncService.guardarNotaEnSupabasePorId(notaId);
                        notaSyncService.upsertNotaEmbeddingPorId(notaId);
                    } catch (Exception ex) {
                        LOGGER.error("⚠️ Error sincronizando nota con Supabase: {}", ex.getMessage());
                    }
                });

                CompletableFuture.runAsync(() -> {
                    try {
                        contratoService.actualizarContratoEnSupabasePorId(contratoId);
                    } catch (Exception e) {
                        LOGGER.error("⚠️ Error al actualizar contrato en Supabase: {}", e.getMessage());
                    }
                });
            }
        });

        return salida;
    }


        private void guardarNotaEnSupabase(Nota n) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> registro = Map.of(
                "id", n.getId(),
                "id_contrato", n.getContrato().getId(),
                "user_id", n.getContrato().getUsuario().getId(),
                "tipo", n.getTipo(),
                "motivo", n.getMotivo(),
                "contenido", n.getContenido(),
                "prioridad", n.getPrioridad(),
                "estado", n.getEstado(),
                "observaciones", n.getObservaciones()
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/notas")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Nota guardada en Supabase: {}", n.getId());
        }
    }

    private String buildContenidoNota(Nota n) {
        return "NOTA|id=" + n.getId() +
                " | motivo=" + n.getMotivo() +
                " | estado=" + n.getEstado() +
                " | prioridad=" + n.getPrioridad() +
                " | contenido=" + n.getContenido();
    }

    private void upsertNotaEmbedding(Nota n) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        List<Float> embedding = embeddingService.generarEmbedding(buildContenidoNota(n));

        Map<String, Object> registro = Map.of(
                "id_nota", n.getId(),
                "id_contrato", n.getContrato().getId(),
                "user_id", n.getContrato().getUsuario().getId(),
                "contenido", buildContenidoNota(n),
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/notas_embeddings")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Embedding de nota creado: {}", n.getId());
        }
    }


    @Override
    @Transactional
    public NotaSalidaDto buscarNotaPorId(Long id) throws ResourceNotFoundException {
        Optional<Nota> notaOptional = notaRepository.findById(id);

        if (notaOptional.isPresent()) {
            Nota nota = notaOptional.get();
            NotaSalidaDto dto = modelMapper.map(nota, NotaSalidaDto.class);

            dto.setIdContrato(nota.getContrato() != null ? nota.getContrato().getId() : null);

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
        Contrato contrato = nota.getContrato();
        notaRepository.delete(nota);

        // ✅ Actualizamos contrato en Supabase (sin bloquear backend)
        CompletableFuture.runAsync(() -> {
            try {
                contratoService.actualizarContratoEnSupabasePorId(contrato.getId());

                LOGGER.info("🔄 Contrato {} actualizado en Supabase tras eliminar nota {}",
                        contrato.getId(), id);
            } catch (Exception ex) {
                LOGGER.error("⚠️ Error al actualizar contrato en Supabase tras eliminar nota: {}",
                        ex.getMessage());
            }
        });
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
