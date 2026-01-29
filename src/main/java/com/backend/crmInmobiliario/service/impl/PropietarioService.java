package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropietarioDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IPropietarioService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.backend.crmInmobiliario.service.impl.ContratoService.opt;


@Service
public class PropietarioService implements IPropietarioService {
    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;
    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private ModelMapper modelMapper;
    private InquilinoRepository inquilinoRepository;
    private PropiedadRepository propiedadRepository;
    private PropietarioRepository propietarioRepository;
    private UsuarioRepository usuarioRepository;
    private AuthUtil authUtil;
    private EmbeddingService embeddingService;
    private ObjectMapper mapper;
    public PropietarioService(EmbeddingService embeddingService, AuthUtil authUtil, ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository, PropietarioRepository propietarioRepository, UsuarioRepository usuarioRepository) {
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.propietarioRepository = propietarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.embeddingService = embeddingService;
        configureMapping();
    }

    private void configureMapping() {
    }

    @Transactional
    @Override
    public List<PropietarioSalidaDto> listarPropietarios() {
       List<Propietario> propietarios = propietarioRepository.findAll();
        return propietarios.stream()
                .map(garante -> {
                    PropietarioSalidaDto dto = modelMapper.map(propietarios, PropietarioSalidaDto.class);

                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public PropietarioSalidaDto crearPropietario(PropietarioEntradaDto propietarioEntradaDto) throws ResourceNotFoundException {

        Long idUser = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", idUser);

        Usuario usuario = usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Propietario propietario = new Propietario();
        propietario.setPronombre(propietarioEntradaDto.getPronombre());
        propietario.setNombre(propietarioEntradaDto.getNombre());
        propietario.setApellido(propietarioEntradaDto.getApellido());
        propietario.setEmail(propietarioEntradaDto.getEmail());
        propietario.setTelefono(propietarioEntradaDto.getTelefono());
        propietario.setDni(propietarioEntradaDto.getDni());
        propietario.setCuit(propietarioEntradaDto.getCuit());
        propietario.setDireccionResidencial(propietarioEntradaDto.getDireccionResidencial());
        propietario.setNacionalidad(propietarioEntradaDto.getNacionalidad());
        propietario.setEstadoCivil(propietarioEntradaDto.getEstadoCivil());
        propietario.setUsuario(usuario);

        Propietario guardado = propietarioRepository.save(propietario);
        CompletableFuture.runAsync(() -> {
            try {
                upsertPropietarioEmbedding(guardado);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error generando embedding de propietario: {}", e.getMessage());
            }
        });
        PropietarioSalidaDto dto = modelMapper.map(guardado, PropietarioSalidaDto.class);

        var usuarioDto = modelMapper.map(usuario, com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida.class);
        if (usuario.getLogoInmobiliaria() != null) {
            usuarioDto.setLogo(usuario.getLogoInmobiliaria().getImageUrl());
        }
        dto.setUsuarioDtoSalida(usuarioDto);

        return dto;
    }
    private String buildContenidoPropietario(Propietario p) {
        return "PROPIETARIO|" +
                "id=" + p.getId() +
                " | nombre=" + opt(p.getNombre()) +
                " | apellido=" + opt(p.getApellido()) +
                " | email=" + opt(p.getEmail()) +
                " | dni=" + opt(p.getDni()) +
                " | cuil=" + opt(p.getCuit()) +
                " | pronombre=" + opt(p.getPronombre()) +
                " | telefono=" + opt(p.getTelefono()) +
                " | direccion=" + opt(p.getDireccionResidencial()) +
                " | nacionalidad=" + opt(p.getNacionalidad()) +
                " | estado_civil=" + opt(p.getEstadoCivil());
    }

    private void upsertPropietarioEmbedding(Propietario p) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String contenido = buildContenidoPropietario(p);
        List<Float> embedding = embeddingService.generarEmbedding(contenido);

        Map<String, Object> registro = Map.of(
                "id_propietario", p.getId(),
                "user_id", p.getUsuario().getId(),
                "contenido", contenido,
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/propietarios_embeddings")
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
            LOGGER.info("✅ Embedding propietario actualizado en Supabase: {}", p.getId());
        }
    }
    @Transactional
    @Override
    public PropietarioSalidaDto buscarPropietarioPorId(Long id) throws ResourceNotFoundException {
        Propietario propietario = propietarioRepository.findById(id).orElse(null);
        PropietarioSalidaDto propietarioSalidaDto = null;
        if(propietario != null){
            propietarioSalidaDto = modelMapper.map(propietario, PropietarioSalidaDto.class);

//            List<ImgUrlSalidaDto> imagenesDto = propietario.getImagenes()
//                    .stream()
//                    .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
//                    .toList();
//
//            propietarioSalidaDto.setImagenes(imagenesDto);

        }else{
            throw new ResourceNotFoundException("No se encontró el propietario con el ID proporcionado");
        }
        return propietarioSalidaDto;
    }



    @Override
    @Transactional
    public List<PropietarioSalidaDto> buscarPropietariosPorUsuario(String username) {
        List<Propietario> propietarioList = propietarioRepository.findPropietarioByUsername(username);
        return propietarioList.stream()
                .map(propietario -> modelMapper.map(propietario, PropietarioSalidaDto.class))
                .toList();
    }


    @Override
    @Transactional
    public Integer enumerarPropietarios(String username) {
        return propietarioRepository.countByUsuarioUsername(username);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public PropietarioSalidaDto editarPropietario(PropietarioDtoModificacion propietarioDtoModificacion ) throws ResourceNotFoundException {

        Propietario propietario = propietarioRepository.findById(propietarioDtoModificacion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el inquilino con el id proporcionado!!"));


        propietario.setNombre(propietarioDtoModificacion.getNombre());
        propietario.setApellido(propietarioDtoModificacion.getApellido());
        propietario.setDni(propietarioDtoModificacion.getDni());
        propietario.setCuit(propietarioDtoModificacion.getCuit());
        propietario.setEmail(propietarioDtoModificacion.getEmail());
        propietario.setTelefono(propietarioDtoModificacion.getTelefono());
        propietario.setDireccionResidencial(propietarioDtoModificacion.getDireccionResidencial());

        Propietario propietarioToSave = propietarioRepository.save(propietario);
        PropietarioSalidaDto propietarioSalidaDto = modelMapper.map(propietarioToSave, PropietarioSalidaDto.class);

        CompletableFuture.runAsync(() -> {
            try {
                upsertPropietarioEmbedding(propietarioToSave);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error actualizando embedding propietario en Supabase: {}", e.getMessage());
            }
        });
        return propietarioSalidaDto;
    }

    @Transactional
    @Override
    public void eliminarPropietario(Long id) throws ResourceNotFoundException {
        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        // ✅ 1) Antes de borrar: cortar relación con propiedades
        if (propietario.getPropiedades() != null) {
            propietario.getPropiedades().forEach(p -> p.setPropietario(null));
            propietario.getPropiedades().clear();
        }

        // ✅ 2) Eliminar en MySQL
        propietarioRepository.delete(propietario);

        // ✅ 3) Eliminar embedding en Supabase de manera asíncrona
        CompletableFuture.runAsync(() -> {
            try {
                eliminarPropietarioEmbeddingSupabase(id);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error eliminando embedding en Supabase: {}", e.getMessage());
            }
        });

        LOGGER.info("🗑️ Propietario eliminado correctamente: {}", id);
    }

    @Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {
        List<Propietario> propietarios = propietarioRepository.findByUsuarioId(userId);

        propietarios.forEach(c -> {
            try {
                upsertPropietarioEmbedding(c);
            } catch (Exception e) {
                LOGGER.error("Error generando embedding para contrato {}: {}", c.getId(), e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} contratos del usuario {}", propietarios.size(), userId);
    }


    private void eliminarPropietarioEmbeddingSupabase(Long id) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/propietarios_embeddings?id_propietario=eq." + id)
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
            LOGGER.info("🗑️ Embedding propietario eliminado: {}", id);
        }
    }
    @Transactional
    @Override
    public Object[] obtenerCredencialesPorPropietario(Long propietarioId) {
        return usuarioRepository.obtenerCredencialesPorPropietario(propietarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No se encontraron credenciales para el propietario con ID " + propietarioId)
                );
    }

    @Transactional
    @Override
    public PropietarioUser listarCredenciales(Long propietarioId) throws ResourceNotFoundException {

        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el propietario con el ID proporcionado"));

        Usuario usuarioCuenta = propietario.getUsuarioCuentaPropietario();

        if (usuarioCuenta == null) {
            throw new ResourceNotFoundException("El propietario con ID " + propietarioId + " no tiene un usuario asociado");
        }

        PropietarioUser dto = new PropietarioUser();
        dto.setUsername(usuarioCuenta.getUsername());
        dto.setPassword(usuarioCuenta.getPassword());

        return dto;
    }

    @Transactional
    public void eliminarUsuarioCuentaPropietario(Long usuarioId) {
        // 1️⃣ Romper la relación antes de borrar
        propietarioRepository.desvincularUsuarioCuentaPropietario(usuarioId);

        // 2️⃣ Eliminar el usuario
        if (usuarioRepository.existsById(usuarioId)) {
            usuarioRepository.deleteById(usuarioId);
        } else {
            throw new ResourceNotFoundException("No se encontró el usuario con ID " + usuarioId);
        }
    }

    public PropietarioSalidaDto crearPropietario(PropietarioEntradaDto dto, Long userId) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Propietario propietario = new Propietario();
        propietario.setPronombre(dto.getPronombre());
        propietario.setNombre(dto.getNombre());
        propietario.setApellido(dto.getApellido());
        propietario.setTelefono(dto.getTelefono());
        propietario.setEmail(dto.getEmail());
        propietario.setUsuario(usuario);

        Propietario guardado = propietarioRepository.save(propietario);

        return modelMapper.map(guardado, PropietarioSalidaDto.class);
    }

    @Override
    @Transactional
    public List<PropietarioSalidaDto> listarPropietariosPorUsuarioId(Long userId) {
        List<Propietario> propietarios = propietarioRepository.findByUsuarioId(userId);

        return propietarios.stream()
                .map(propietario -> {
                    PropietarioSalidaDto dto = modelMapper.map(propietario, PropietarioSalidaDto.class);

                    Usuario usuario = propietario.getUsuario();
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
}

