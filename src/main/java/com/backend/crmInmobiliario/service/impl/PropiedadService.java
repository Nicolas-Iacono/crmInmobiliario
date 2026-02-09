package com.backend.crmInmobiliario.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropiedadModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.ProspectoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.service.IPropiedadService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.backend.crmInmobiliario.service.impl.ContratoService.opt;


@Service
public class PropiedadService implements IPropiedadService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${openai.api.key}")
    private String OPENAI_APIKEY;

    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private InquilinoRepository inquilinoRepository;

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProspectoRepository prospectoRepository;
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired
    private PushNotificationService pushNotificationService;

    private EmbeddingService embeddingService;

    @Autowired
    private JwtUtil jwtUtil;

    private AuthUtil authUtil;


    public PropiedadService(AuthUtil authUtil, EmbeddingService embeddingService,JwtUtil jwtUtil,ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository, PropietarioRepository propietarioRepository) {
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.propietarioRepository = propietarioRepository;
        this.jwtUtil = jwtUtil;
        this.embeddingService = embeddingService;
        this.authUtil = authUtil;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true); // Ignorar ambigüedad en el mapeo.
        modelMapper.typeMap(PropiedadEntradaDto.class, Propiedad.class)
                .addMapping(PropiedadEntradaDto::getId_propietario, Propiedad::setPropietario);
