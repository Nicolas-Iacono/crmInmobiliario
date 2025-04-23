package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.repository.*;
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
    public ImagenService(ModelMapper mapper, GaranteRepository garanteRepository, InquilinoRepository inquilinoRepository, PropietarioRepository propietarioRepository, PropiedadRepository propiedadRepository, ImageUrlsRepository imageUrlsRepository) {
        this.mapper = mapper;
        this.garanteRepository = garanteRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.propietarioRepository = propietarioRepository;
        this.propiedadRepository = propiedadRepository;
        this.imageUrlsRepository = imageUrlsRepository;
        configureMapping();
    }

    private void configureMapping() {


    }

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

}
