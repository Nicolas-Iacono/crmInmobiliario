package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImgUrlEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IImageUrlsService {

//    List<String> subirImagenesYAsociarAGarante(Long garanteId, MultipartFile[] archivos) throws IOException, ResourceNotFoundException;

   List<ImgUrlSalidaDto> subirImagenesYAsociarAPropiedad(Long propiedadId, MultipartFile[] archivos) throws IOException, ResourceNotFoundException;

   List<ImgUrlSalidaDto> subirImagenesYAsociarANota(Long notaId, MultipartFile[] archivos) throws IOException, ResourceNotFoundException;

   ImgUrlSalidaDto subirLogo (Long usuarioId, MultipartFile archivo )throws IOException, ResourceNotFoundException;
   void eliminarLogo(Long usuarioId) throws ResourceNotFoundException, IOException;

}
