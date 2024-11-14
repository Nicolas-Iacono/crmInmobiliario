package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.ImgUrlEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/imagen")
public class ImagenController {

    private final ImagenService imagenService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> crearImgUrl(@RequestBody @Valid ImgUrlEntradaDto imgUrlEntradaDto) {
        try{
            ImgUrlSalidaDto imgUrlSalidaDto = imagenService.agregarImagen(imgUrlEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("imagen agregada exitosamente", imgUrlSalidaDto));
        }  catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro el Garante con el ID proporcionado.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Ocurrió un error al procesar la solicitud.", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ImgUrlSalidaDto>>> allImages() {
        List<ImgUrlSalidaDto> imagesUrlsDtoExits = imagenService.listarTodasLasImagens();
        ApiResponse<List<ImgUrlSalidaDto>> response =
                new ApiResponse<>("Lista de Imagenes Urls exitosa.", imagesUrlsDtoExits);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/buscar/{idImage}")
    public ResponseEntity<?> buscarImgPorId(@PathVariable Long idImage) {
        try {
            ImgUrlSalidaDto imagesUrlsDtoExit = imagenService.obtenerImagenPorId(idImage);
            return ResponseEntity.ok(new ApiResponse<>("Imagen Urls encontrada con exito.", imagesUrlsDtoExit));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro la imagen Urls con el ID proporcionado.", null));
        }
    }
}
