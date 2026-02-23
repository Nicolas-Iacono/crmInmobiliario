package com.backend.crmInmobiliario.service.impl.aliados;

import com.backend.crmInmobiliario.DTO.salida.aliados.OficioImagenSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.entity.OficioServicio;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ImageUrlsRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioServicioRepository;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OficioImagenService {

    private final OficioServicioRepository servicioRepository;
    private final ImageUrlsRepository imageUrlsRepository;
    private final ImagenService imagenService;

    public OficioImagenService(OficioServicioRepository servicioRepository,
                               ImageUrlsRepository imageUrlsRepository,
                               ImagenService imagenService) {
        this.servicioRepository = servicioRepository;
        this.imageUrlsRepository = imageUrlsRepository;
        this.imagenService = imagenService;
    }

    @Transactional
    public OficioServicioSalidaDto agregarNuevasPorUsuario(
            Long userId,
            MultipartFile[] imagenes
    ) {
        OficioServicio servicio = servicioRepository
                .findServicioActivoByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("El proveedor no tiene un servicio activo"));

        return agregarNuevas(userId, servicio.getId(), imagenes);
    }



    public void subirImagenesServicio(Long servicioId, MultipartFile[] imagenes) throws IOException {
        if (imagenes == null || imagenes.length == 0) return;

        OficioServicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        for (MultipartFile archivo : imagenes) {
            if (archivo == null || archivo.isEmpty()) continue;

            byte[] webp = imagenService.convertirImagenExternamente(archivo);

            String nombreArchivo = "oficios/servicios/" + servicioId + "/" + UUID.randomUUID() + ".webp";
            String url = imagenService.subirAStorageSupabase(webp, nombreArchivo);

            ImageUrls img = new ImageUrls();
            img.setImageUrl(url);
            img.setNombreOriginal(archivo.getOriginalFilename());
            img.setTipoImagen("SERVICIO_OFICIO");
            img.setFechaSubida(LocalDateTime.now());
            img.setServicio(servicio);

            ImageUrls guardada = imageUrlsRepository.save(img);
            servicio.getImagenes().add(guardada);
        }

        servicioRepository.save(servicio);
    }

    @Transactional
    public void eliminarImagenesDeServicio(Long servicioId) {

        OficioServicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (servicio.getImagenes() == null || servicio.getImagenes().isEmpty()) {
            return;
        }

        // Copia defensiva (evita ConcurrentModificationException)
        List<ImageUrls> imagenes = new ArrayList<>(servicio.getImagenes());

        for (ImageUrls img : imagenes) {
            try {
                // 1️⃣ borrar del storage
                if (img.getImageUrl() != null) {
                    imagenService.eliminarDeStorageSupabase(img.getImageUrl());
                }
            } catch (Exception e) {
                // ⚠️ decisión de negocio: NO romper borrado completo
                // logger.warn("No se pudo borrar imagen del storage: {}", img.getImageUrl(), e);
            }

            // 2️⃣ borrar de la BD
            imageUrlsRepository.delete(img);
        }

        // 3️⃣ limpiar relación en memoria
        servicio.getImagenes().clear();

        // 4️⃣ persistir estado limpio
        servicioRepository.save(servicio);
    }

    // ============ DELETE /servicios/{id}/imagenes/{imageId} ============
    @Transactional
    public OficioServicioSalidaDto eliminarUna(Long userId, Long servicioId, Long imageId) {

        OficioServicio servicio = validarOwner(userId, servicioId);

        ImageUrls img = imageUrlsRepository.findByIdImageAndServicioId(imageId, servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada en este servicio"));

        // 1) Storage
        try {
            if (img.getImageUrl() != null) {
                imagenService.eliminarDeStorageSupabase(img.getImageUrl());
            }
        } catch (Exception e) {
            // decisión: si storage falla, igual borramos BD para que el usuario no quede trabado
            // logger.warn(...)
        }

        // 2) BD + relación
        servicio.getImagenes().removeIf(x -> x.getIdImage().equals(img.getIdImage()));
        imageUrlsRepository.delete(img);

        // devolver servicio actualizado
        OficioServicio recargado = servicioRepository.findByIdWithImagenes(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return toServicioDto(recargado);
    }

    // ============ POST /servicios/{id}/imagenes (agregar nuevas) ============
    @Transactional
    public OficioServicioSalidaDto agregarNuevas(Long userId, Long servicioId, MultipartFile[] imagenes) {

        OficioServicio servicio = validarOwner(userId, servicioId);

        if (imagenes == null || imagenes.length == 0) {
            throw new IllegalArgumentException("No se recibieron imágenes");
        }

        for (MultipartFile archivo : imagenes) {
            if (archivo == null || archivo.isEmpty()) continue;

            // convert + upload
            byte[] webp = convertirSeguro(archivo);
            String nombreArchivo = "oficios/servicios/" + servicioId + "/" + UUID.randomUUID() + ".webp";
            String url = subirSeguro(webp, nombreArchivo);

            ImageUrls img = new ImageUrls();
            img.setImageUrl(url);
            img.setNombreOriginal(archivo.getOriginalFilename());
            img.setTipoImagen("SERVICIO_OFICIO");
            img.setFechaSubida(LocalDateTime.now());
            img.setServicio(servicio);

            ImageUrls guardada = imageUrlsRepository.save(img);
            servicio.getImagenes().add(guardada);
        }

        // devolver servicio actualizado
        OficioServicio recargado = servicioRepository.findByIdWithImagenes(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return toServicioDto(recargado);
    }

    // ============ PUT /servicios/{id}/imagenes (replace total) ============
    @Transactional
    public OficioServicioSalidaDto reemplazarTodas(Long userId, Long servicioId, MultipartFile[] nuevas) {

        OficioServicio servicio = validarOwner(userId, servicioId);

        // 1) borrar existentes (storage + BD)
        List<ImageUrls> actuales = new ArrayList<>(servicio.getImagenes());

        for (ImageUrls img : actuales) {
            try {
                if (img.getImageUrl() != null) {
                    imagenService.eliminarDeStorageSupabase(img.getImageUrl());
                }
            } catch (Exception e) {
                // logger.warn(...)
            }
            imageUrlsRepository.delete(img);
        }
        servicio.getImagenes().clear();

        // 2) subir nuevas (si llegaron)
        if (nuevas != null && nuevas.length > 0) {
            for (MultipartFile archivo : nuevas) {
                if (archivo == null || archivo.isEmpty()) continue;

                byte[] webp = convertirSeguro(archivo);
                String nombreArchivo = "oficios/servicios/" + servicioId + "/" + UUID.randomUUID() + ".webp";
                String url = subirSeguro(webp, nombreArchivo);

                ImageUrls img = new ImageUrls();
                img.setImageUrl(url);
                img.setNombreOriginal(archivo.getOriginalFilename());
                img.setTipoImagen("SERVICIO_OFICIO");
                img.setFechaSubida(LocalDateTime.now());
                img.setServicio(servicio);

                ImageUrls guardada = imageUrlsRepository.save(img);
                servicio.getImagenes().add(guardada);
            }
        }

        // devolver servicio actualizado
        OficioServicio recargado = servicioRepository.findByIdWithImagenes(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return toServicioDto(recargado);
    }

    // ---------------- helpers ----------------

    private OficioServicio validarOwner(Long userId, Long servicioId) {
        OficioServicio servicio = servicioRepository.findByIdWithProveedorUsuario(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        Long ownerUserId = servicio.getProveedor().getUsuario().getId();
        if (!ownerUserId.equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("No sos dueño de este servicio");
        }
        return servicio;
    }

    private byte[] convertirSeguro(MultipartFile archivo) {
        try {
            return imagenService.convertirImagenExternamente(archivo);
        } catch (Exception e) {
            // acá podés decidir: o fallás o seguís sin esa imagen
            throw new RuntimeException("Falló la conversión de imagen", e);
        }
    }

    private String subirSeguro(byte[] data, String path) {
        try {
            return imagenService.subirAStorageSupabase(data, path);
        } catch (Exception e) {
            throw new RuntimeException("Falló la subida al storage", e);
        }
    }

    private OficioServicioSalidaDto toServicioDto(OficioServicio s) {
        OficioServicioSalidaDto dto = new OficioServicioSalidaDto();
        dto.setId(s.getId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setPrecio(s.getPrecio());
        dto.setActivo(s.isActivo());

        List<OficioImagenSalidaDto> imgs = (s.getImagenes() == null) ? List.of()
                : s.getImagenes().stream()
                .map(i -> new OficioImagenSalidaDto(
                        i.getIdImage(),
                        i.getImageUrl(),
                        i.getNombreOriginal(),
                        i.getTipoImagen()
                ))
                .toList();

        dto.setImagenes(imgs);
        return dto;
    }
}

