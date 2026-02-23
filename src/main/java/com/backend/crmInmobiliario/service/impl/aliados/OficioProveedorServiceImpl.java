package com.backend.crmInmobiliario.service.impl.aliados;

import com.backend.crmInmobiliario.DTO.salida.aliados.OficioImagenSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.entity.OficioProveedor;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ImageUrlsRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioProveedorRepository;
import com.backend.crmInmobiliario.service.IOficioProveedorService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OficioProveedorServiceImpl implements IOficioProveedorService {

    private final OficioProveedorRepository proveedorRepository;
    private final ModelMapper modelMapper;
    private final ImagenService imagenService;
    private final ImageUrlsRepository imageUrlsRepository;

    @Override
    @Transactional
    public OficioProveedorSalidaDto obtenerMiPerfil(Long userId) {

        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado para el usuario id: " + userId));

        return modelMapper.map(proveedor, OficioProveedorSalidaDto.class);
    }

    @Transactional
    public OficioProveedorSalidaDto reemplazarFotoPerfil(Long userId, MultipartFile imagen) throws IOException {
        OficioProveedor prov = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        // 1) borrar anterior si existe (storage + BD)
        if (prov.getImagenPerfil() != null) {
            try {
                imagenService.eliminarDeStorageSupabase(prov.getImagenPerfil().getImageUrl());
            } catch (Exception ignored) {}

            imageUrlsRepository.delete(prov.getImagenPerfil());
            prov.setImagenPerfil(null);
        }

        // 2) subir nueva
        byte[] webp = imagenService.convertirImagenExternamente(imagen);
        String path = "oficios/proveedores/" + prov.getId() + "/perfil-" + UUID.randomUUID() + ".webp";
        String url = imagenService.subirAStorageSupabase(webp, path);

        ImageUrls img = new ImageUrls();
        img.setImageUrl(url);
        img.setNombreOriginal(imagen.getOriginalFilename());
        img.setTipoImagen("PERFIL_OFICIO");
        img.setFechaSubida(LocalDateTime.now());

        ImageUrls guardada = imageUrlsRepository.save(img);

        // 3) linkear
        prov.setImagenPerfil(guardada);
        proveedorRepository.save(prov);

        return toProveedorDto(prov);
    }

    private OficioProveedorSalidaDto toProveedorDto(OficioProveedor prov) {

        OficioProveedorSalidaDto dto = new OficioProveedorSalidaDto();

        dto.setId(prov.getId());
        dto.setNombreCompleto(prov.getNombreCompleto());
        dto.setEmpresa(prov.getEmpresa());
        dto.setDescripcion(prov.getDescripcion());
        dto.setEmailContacto(prov.getEmailContacto());
        dto.setTelefonoContacto(prov.getTelefonoContacto());
        dto.setProvincia(prov.getProvincia());
        dto.setLocalidad(prov.getLocalidad());

        dto.setPromedioCalificacion(prov.getPromedioCalificacion());
        dto.setTotalCalificaciones(prov.getTotalCalificaciones());

        // ================== FOTO DE PERFIL ==================
        if (prov.getImagenPerfil() != null) {
            dto.setImagenPerfilUrl(String.valueOf(new OficioImagenSalidaDto(
                    prov.getImagenPerfil().getIdImage(),
                    prov.getImagenPerfil().getImageUrl(),
                    prov.getImagenPerfil().getNombreOriginal(),
                    prov.getImagenPerfil().getTipoImagen()
            )));
        } else {
            dto.setImagenPerfilUrl(null);
        }

        return dto;
    }


    @Transactional
    public void eliminarFotoPerfil(Long userId) {
        OficioProveedor prov = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (prov.getImagenPerfil() == null) return;

        try {
            imagenService.eliminarDeStorageSupabase(prov.getImagenPerfil().getImageUrl());
        } catch (Exception ignored) {}

        imageUrlsRepository.delete(prov.getImagenPerfil());
        prov.setImagenPerfil(null);
        proveedorRepository.save(prov);
    }
}