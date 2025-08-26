package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.repository.*;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.utils.MultipartInputStreamFileResource;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IImageUrlsService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ImagenService implements IImageUrlsService {
    public static final MediaType MEDIA_TYPE_WEBP = new MediaType("image", "webp");
    private final static Logger LOGGER = LoggerFactory.getLogger(IImageUrlsService.class);
    private final ModelMapper mapper;
    private final GaranteRepository garanteRepository;
    private final InquilinoRepository inquilinoRepository;
    private final PropietarioRepository propietarioRepository;
    private final PropiedadRepository propiedadRepository;
    private final ImageUrlsRepository imageUrlsRepository;
    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public byte[] convertirImagenExternamente(MultipartFile archivo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(archivo.getInputStream(), archivo.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://webp-converter-production.up.railway.app/convert", HttpMethod.POST, requestEntity, byte[].class);

        return response.getBody();
    }
    @Transactional
    public String subirAStorageSupabase(byte[] imagenWebp, String nombreArchivo) throws IOException {
        String SUPABASE_URL = "https://kksdxwqcgrbemlpgjifr.supabase.co";
        String BUCKET = "documentos";
        String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtrc2R4d3FjZ3JiZW1scGdqaWZyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4MzM4NDEsImV4cCI6MjA2MDQwOTg0MX0.1VxlR2YSdnwvVkAZ0df8eZo3PBiMr90sbr9PgTQhQ-U"; // service role key
        String STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + nombreArchivo;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MEDIA_TYPE_WEBP);
        headers.setBearerAuth(API_KEY);

        HttpEntity<byte[]> request = new HttpEntity<>(imagenWebp, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                STORAGE_ENDPOINT, HttpMethod.PUT, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + nombreArchivo;
        } else {
            throw new IOException("No se pudo subir la imagen a Supabase. Código: " + response.getStatusCode());
        }
    }



    @Transactional
    public void eliminarDeStorageSupabase(String imageUrl) throws IOException {
        String SUPABASE_URL = "https://kksdxwqcgrbemlpgjifr.supabase.co/";
        String BUCKET = "documentos";
        String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtrc2R4d3FjZ3JiZW1scGdqaWZyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4MzM4NDEsImV4cCI6MjA2MDQwOTg0MX0.1VxlR2YSdnwvVkAZ0df8eZo3PBiMr90sbr9PgTQhQ-U"; // 🔐 Service role key

        // Extraer solo el path después del bucket
        String relativePath = imageUrl.split("/object/public/" + BUCKET + "/")[1];

        String STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + relativePath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                STORAGE_ENDPOINT,
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("No se pudo eliminar la imagen de Supabase. Código: " + response.getStatusCode());
        }
    }
    public ImagenService(ModelMapper mapper, GaranteRepository garanteRepository, InquilinoRepository inquilinoRepository, PropietarioRepository propietarioRepository, PropiedadRepository propiedadRepository, ImageUrlsRepository imageUrlsRepository, NotaRepository notaRepository, UsuarioRepository usuarioRepository) {
        this.mapper = mapper;
        this.garanteRepository = garanteRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.propietarioRepository = propietarioRepository;
        this.propiedadRepository = propiedadRepository;
        this.imageUrlsRepository = imageUrlsRepository;
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;

        configureMapping();
    }

    private void configureMapping() {


    }
    @Transactional
    @Override
    public List<ImgUrlSalidaDto> subirImagenesYAsociarANota(Long notaId, MultipartFile[] archivos) throws IOException, ResourceNotFoundException {
        Nota notaBuscada = notaRepository.findById(notaId).orElseThrow(() -> new ResourceNotFoundException("No se encontro la nota con el ID " + notaId));

        List<ImgUrlSalidaDto> nuevasImagenesDTO = new ArrayList<>();

        for (MultipartFile archivo : archivos) {
            byte[] webp = convertirImagenExternamente(archivo);
            String nombreArchivo = UUID.randomUUID() + ".webp";
            String url = subirAStorageSupabase(webp, nombreArchivo);

            ImageUrls imagen = new ImageUrls();
            imagen.setImageUrl(url);
            imagen.setNombreOriginal(archivo.getOriginalFilename());
            imagen.setTipoImagen("GENERICA");
            imagen.setFechaSubida(LocalDateTime.now());
            imagen.setNota(notaBuscada);
            // Guardás imagen directamente, así obtenés el ID al instante
            ImageUrls imagenGuardada = imageUrlsRepository.save(imagen);

            // También actualizás la colección en la propiedad
            notaBuscada.getImagenes().add(imagenGuardada);

            // DTO
            ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
            dto.setIdImage(imagenGuardada.getIdImage());
            dto.setImageUrl(imagenGuardada.getImageUrl());
            dto.setNombreOriginal(imagenGuardada.getNombreOriginal());
            dto.setTipoImagen(imagenGuardada.getTipoImagen());
            dto.setFechaSubida(imagenGuardada.getFechaSubida());

            nuevasImagenesDTO.add(dto);
        }

        notaRepository.save(notaBuscada);

        return nuevasImagenesDTO;
    }

    @Override
    @Transactional
    public ImgUrlSalidaDto subirLogo(Long usuarioId, MultipartFile archivo) throws IOException, ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID " + usuarioId));

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no fue proporcionado.");
        }

        // Eliminar logo existente si ya tiene uno
        ImageUrls logoExistente = usuario.getLogoInmobiliaria();
        if (logoExistente != null) {
            // Eliminar del storage Supabase
            eliminarDeStorageSupabase(logoExistente.getImageUrl());

            // Desasociar del usuario y eliminar de la BD
            usuario.setLogoInmobiliaria(null);
            imageUrlsRepository.delete(logoExistente);
            imageUrlsRepository.flush();
        }

        // Convertir imagen a formato webp
        byte[] webp = convertirImagenExternamente(archivo);

        // Subir nueva imagen al storage
        String nombreArchivo = UUID.randomUUID() + ".webp";
        String url = subirAStorageSupabase(webp, nombreArchivo);

        // Crear entidad de imagen
        ImageUrls nuevaImagen = new ImageUrls();
        nuevaImagen.setImageUrl(url);
        nuevaImagen.setNombreOriginal(archivo.getOriginalFilename());
        nuevaImagen.setTipoImagen("GENERICA");
        nuevaImagen.setFechaSubida(LocalDateTime.now());
        nuevaImagen.setUsuario(usuario);

        // Guardar imagen en BD y asociarla al usuario
        ImageUrls imagenGuardada = imageUrlsRepository.save(nuevaImagen);
        usuario.setLogoInmobiliaria(imagenGuardada);
        usuarioRepository.save(usuario);

        // Crear y devolver DTO
        ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
        dto.setIdImage(imagenGuardada.getIdImage());
        dto.setImageUrl(imagenGuardada.getImageUrl());
        dto.setNombreOriginal(imagenGuardada.getNombreOriginal());
        dto.setFechaSubida(imagenGuardada.getFechaSubida());
        dto.setTipoImagen(imagenGuardada.getTipoImagen());

        return dto;
    }

    // Guardás imagen directamente, así obtenés el ID al instante
