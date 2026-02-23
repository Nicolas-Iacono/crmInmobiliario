package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OficioProveedorDetalleDto {
    private Long id;
    private String nombreCompleto;
    private String empresa;
    private String emailContacto;
    private String telefonoContacto;
    private String descripcion;
    private String localidad;
    private String provincia;
    private List<String> categorias;
    private String imagenPerfilUrl;

    private Double promedioCalificacion;
    private Integer totalCalificaciones;

    private List<ServicioCardDto> servicios = new ArrayList<>();

    // ===== Reseñas =====
    private List<ResenaSalidaDto> resenas = new ArrayList<>();

    @Data
    public static class ServicioCardDto {
        private Long id;
        private String titulo;
        private String descripcion;
        private BigDecimal precio;          // si usás uno solo
        private BigDecimal precioHastaArs;  // si usás rango
        private Boolean activo;

        private List<ImagenServicioDto> imagenes = new ArrayList<>();
    }

    @Data
    public static class ResenaSalidaDto {
        private Long id;
        private Integer calificacion;      // 1..5
        private String comentario;
        private LocalDateTime fechaCreacion;

        // inmobiliaria que opina
        private Long usuarioId;
        private String username;
        private String nombreNegocio;      // opcional si lo tenés en Usuario
        private Long usuarioImagenPerfilId; // opcional
        private String usuarioImagenPerfilUrl; // opcional
    }
    @Data
    public static class ImagenServicioDto {
        private Long id;
        private String url;
        private String nombreOriginal;
        private String tipoImagen;
    }
}
