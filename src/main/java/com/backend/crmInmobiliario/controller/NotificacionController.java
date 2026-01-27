package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.entity.EstadoNotificacion;
import com.backend.crmInmobiliario.entity.Notificacion;
import com.backend.crmInmobiliario.repository.notificacionesPush.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionRepository repo;

    @GetMapping("/pendientes/{usuarioId}")
    public List<Notificacion> pendientes(@PathVariable Long usuarioId) {
        return repo.findByUsuarioIdAndEstadoOrderByIdDesc(usuarioId, EstadoNotificacion.PENDIENTE);
    }
}
