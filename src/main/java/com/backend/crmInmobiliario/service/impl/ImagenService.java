package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImgUrlEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.ImageUrlsRepository;
import com.backend.crmInmobiliario.service.IImageUrlsService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ImagenService implements IImageUrlsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(IImageUrlsService.class);
    private final ImageUrlsRepository imageUrlsRepository;
    private final ModelMapper mapper;
    private final GaranteRepository garanteRepository;

    public ImagenService(ImageUrlsRepository imageUrlsRepository, ModelMapper mapper, GaranteRepository garanteRepository) {
        this.imageUrlsRepository = imageUrlsRepository;
        this.mapper = mapper;
        this.garanteRepository = garanteRepository;
        configureMapping();
    }

    private void configureMapping() {
        mapper.typeMap(ImgUrlEntradaDto.class, ImageUrls.class)
                .addMappings(mapper -> mapper.skip(ImageUrls::setGarante));

        mapper.typeMap(ImageUrls.class, ImgUrlSalidaDto.class)
                .addMapping(src -> src.getGarante().getId(), ImgUrlSalidaDto::setGarante_id);

    }

    @Override
    public ImgUrlSalidaDto agregarImagen(ImgUrlEntradaDto imgUrlEntradaDto) throws ResourceNotFoundException {

        Long garanteId = imgUrlEntradaDto.getGarante_id();
        if(garanteId == null) {
            throw new IllegalArgumentException("El id del garante no puede ser nulo");
        }
        Garante garante = garanteRepository.findById(garanteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el garante con el ID proporcionado"));
        ImageUrls imageUrls = new ImageUrls();
        imageUrls.setImageUrl(imgUrlEntradaDto.getImageUrl());
        imageUrls.setGarante(garante);
        ImageUrls imageToSave = imageUrlsRepository.save(imageUrls);
        Long imagenId = imageToSave.getIdImage();
        ImgUrlSalidaDto imgUrlSalidaDto = new ImgUrlSalidaDto();
        imgUrlSalidaDto.setIdImage(imagenId);
        imgUrlSalidaDto.setImageUrl(imageToSave.getImageUrl());
        imgUrlSalidaDto.setGarante_id(garanteId);
        return imgUrlSalidaDto;
    }

    @Override
    public List<ImgUrlSalidaDto> listarTodasLasImagens() {
        List<ImageUrls> imagenes = imageUrlsRepository.findAll();
                return imagenes.stream()
                        .map(imagen -> mapper.map(imagen,ImgUrlSalidaDto.class))
                        .toList();
    }

    @Override
    public ImgUrlSalidaDto obtenerImagenPorId(Long idImage) throws ResourceNotFoundException {
        ImageUrls imageUrls = imageUrlsRepository.findById(idImage).orElse(null);
        ImgUrlSalidaDto imgUrlSalidaDto = null;
        if (imageUrls != null) {
            imgUrlSalidaDto = mapper.map(imageUrls, ImgUrlSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("Imagen no encontrada con ID " + idImage);
        }
        return imgUrlSalidaDto;
    }

    @Override
    public void eliminarImagen(Long idImage, Long idGarante) throws ResourceNotFoundException {

    }
}
