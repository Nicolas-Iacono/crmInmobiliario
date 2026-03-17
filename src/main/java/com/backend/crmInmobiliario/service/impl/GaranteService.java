package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.modificacion.GaranteDtoModificacion;
import com.backend.crmInmobiliario.DTO.modificacion.InquilinoDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteUser;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.pages.PageResponse;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IGaranteService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class GaranteService implements IGaranteService {
    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;
    private static final String UPLOAD_DIR = "https://srv1597-files.hstgr.io/cb06a4ee9e063e7f/files/public_html/uploads";
    private final Logger LOGGER = LoggerFactory.getLogger(GaranteService.class);
    private ModelMapper modelMapper;

    private ContratoRepository contratoRepository;
    private GaranteRepository garanteRepository;
    private UsuarioRepository usuarioRepository;
    private AuthUtil authUtil;
    private EmbeddingService embeddingService;

    public GaranteService(EmbeddingService embeddingService, AuthUtil authUtil, ModelMapper modelMapper, ContratoRepository contratoRepository, GaranteRepository garanteRepository,UsuarioRepository usuarioRepository) {
        this.authUtil = authUtil;
        this.modelMapper = modelMapper;
        this.contratoRepository = contratoRepository;
        this.garanteRepository = garanteRepository;
        this.usuarioRepository = usuarioRepository;
        this.embeddingService = embeddingService;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.typeMap(GaranteEntradaDto.class, Garante.class);
        modelMapper.typeMap(Garante.class, GaranteSalidaDto.class)
                .addMapping(Garante::getUsuario, GaranteSalidaDto::setUsuarioDtoSalida);
//                .addMapping(Garante::getImagenes, GaranteSalidaDto::setImagenes);



    }
    public void deleteByContratoId(Long contratoId) {
        garanteRepository.deleteByContratoId(contratoId);
    }

    private GaranteSalidaDto mapearGaranteSalidaDto(Garante garante) {
        GaranteSalidaDto dto = modelMapper.map(garante, GaranteSalidaDto.class);

        Usuario usuarioCuenta = garante.getUsuarioCuentaGarante();
        if (usuarioCuenta != null) {
            dto.setUsuarioCuentaId(usuarioCuenta.getId());
            dto.setUsuarioCuentaGarante(new GaranteUser(
                    usuarioCuenta.getUsername(),
                    usuarioCuenta.getPassword()
            ));
        } else {
            dto.setUsuarioCuentaId(null);
            dto.setUsuarioCuentaGarante(null);
        }

        return dto;
    }


    @Override
    public List<GaranteSalidaDto> buscarGarantePorUsuario(String username) {
        List<Garante> garanteList = garanteRepository.findGaranteByUsername(username);
        return garanteList.stream()
                .map(this::mapearGaranteSalidaDto)
                .toList();
    }

    @Override
    @Transactional
    public PageResponse<GaranteSalidaDto> listarGarantesXPagina(int page) throws ResourceNotFoundException {
        Long idUser = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", idUser);

        // opcional pero recomendable: validar que exista el usuario
        usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Si tu clase Persona tiene "id" como campo (lo normal), esto sirve.
        // Si el id real se llama distinto, sacá el Sort o poné el nombre correcto.
        Pageable pageable = PageRequest.of(page, 6, Sort.by(Sort.Direction.DESC, "id"));

        Page<Garante> pageResult = garanteRepository.findAllByUsuario_Id(idUser, pageable);

        return new PageResponse<>(
                pageResult.getContent().stream()
                        .map(this::mapearGaranteSalidaDto)
                        .toList(),
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );
    }
    @Transactional
    @Override
    public List<GaranteSalidaDto> listarGarantes() {
        List<Garante> garantes = garanteRepository.findAll();
        return garantes.stream()
                .map(garante -> {
                    GaranteSalidaDto dto = mapearGaranteSalidaDto(garante);

//                    List<ImgUrlSalidaDto> imagenesDto = garante.getImagenes()
//                            .stream()
//                            .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
//                            .toList();
//
//                    dto.setImagenes(imagenesDto);
                    return dto;
                })
                .toList();
    }
    @Override
    @Transactional
    public List<GaranteSalidaDto> listarGarantesPorUsuarioId(Long userId) {

        List<Garante> garantes = garanteRepository.findByUsuarioId(userId);

        return garantes.stream()
                .map(garante -> {
                    GaranteSalidaDto dto = mapearGaranteSalidaDto(garante);

                    Usuario usuario = garante.getUsuario();
                    if (usuario != null) {
                        var usuarioDto = modelMapper.map(usuario, com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida.class);

                        if (usuario.getLogoInmobiliaria() != null) {
                            usuarioDto.setLogo(usuario.getLogoInmobiliaria().getImageUrl());
                        }

                        dto.setUsuarioDtoSalida(usuarioDto);
                    }

                    return dto;
                })
                .toList();
    }
    @Override
    @Transactional
    public GaranteSalidaDto crearGarante(GaranteEntradaDto garanteEntradaDto) throws ResourceNotFoundException{
        Long idUser = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", idUser);

        Usuario usuario = usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Garante garante = new Garante();
        garante.setPronombre(garanteEntradaDto.getPronombre());
        garante.setNombre(garanteEntradaDto.getNombre());
        garante.setApellido(garanteEntradaDto.getApellido());
        garante.setEmail(garanteEntradaDto.getEmail());
        garante.setDni(garanteEntradaDto.getDni());
        garante.setTelefono(garanteEntradaDto.getTelefono());
        garante.setDireccionResidencial(garanteEntradaDto.getDireccionResidencial());
        garante.setCuit(garanteEntradaDto.getCuit());
        garante.setCargoActual(garanteEntradaDto.getCargoActual());
        garante.setCuitEmpresa(garanteEntradaDto.getCuitEmpresa());
        garante.setPronombre(garanteEntradaDto.getPronombre());
        garante.setLegajo((long) garanteEntradaDto.getLegajo());
        garante.setSectorActual(garanteEntradaDto.getSectorActual());
        garante.setNombreEmpresa(garanteEntradaDto.getNombreEmpresa());
        garante.setEstadoCivil(garanteEntradaDto.getEstadoCivil());
        garante.setNacionalidad(garanteEntradaDto.getNacionalidad());
        garante.setTipoGarantia(garanteEntradaDto.getTipoGarantia());
        garante.setTipoPropiedad(garanteEntradaDto.getTipoPropiedad());
        garante.setPartidaInmobiliaria(garanteEntradaDto.getPartidaInmobiliaria());
        garante.setEstadoOcupacion(garanteEntradaDto.getEstadoOcupacion());
        garante.setDireccion(garanteEntradaDto.getDireccion());
        garante.setInfoCatastral(garanteEntradaDto.getInfoCatastral());
        garante.setInformeDominio(garanteEntradaDto.getInformeDominio());
        garante.setInformeInhibicion(garanteEntradaDto.getInformeInhibicion());
        garante.setUsuario(usuario);

        Garante garanteToSave = garanteRepository.save(garante);
        LOGGER.info("Garante guardado para usuario: {}", garanteToSave.getUsuario().getUsername());
        CompletableFuture.runAsync(() -> {
            try {
                guardarGaranteEnSupabase(garanteToSave);
                upsertGaranteEmbedding(garanteToSave);
            } catch (Exception ex) {
                LOGGER.error("⚠️ Error sincronizando garante con Supabase: {}", ex.getMessage());
            }
        });
// Mapeo manual con fallback seguro para el UsuarioDtoSalida
        GaranteSalidaDto garanteSalidaDto = mapearGaranteSalidaDto(garanteToSave);

// Si el mapeo automático no funcionó, lo forzamos
        if (garanteSalidaDto.getUsuarioDtoSalida() == null && garanteToSave.getUsuario() != null) {
            UsuarioDtoSalida usuarioDtoSalida = modelMapper.map(garanteToSave.getUsuario(), UsuarioDtoSalida.class);
            garanteSalidaDto.setUsuarioDtoSalida(usuarioDtoSalida);
        }

// Logging seguro (sin NPEs 💣)
        String usernameLog = Optional.ofNullable(garanteSalidaDto.getUsuarioDtoSalida())
                .map(UsuarioDtoSalida::getUsername)
                .orElse("usuario-desconocido");

        LOGGER.info("GaranteSalidaDto generado para usuario: {}", usernameLog);

        return garanteSalidaDto;
    }
    private void guardarGaranteEnSupabase(Garante g) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> registro = Map.of(
                "id", g.getId(),
                "user_id", g.getUsuario().getId(),
                "nombre", g.getNombre(),
                "apellido", g.getApellido(),
                "email", g.getEmail(),
                "dni", g.getDni(),
                "telefono", g.getTelefono(),
                "tipo_garantia", g.getTipoGarantia(),
                "direccion", g.getDireccionResidencial()
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/garantes")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Garante guardado en Supabase: {}", g.getId());
        }
    }
    private String buildContenidoGarante(Garante g) {
        return "GARANTE|id=" + g.getId() +
                " | nombre=" + g.getNombre() + " " + g.getApellido() +
                " | dni=" + g.getDni() +
                " | tipo_garantia=" + g.getTipoGarantia();
    }
    private void upsertGaranteEmbedding(Garante g) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        List<Float> embedding = embeddingService.generarEmbedding(buildContenidoGarante(g));

        Map<String, Object> registro = Map.of(
                "id_garante", g.getId(),
                "user_id", g.getUsuario().getId(),
                "contenido", buildContenidoGarante(g),
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/garantes_embeddings")
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
            LOGGER.info("✅ Embedding garante creado: {}", g.getId());
        }
    }
    @org.springframework.transaction.annotation.Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {
        List<Garante> garantes = garanteRepository.findByUsuarioId(userId);

        garantes.forEach(c -> {
            try {
                upsertGaranteEmbedding(c);
            } catch (Exception e) {
                LOGGER.error("Error generando embedding para contrato {}: {}", c.getId(), e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} garantes del usuario {}", garantes.size(), userId);
    }

    @Transactional
    @Override
    public GaranteSalidaDto listarGarantePorId(Long id) throws ResourceNotFoundException {
        Garante garante = garanteRepository.findById(id).orElse(null);
        GaranteSalidaDto garanteSalidaDto = null;
        if(garante !=null){
            garanteSalidaDto = mapearGaranteSalidaDto(garante);

//            List<ImgUrlSalidaDto> imagenesDto = garante.getImagenes()
//                    .stream()
//                    .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
//                    .toList();
//
//            garanteSalidaDto.setImagenes(imagenesDto);

        }else{
            throw new ResourceNotFoundException("No se encontró el garante con el ID proporcionado");
        }
        return garanteSalidaDto;
    }

    @Override
    public void eliminarGarante(Long id) throws ResourceNotFoundException {
        Garante garante = garanteRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No se encontro el garante con el id: " + id));
        garanteRepository.delete(garante);
        CompletableFuture.runAsync(() -> {
            try {
                eliminarGaranteEmbeddingSupabase(id);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error eliminando embedding de inquilino: {}", e.getMessage());
            }
        });
    }
    private void eliminarGaranteEmbeddingSupabase(Long id) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/garantes_embeddings?id_garante=eq." + id)
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
            LOGGER.info("🗑️ Embedding de garante eliminado: {}", id);
        }
    }
    @Override
    public void asignarGarante(Long idGarante, Long idContrato) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el contrato con el id proporcionado!!"));

        Garante garante = garanteRepository.findById(idGarante)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el garante con el id proporcionado!!"));

        // Relación bidireccional
        garante.setContrato(contrato);

        List<Garante> garantes = contrato.getGarantes();
        if (garantes == null) {
            garantes = new ArrayList<>();  // Inicializa la lista si es null
        }
        garantes.add(garante); // HashSet evita duplicados automáticamente

        contrato.setGarantes(garantes);

        contratoRepository.save(contrato);
        garanteRepository.save(garante);
    }


    @Override
    @Transactional
    public GaranteSalidaDto editarGarante(GaranteDtoModificacion garanteDtoModificacion) throws ResourceNotFoundException {

        Garante garante = garanteRepository.findById(garanteDtoModificacion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el garante con el id proporcionado!!"));


        garante.setNombre(garanteDtoModificacion.getNombre());
        garante.setApellido(garanteDtoModificacion.getApellido());
        garante.setDni(garanteDtoModificacion.getDni());
        garante.setCuit(garanteDtoModificacion.getCuit());
        garante.setEmail(garanteDtoModificacion.getEmail());
        garante.setTelefono(garanteDtoModificacion.getTelefono());
        garante.setDireccionResidencial(garanteDtoModificacion.getDireccionResidencial());

        Garante garanteToSave = garanteRepository.save(garante);
        GaranteSalidaDto garanteSalidaDto = mapearGaranteSalidaDto(garanteToSave);

        CompletableFuture.runAsync(() -> {
            try {
                upsertGaranteEmbedding(garanteToSave);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error actualizando embedding garante en Supabase: {}", e.getMessage());
            }
        });
        return garanteSalidaDto;
    }


    @Override
    @Transactional
    public GaranteUser listarCredenciales(Long garanteId) throws ResourceNotFoundException {

        Garante garante = garanteRepository.findById(garanteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el garante con el ID proporcionado"));

        Usuario usuarioCuenta = garante.getUsuarioCuentaGarante();

        if (usuarioCuenta == null) {
            throw new ResourceNotFoundException("El garante con ID " + garanteId + " no tiene un usuario asociado");
        }

        GaranteUser dto = new GaranteUser();
        dto.setUsername(usuarioCuenta.getUsername());
        dto.setPassword(usuarioCuenta.getPassword());

        return dto;
    }

    @Override
    @Transactional
    public void eliminarUsuarioCuentaGarante(Long usuarioId) {
        garanteRepository.desvincularUsuarioCuentaGarante(usuarioId);

        if (usuarioRepository.existsById(usuarioId)) {
            usuarioRepository.deleteById(usuarioId);
        } else {
            throw new ResourceNotFoundException("No se encontró el usuario con ID " + usuarioId);
        }
    }

    public Garante clonarGarante(Garante origen) {
        if (origen == null) return null;

        Garante g = new Garante();

        // =========================
        // Campos heredados de Persona
        // =========================
        g.setPronombre(origen.getPronombre());
        g.setNombre(origen.getNombre());
        g.setApellido(origen.getApellido());
        g.setTelefono(origen.getTelefono());
        g.setEmail(origen.getEmail());
        g.setDni(origen.getDni());
        g.setCuit(origen.getCuit());
        g.setDireccionResidencial(origen.getDireccionResidencial());
        g.setNacionalidad(origen.getNacionalidad());
        g.setEstadoCivil(origen.getEstadoCivil());

        // =========================
        // Campos propios de Garante
        // =========================
        g.setTipoGarantia(origen.getTipoGarantia());

        g.setNombreEmpresa(origen.getNombreEmpresa());
        g.setSectorActual(origen.getSectorActual());
        g.setCargoActual(origen.getCargoActual());
        g.setLegajo(origen.getLegajo());
        g.setCuitEmpresa(origen.getCuitEmpresa());

        g.setPartidaInmobiliaria(origen.getPartidaInmobiliaria());
        g.setDireccion(origen.getDireccion());
        g.setInfoCatastral(origen.getInfoCatastral());
        g.setEstadoOcupacion(origen.getEstadoOcupacion());
        g.setTipoPropiedad(origen.getTipoPropiedad());
        g.setInformeDominio(origen.getInformeDominio());
        g.setInformeInhibicion(origen.getInformeInhibicion());

        // =========================
        // Relaciones (NO clonar docs)
        // =========================
        g.setDocumentos(new ArrayList<>()); // renovación: documentos nuevos si hace falta

        // contrato y usuario se setean afuera (cuando ya tenés el nuevo contrato)
        return g;
    }


}
