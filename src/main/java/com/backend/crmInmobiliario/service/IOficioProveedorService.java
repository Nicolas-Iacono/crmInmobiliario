package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;

public interface IOficioProveedorService {
    OficioProveedorSalidaDto obtenerMiPerfil(Long userId);
}
