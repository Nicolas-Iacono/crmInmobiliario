package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.modificacion.InquilinoDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoUser;
import com.backend.crmInmobiliario.DTO.salida.pages.PageResponse;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IInquilinoService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.backend.crmInmobiliario.service.impl.ContratoService.opt;


@Service
public class InquilinoService implements IInquilinoService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${openai.api.key}")
    private String OPENAI_APIKEY;


    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private ModelMapper modelMapper;
    private InquilinoRepository inquilinoRepository;
    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;
    private AuthUtil authUtil;
    private EmbeddingService embeddingService;

    public InquilinoService(EmbeddingService embeddingService, AuthUtil authUtil,  UsuarioRepository usuarioRepository,ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository) {
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.authUtil = authUtil;
        this.embeddingService = embeddingService;
        configureMapping();

    }

    private void configureMapping() {
    }

    @Override
    @Transactional
    public List<InquilinoSalidaDto> listarInquilinos() {
        List<Inquilino> inquilinos = inquilinoRepository.findAll();
        return inquilinos.stream()
                .map(garante -> {
                    InquilinoSalidaDto dto = modelMapper.map(inquilinos, InquilinoSalidaDto.class);

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
    public InquilinoSalidaDto crearInquilino(InquilinoEntradaDto inquilinoEntradaDto) throws ResourceNotFoundException {


        Long idUser = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", idUser);

        Usuario usuario = usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));


        Inquilino inquilino = new Inquilino();
        inquilino.setPronombre(inquilinoEntradaDto.getPronombre());
        inquilino.setNombre(inquilinoEntradaDto.getNombre());
        inquilino.setApellido(inquilinoEntradaDto.getApellido());
        inquilino.setEmail(inquilinoEntradaDto.getEmail());
        inquilino.setDni(inquilinoEntradaDto.getDni());
        inquilino.setTelefono(inquilinoEntradaDto.getTelefono());
        inquilino.setDireccionResidencial(inquilinoEntradaDto.getDireccionResidencial());
        inquilino.setCuit(inquilinoEntradaDto.getCuit());
        inquilino.setEstadoCivil(inquilinoEntradaDto.getEstadoCivil());
        inquilino.setNacionalidad(inquilinoEntradaDto.getNacionalidad());
        inquilino.setActivo(false);
        inquilino.setUsuario(usuario);

        Inquilino inquilinoToSave = inquilinoRepository.save(inquilino);

        InquilinoSalidaDto inquilinoSalidaDto = modelMapper.map(inquilinoToSave, InquilinoSalidaDto.class);

        CompletableFuture.runAsync(() -> {
            try {
                upsertInquilinoEmbedding(inquilinoToSave);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error guardando embedding de inquilino en Supabase: {}", e.getMessage());
            }
        });

        return inquilinoSalidaDto;
    }

    private void guardarInquilinoEnSupabase(Inquilino inquilino) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> registro = new HashMap<>();
        registro.put("id", inquilino.getId());
        registro.put("user_id", inquilino.getUsuario().getId()); // ✅ Foreign Key correcta
        registro.put("nombre", inquilino.getNombre());
        registro.put("apellido", inquilino.getApellido());
        registro.put("email", inquilino.getEmail());
        registro.put("telefono", inquilino.getTelefono());
        registro.put("dni", inquilino.getDni());
        registro.put("direccion_residencial", inquilino.getDireccionResidencial());
        registro.put("nacionalidad", inquilino.getNacionalidad());
        registro.put("estado_civil", inquilino.getEstadoCivil());
        registro.put("fecha_registro", LocalDate.now().toString());

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/inquilinos")
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Inquilino guardado en Supabase: {}", inquilino.getId());
        }
    }
    private String buildContenidoInquilino(Inquilino i) {
        return "INQUILINO|" +
                "id=" + i.getId() +
                " | nombre=" + opt(i.getNombre()) +
                " | apellido=" + opt(i.getApellido()) +
                " | email=" + opt(i.getEmail()) +
                " | dni=" + opt(i.getDni()) +
                " | cuil=" + opt(i.getCuit()) +
                " | pronombre=" + opt(i.getPronombre()) +
                " | telefono=" + opt(i.getTelefono()) +
                " | direccion=" + opt(i.getDireccionResidencial()) +
                " | nacionalidad=" + opt(i.getNacionalidad()) +
                " | estado_civil=" + opt(i.getEstadoCivil()) +
                " | activo=" + (i.isActivo() ? "si" : "no");
    }
    @Override
    @Transactional
    public InquilinoSalidaDto buscarInquilinoPorId(Long id) throws ResourceNotFoundException {
        List<Inquilino> inquilinos = inquilinoRepository.findByUsuarioId(id);

        return (InquilinoSalidaDto) inquilinos.stream()
                .map(inquilino -> {
                    InquilinoSalidaDto dto = modelMapper.map(inquilino, InquilinoSalidaDto.class);

                    Usuario usuario = inquilino.getUsuario();
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
    private void upsertInquilinoEmbedding(Inquilino i) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String contenido = buildContenidoInquilino(i);
        List<Float> embedding = embeddingService.generarEmbedding(contenido);

        Map<String, Object> registro = Map.of(
                "id_inquilino", i.getId(),
                "user_id", i.getUsuario().getId(),
                "contenido", contenido,
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/inquilinos_embeddings")
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
            LOGGER.info("✅ Embedding inquilino actualizado: {}", i.getId());
        }
    }
    @Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {
        List<Inquilino> inquilinos = inquilinoRepository.findByUsuarioId(userId);

        inquilinos.forEach(c -> {
            try {
                upsertInquilinoEmbedding(c);
            } catch (Exception e) {
                LOGGER.error("Error generando embedding para contrato {}: {}", c.getId(), e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} contratos del usuario {}", inquilinos.size(), userId);
    }
    @Override
    @Transactional
    public InquilinoSalidaDto editarInquilino(InquilinoDtoModificacion inquilinoDtoModificacion) throws ResourceNotFoundException {

        Inquilino inquilino = inquilinoRepository.findById(inquilinoDtoModificacion.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el inquilino con el id proporcionado!!"));


        inquilino.setNombre(inquilinoDtoModificacion.getNombre());
        inquilino.setApellido(inquilinoDtoModificacion.getApellido());
        inquilino.setDni(inquilinoDtoModificacion.getDni());
        inquilino.setCuit(inquilinoDtoModificacion.getCuit());
        inquilino.setEmail(inquilinoDtoModificacion.getEmail());
        inquilino.setTelefono(inquilinoDtoModificacion.getTelefono());
        inquilino.setDireccionResidencial(inquilinoDtoModificacion.getDireccionResidencial());

        Inquilino inquilinoToSave = inquilinoRepository.save(inquilino);
        InquilinoSalidaDto inquilinoSalidaDto = modelMapper.map(inquilinoToSave, InquilinoSalidaDto.class);
        CompletableFuture.runAsync(() -> {
            try {
                upsertInquilinoEmbedding(inquilinoToSave);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error actualizando embedding inquilino en Supabase: {}", e.getMessage());
            }
        });
        return inquilinoSalidaDto;
    }


    @Override
    @Transactional
    public void eliminarInquilino(Long id) throws ResourceNotFoundException {
        Inquilino inquilino = inquilinoRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el inquilino con el id proporcionado!!"));
        inquilinoRepository.delete(inquilino);
        CompletableFuture.runAsync(() -> {
            try {
                eliminarInquilinoEmbeddingSupabase(id);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error eliminando embedding de inquilino: {}", e.getMessage());
            }
        });
    }

    private void eliminarInquilinoEmbeddingSupabase(Long id) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/inquilinos_embeddings?id_inquilino=eq." + id)
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
            LOGGER.info("🗑️ Embedding de inquilino eliminado: {}", id);
        }
    }

    @Override
    @Transactional
    public List<InquilinoSalidaDto> buscarInquilinoPorUsuario(String username) {
        List<Inquilino> inquilinoList = inquilinoRepository.findInquilinoByUsername(username);
        return inquilinoList.stream()
                .map(inquilino -> modelMapper.map(inquilino, InquilinoSalidaDto.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public InquilinoSalidaDto buscarInquilinoPorCuenta(String username) {
        Inquilino inquilino = inquilinoRepository.findByUsuarioCuentaInquilinoUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró inquilino para este usuario"));

        return modelMapper.map(inquilino, InquilinoSalidaDto.class);
    }


    @Override
    @Transactional
    public Integer enumerarInquilinos() {
        Long userId = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", userId);

        return inquilinoRepository.countByUsuarioId(userId);
    }



    @Transactional
    @Override
    public InquilinoUser listarCredenciales(Long inquilinoId) throws ResourceNotFoundException {

        Inquilino inquilino = inquilinoRepository.findById(inquilinoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el inquilino con el ID proporcionado"));

        Usuario usuarioCuenta = inquilino.getUsuarioCuentaInquilino();

        if (usuarioCuenta == null) {
            throw new ResourceNotFoundException("El inquilino con ID " + inquilinoId + " no tiene un usuario asociado");
        }

        InquilinoUser dto = new InquilinoUser();
        dto.setUsername(usuarioCuenta.getUsername());
        dto.setPassword(usuarioCuenta.getPassword());

        return dto;
    }

    @Transactional
    public void eliminarUsuarioCuentaInquilino(Long usuarioId) {
        // 1️⃣ Romper la relación antes de borrar
        inquilinoRepository.desvincularUsuarioCuenta(usuarioId);

        // 2️⃣ Eliminar el usuario si existe
        if (usuarioRepository.existsById(usuarioId)) {
            usuarioRepository.deleteById(usuarioId);
        } else {
            throw new ResourceNotFoundException("No se encontró el usuario con ID " + usuarioId);
        }
    }

    @Transactional(readOnly = true)
    public List<InquilinoSalidaDto> listarInquilinosPorUsuarioId(Long userId) {
        List<Inquilino> inquilinos = inquilinoRepository.findByUsuarioId(userId);

        return inquilinos.stream()
                .map(inquilino -> modelMapper.map(inquilino, InquilinoSalidaDto.class))
                .toList();
    }

    @Override
    @Transactional
    public PageResponse<InquilinoSalidaDto> listarInquilinosXPagina(int page) throws ResourceNotFoundException {
        Long idUser = authUtil.extractUserId();
        LOGGER.info("✅ User ID desde JWT: {}", idUser);

        // opcional pero recomendable: validar que exista el usuario
        usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Si tu clase Persona tiene "id" como campo (lo normal), esto sirve.
        // Si el id real se llama distinto, sacá el Sort o poné el nombre correcto.
        Pageable pageable = PageRequest.of(page, 6, Sort.by(Sort.Direction.DESC, "id"));

        Page<Inquilino> pageResult = inquilinoRepository.findAllByUsuario_Id(idUser, pageable);

        return new PageResponse<>(
                pageResult.getContent().stream()
                        .map(p -> modelMapper.map(p, InquilinoSalidaDto.class))
                        .toList(),
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );
    }
}