//                .addMapping(PropiedadEntradaDto::getNombreUsuario, Propiedad::setUsuario);

        modelMapper.typeMap(Propiedad.class, PropiedadSalidaDto.class)
                .addMapping(Propiedad::getPropietario, PropiedadSalidaDto::setPropietarioSalidaDto)
                .addMapping(Propiedad::getUsuario, PropiedadSalidaDto::setUsuarioDtoSalida);

    }

    @Transactional
    @Override
    public List<PropiedadSoloSalidaDto> listarPropiedades() {
        List<Propiedad> propiedades = propiedadRepository.findAll();

        return propiedades.stream()
                .filter(Objects::nonNull)
                .map(propiedad -> {
                    PropiedadSoloSalidaDto dto = modelMapper.map(propiedad, PropiedadSoloSalidaDto.class);

                    if (propiedad.getImagenes() != null && !propiedad.getImagenes().isEmpty()) {
                        List<ImgUrlSalidaDto> imagenesDto = propiedad.getImagenes().stream()
                                .map(img -> {
                                    ImgUrlSalidaDto imgDto = new ImgUrlSalidaDto();
                                    imgDto.setIdImage(img.getIdImage());
                                    imgDto.setImageUrl(img.getImageUrl());
                                    imgDto.setNombreOriginal(img.getNombreOriginal());
                                    imgDto.setTipoImagen(img.getTipoImagen());
                                    imgDto.setFechaSubida(img.getFechaSubida());
                                    return imgDto;
                                })
                                .toList();
                        dto.setImagenes(imagenesDto);
                    } else {
                        dto.setImagenes(Collections.emptyList());
                    }

                    return dto;
                })
                .toList();
    }


    @Override
    @Transactional
    public PropiedadSalidaDto crearPropiedad(PropiedadEntradaDto propiedadEntradaDto, HttpServletRequest request)
            throws ResourceNotFoundException {

        // 🔹 1️⃣ Extraer token JWT del header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Token no encontrado o inválido");
        }

        String token = authHeader.substring(7);
        DecodedJWT decodedJWT = jwtUtil.validateAccessToken(token);
        Long userId = jwtUtil.extractUserId(decodedJWT);

        // 🔹 2️⃣ Buscar usuario autenticado
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 🔹 3️⃣ Crear propiedad
        Propiedad propiedad = new Propiedad();
        propiedad.setDireccion(propiedadEntradaDto.getDireccion());
        propiedad.setLocalidad(propiedadEntradaDto.getLocalidad());
        propiedad.setPartido(propiedadEntradaDto.getPartido());
        propiedad.setProvincia(propiedadEntradaDto.getProvincia());
        propiedad.setTipo(propiedadEntradaDto.getTipo());
        propiedad.setInventario(propiedadEntradaDto.getInventario());
        propiedad.setDisponibilidad(propiedadEntradaDto.getDisponibilidad());
        propiedad.setPrecio(propiedadEntradaDto.getPrecio());
        propiedad.setCantidadAmbientes(propiedadEntradaDto.getCantidadAmbientes());
        propiedad.setPileta(propiedadEntradaDto.getPileta());
        propiedad.setCochera(propiedadEntradaDto.getCochera());
        propiedad.setJardin(propiedadEntradaDto.getJardin());
        propiedad.setPatio(propiedadEntradaDto.getPatio());
        propiedad.setVisibleAOtros(propiedadEntradaDto.isVisibleAOtros());
        propiedad.setUsuario(usuario);

        // 🔹 4️⃣ Asignar propietario (opcional)
        if (propiedadEntradaDto.getId_propietario() != null) {
            Propietario propietario = propietarioRepository.findById(propiedadEntradaDto.getId_propietario())
                    .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

            if (!propietario.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
            }

            propiedad.setPropietario(propietario);
        }

        // 🔹 5️⃣ Guardar propiedad
        Propiedad propiedadAPersistir = propiedadRepository.save(propiedad);

        // 🔹 6️⃣ Activar disponibilidad
        boolean propiedadActiva = cambiarDisponibilidadPropiedad(propiedadAPersistir.getId_propiedad());
        if (!propiedadActiva) {
            throw new RuntimeException("No se pudo activar la propiedad");
        }

        // 🔹 7️⃣ Mapear a DTO de salida
        PropiedadSalidaDto propiedadSalidaDto = modelMapper.map(propiedadAPersistir, PropiedadSalidaDto.class);

        if (propiedadAPersistir.getPropietario() != null) {
            PropietarioContratoDtoSalida propietarioSalidaDto =
                    modelMapper.map(propiedadAPersistir.getPropietario(), PropietarioContratoDtoSalida.class);
            propiedadSalidaDto.setPropietarioSalidaDto(propietarioSalidaDto);
        } else {
            propiedadSalidaDto.setPropietarioSalidaDto(null);
        }

        CompletableFuture.runAsync(() -> {
            try {
                guardarPropiedadEnSupabase(propiedadAPersistir);
                upsertPropiedadEmbedding(propiedadAPersistir); // ✅ Embedding en Supabase IA
            } catch (Exception ex) {
                LOGGER.error("⚠️ Error al procesar Propiedad en Supabase: {}", ex.getMessage());
            }
        });

        notificarProspectosPorPropiedad(propiedadAPersistir);
        return propiedadSalidaDto;
    }

    private String buildContenidoPropiedad(Propiedad p) {
        return "PROPIEDAD|" +
                "id=" + p.getId_propiedad() +
                " | direccion=" + opt(p.getDireccion()) +
                " | localidad=" + opt(p.getLocalidad()) +
                " | partido=" + opt(p.getPartido()) +
                " | provincia=" + opt(p.getProvincia()) +
                " | tipo=" + opt(p.getTipo()) +
                " | disponible=" + (p.isDisponibilidad() ? "si" : "no");
    }
    private void upsertPropiedadEmbedding(Propiedad p) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String contenido = buildContenidoPropiedad(p);
        List<Float> embedding = embeddingService.generarEmbedding(contenido);

        Map<String, Object> registro = Map.of(
                "id_propiedad", p.getId_propiedad(),
                "user_id", p.getUsuario().getId(),
                "contenido", contenido,
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/propiedades_embeddings")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Embedding propiedad actualizado: {}", p.getId_propiedad());
        }
    }

    @Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {
        List<Propiedad> propiedades = propiedadRepository.findByUsuarioId(userId);

        propiedades.forEach(c -> {
            try {
                upsertPropiedadEmbedding(c);
            } catch (Exception e) {
                LOGGER.error("Error generando embedding para propiedad {}: {}", c.getId_propiedad(), e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} contratos del usuario {}", propiedades.size(), userId);
    }


    private void guardarPropiedadEnSupabase(Propiedad propiedad) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> registro = new HashMap<>();
        registro.put("id", propiedad.getId_propiedad());
        registro.put("user_id", propiedad.getUsuario().getId());
        registro.put("tipo", propiedad.getTipo());
        registro.put("direccion", propiedad.getDireccion());
        registro.put("localidad", propiedad.getLocalidad());
        registro.put("partido", propiedad.getPartido());
        registro.put("provincia", propiedad.getProvincia());
        registro.put("inventario", propiedad.getInventario());
        registro.put("disponibilidad", propiedad.isDisponibilidad());
        registro.put("fecha_registro", LocalDate.now().toString());

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/propiedades")
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
            LOGGER.info("✅ Propiedad guardada en Supabase: {}", propiedad.getId_propiedad());
        }
    }

    private void eliminarPropiedadEmbeddingSupabase(Long idPropiedad) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/propiedades_embeddings?id_propiedad=eq." + idPropiedad)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase delete error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("🗑️ Embedding propiedad eliminado: {}", idPropiedad);
        }
    }
    @Transactional
    @Override
    public Boolean cambiarDisponibilidadPropiedad(Long id) throws ResourceNotFoundException {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Propiedad no encontrado"));
        propiedad.setDisponibilidad(!propiedad.isDisponibilidad());
        propiedadRepository.save(propiedad);
        return propiedad.isDisponibilidad();
    }

    @Override
    @Transactional
    public PropiedadSalidaDto actualizarPropiedad(Long id, PropiedadModificacionDto dto) throws ResourceNotFoundException {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        if (dto.getDireccion() != null) propiedad.setDireccion(dto.getDireccion());
        if (dto.getLocalidad() != null) propiedad.setLocalidad(dto.getLocalidad());
        if (dto.getPartido() != null) propiedad.setPartido(dto.getPartido());
        if (dto.getProvincia() != null) propiedad.setProvincia(dto.getProvincia());
        if (dto.getTipo() != null) propiedad.setTipo(dto.getTipo());
        if (dto.getInventario() != null) propiedad.setInventario(dto.getInventario());
        if (dto.getDisponibilidad() != null) propiedad.setDisponibilidad(dto.getDisponibilidad());
        if (dto.getPrecio() != null) propiedad.setPrecio(dto.getPrecio());
        if (dto.getCantidadAmbientes() != null) propiedad.setCantidadAmbientes(dto.getCantidadAmbientes());
        if (dto.getPileta() != null) propiedad.setPileta(dto.getPileta());
        if (dto.getCochera() != null) propiedad.setCochera(dto.getCochera());
        if (dto.getJardin() != null) propiedad.setJardin(dto.getJardin());
        if (dto.getPatio() != null) propiedad.setPatio(dto.getPatio());
        propiedad.setVisibleAOtros(dto.isVisibleAOtros());

        if (dto.getPropietarioId() != null) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));
            if (!propietario.getUsuario().getId().equals(propiedad.getUsuario().getId())) {
                throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
            }
            propiedad.setPropietario(propietario);
        }

        Propiedad saved = propiedadRepository.save(propiedad);
        return modelMapper.map(saved, PropiedadSalidaDto.class);
    }
    private void notificarProspectosPorPropiedad(Propiedad propiedad) {
        List<Prospecto> prospectos = prospectoRepository.findByUsuarioIdNot(propiedad.getUsuario().getId()).stream()
                .filter(prospecto -> Boolean.TRUE.equals(prospecto.getVisibilidadPublico()))
                .filter(prospecto -> prospecto.cumpleConPropiedad(propiedad))
                .toList();
        if (prospectos.isEmpty()) {
            return;
        }

        int total = prospectos.size();
        pushSubscriptionRepository.findByUserId(propiedad.getUsuario().getId())
                .forEach(sub -> pushNotificationService.enviarNotificacion(
                        sub,
                        "📌 Prospectos encontrados para tu propiedad",
                        String.format("Hay %d prospecto(s) que buscan una propiedad en %s.",
                                total,
                                propiedad.getLocalidad() != null ? propiedad.getLocalidad() : "tu zona")
                ));
    }
    @Override
    @Transactional
    public List<PropiedadSalidaDto> buscarPropiedadesPorUsuario(String username) {
        List<Propiedad> propiedadList = propiedadRepository.findPropiedadByUsername(username);
        return propiedadList.stream()
                .map(propiedad -> modelMapper.map(propiedad, PropiedadSalidaDto.class))
                .toList();
    }
    @Transactional
    public PropiedadSalidaDto asignarPropietario(Long propiedadId, Long propietarioId) throws ResourceNotFoundException {
        Propiedad p = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        Propietario prop = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        if (!prop.getUsuario().getId().equals(p.getUsuario().getId())) {
            throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
        }

        p.setPropietario(prop);
        Propiedad saved = propiedadRepository.save(p);

        return modelMapper.map(saved, PropiedadSalidaDto.class);
    }
    @Transactional
    public PropiedadSalidaDto quitarPropietario(Long propiedadId) throws ResourceNotFoundException {
        Propiedad p = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        p.setPropietario(null);
        Propiedad saved = propiedadRepository.save(p);

        PropiedadSalidaDto out = modelMapper.map(saved, PropiedadSalidaDto.class);
        out.setPropietarioSalidaDto(null);
        return out;
    }
    @Override
    @Transactional
    public PropiedadSalidaDto buscarPropiedadPorId(Long id)throws ResourceNotFoundException {
       Propiedad propiedad = propiedadRepository.findById(id).orElse(null);
       PropiedadSalidaDto propiedadSalidaDto =  null;
       if(propiedad != null){
           propiedadSalidaDto = modelMapper.map(propiedad, PropiedadSalidaDto.class);

           List<ImgUrlSalidaDto> imagenesDto = propiedad.getImagenes()
                   .stream()
                   .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
                   .toList();

           propiedadSalidaDto.setImagenes(imagenesDto);


       }else{
           throw new ResourceNotFoundException("No se encontro la propiedad buscada");
       }
        return propiedadSalidaDto;
    }

    @Override
    @Transactional
    public void eliminarPropiedad(Long id) throws ResourceNotFoundException {
        // 1) Traer con imágenes cargadas
        Propiedad p = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la propiedad con el id proporcionado."));

        // 2) Romper relación con el propietario para que no la re-persistan al flush
        Propietario prop = p.getPropietario();
        if (prop != null && prop.getPropiedades() != null) {
            prop.getPropiedades().remove(p); // <- clave
        }
        p.setPropietario(null);

        // 3) Cargar y cortar hijos (lado dueño) antes de clear
        p.getImagenes().forEach(img -> img.setPropiedad(null));
        p.getImagenes().clear();

        // 4) Borrar y forzar flush para ver los DELETE ya
        propiedadRepository.delete(p);
        propiedadRepository.flush();
        CompletableFuture.runAsync(() -> {
            try {
                eliminarPropiedadEmbeddingSupabase(id);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error eliminando embedding propiedad en Supabase: {}", e.getMessage());
            }
        });
        System.out.println("Propiedad eliminada: " + id);
    }

    @Override
    @Transactional
    public Integer enumerarPropiedades() {
        Long userId = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", userId);

        return propiedadRepository.countByUsuarioId(userId);

    }

    @Override
    public List<PropiedadSoloSalidaDto> buscarPorEmailPropietario(String email) {
        List<Propiedad> propiedades = propiedadRepository.findByPropietarioEmail(email);
        return propiedades.stream()
                .map(p -> modelMapper.map(p, PropiedadSoloSalidaDto.class))
                .toList();
    }
    @Transactional
    public List<PropiedadSalidaDto> buscarPorPropietarioConImagenes(Long propietarioId) {
        List<Propiedad> propiedades = propiedadRepository.findByPropietarioIdWithImages(propietarioId);

        return propiedades.stream()
                .map(prop -> {
                    PropiedadSalidaDto dto = modelMapper.map(prop, PropiedadSalidaDto.class);

                    if (prop.getImagenes() != null && !prop.getImagenes().isEmpty()) {
                        dto.setImagenes(
                                prop.getImagenes().stream()
                                        .map(img -> {
                                            ImgUrlSalidaDto imgDto = new ImgUrlSalidaDto();
                                            imgDto.setIdImage(img.getIdImage());
                                            imgDto.setImageUrl(img.getImageUrl());
                                            imgDto.setNombreOriginal(img.getNombreOriginal());
                                            imgDto.setTipoImagen(img.getTipoImagen());
                                            imgDto.setFechaSubida(img.getFechaSubida());
                                            return imgDto;
                                        }).toList()
                        );
                    }

                    return dto;
                }).toList();
    }

    @Override
    @Transactional()
    public List<PropiedadSalidaDto> listarPropiedadesPorUsuarioId(Long userId) {
        List<Propiedad> propiedades = propiedadRepository.findByUsuarioId(userId);

        return propiedades.stream()
                .map(propiedad -> modelMapper.map(propiedad, PropiedadSalidaDto.class))
                .toList();
    }

}
