package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImgUrlEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IImageUrlsService {

    ImgUrlSalidaDto agregarImagen(ImgUrlEntradaDto imgUrlEntradaDto) throws ResourceNotFoundException;

    List<ImgUrlSalidaDto> listarTodasLasImagens();

    ImgUrlSalidaDto obtenerImagenPorId(Long idImage) throws ResourceNotFoundException;

//    ImgUrlSalidaDto updateImageUrls(ImageUrlsDtoModify imageUrlsDtoModify) throws ResourceNotFoundException;

    void eliminarImagen(Long idImage, Long idGarante) throws ResourceNotFoundException;
}