//    ImageUrls imagenGuardada = imageUrlsRepository.save(imagen);
//
//    // También actualizás la colección en la propiedad
//        propiedad.getImagenes().add(imagenGuardada);
//
//    // DTO
//    ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
//        dto.setIdImage(imagenGuardada.getIdImage());
//        dto.setImageUrl(imagenGuardada.getImageUrl());
//        dto.setNombreOriginal(imagenGuardada.getNombreOriginal());
//        dto.setTipoImagen(imagenGuardada.getTipoImagen());
//        dto.setFechaSubida(imagenGuardada.getFechaSubida());
//
//        nuevasImagenesDTO.add(dto);
//}
//
//    propiedadRepository.save(propiedad);
//
//            return nuevasImagenesDTO;
    @Transactional
public List<ImgUrlSalidaDto> subirImagenesYAsociarAPropiedad(Long propiedadId, MultipartFile[] archivos)
        throws IOException, ResourceNotFoundException {

    Propiedad propiedad = propiedadRepository.findById(propiedadId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró la propiedad con ID " + propiedadId));

    List<ImgUrlSalidaDto> nuevasImagenesDTO = new ArrayList<>();

    for (MultipartFile archivo : archivos) {
        byte[] webp = convertirImagenExternamente(archivo);
        String nombreArchivo = UUID.randomUUID() + ".webp";
        String url = subirAStorageSupabase(webp, nombreArchivo);

        ImageUrls imagen = new ImageUrls();
        imagen.setImageUrl(url);
        imagen.setNombreOriginal(archivo.getOriginalFilename());
        imagen.setTipoImagen("GENERICA");
        imagen.setFechaSubida(LocalDateTime.now());
        imagen.setPropiedad(propiedad);

        // Guardás imagen directamente, así obtenés el ID al instante
        ImageUrls imagenGuardada = imageUrlsRepository.save(imagen);

        // También actualizás la colección en la propiedad
        propiedad.getImagenes().add(imagenGuardada);

        // DTO
        ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
        dto.setIdImage(imagenGuardada.getIdImage());
        dto.setImageUrl(imagenGuardada.getImageUrl());
        dto.setNombreOriginal(imagenGuardada.getNombreOriginal());
        dto.setTipoImagen(imagenGuardada.getTipoImagen());
        dto.setFechaSubida(imagenGuardada.getFechaSubida());

        nuevasImagenesDTO.add(dto);
    }

    propiedadRepository.save(propiedad);

    return nuevasImagenesDTO;
}

    @Override
    @Transactional
    public void eliminarLogo(Long usuarioId) throws ResourceNotFoundException, IOException {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID " + usuarioId));

        ImageUrls logoActual = usuario.getLogoInmobiliaria();
        if (logoActual != null) {
            // Opcional: eliminar archivo del storage
            eliminarDeStorageSupabase(logoActual.getImageUrl());

            usuario.setLogoInmobiliaria(null);
            usuarioRepository.save(usuario);

            imageUrlsRepository.delete(logoActual);
        }
    }

    @Transactional
    public void eliminarImagenDePropiedad(Long propiedadId, Long idImagen) throws ResourceNotFoundException, IOException {
        // 1. Buscar la imagen
        ImageUrls imagen = imageUrlsRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la imagen con ID " + idImagen));

        // 2. Verificar si la imagen pertenece a la propiedad indicada
        if (imagen.getPropiedad() == null || !imagen.getPropiedad().getId_propiedad().equals(propiedadId)) {
            throw new ResourceNotFoundException("La imagen no pertenece a la propiedad con ID " + propiedadId);
        }

        // 3. Eliminar del Storage (Supabase o el sistema que uses)
        eliminarDeStorageSupabase(imagen.getImageUrl()); // método auxiliar que tenés que implementar

        // 4. Eliminar de la base de datos
        imageUrlsRepository.delete(imagen);
    }

    @Transactional
    public void eliminarImagenDeNota(Long notaId, Long idImagen) throws ResourceNotFoundException, IOException {
        // 1. Buscar la imagen
        ImageUrls imagen = imageUrlsRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la imagen con ID " + idImagen));

        // 2. Verificar si la imagen pertenece a la propiedad indicada
        if (imagen.getNota() == null || !imagen.getNota().getId().equals(notaId)) {
            throw new ResourceNotFoundException("La imagen no pertenece a la nota con ID " + notaId);
        }

        // 3. Eliminar del Storage (Supabase o el sistema que uses)
        eliminarDeStorageSupabase(imagen.getImageUrl()); // método auxiliar que tenés que implementar

        // 4. Eliminar de la base de datos
        imageUrlsRepository.delete(imagen);
    }


}
