package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoImpuestoTemplateDto;
import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoModoRecibosDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.ModoRecibos;
import com.backend.crmInmobiliario.entity.impuestos.ContratoImpuestoTemplate;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoImpuestoTemplateRepository;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReciboAutomaticoService {

    private final ContratoRepository contratoRepository;
    private final ContratoImpuestoTemplateRepository contratoImpuestoTemplateRepository;
    private final AuthUtil authUtil;

    @Transactional
    public Contrato configurarModoRecibos(Long contratoId, ContratoModoRecibosDto dto) {
        Contrato contrato = getContratoPropio(contratoId);

        if (dto.getModoRecibos() != null) {
            contrato.setModoRecibos(dto.getModoRecibos());
        }
        if (dto.getAutoRecibosActivo() != null) {
            contrato.setAutoRecibosActivo(dto.getAutoRecibosActivo());
        }
        if (dto.getDiaGeneracion() != null) {
            contrato.setDiaGeneracion(normalizarDia(dto.getDiaGeneracion()));
        }
        if (dto.getDiaVencimiento() != null) {
            contrato.setDiaVencimiento(normalizarDia(dto.getDiaVencimiento()));
        }

        if (contrato.getModoRecibos() == ModoRecibos.AUTOMATICO && !contrato.isAutoRecibosActivo()) {
            contrato.setAutoRecibosActivo(true);
        }

        return contratoRepository.save(contrato);
    }

    @Transactional
    public List<ContratoImpuestoTemplateDto> reemplazarTemplates(Long contratoId, List<ContratoImpuestoTemplateDto> dtos) {
        Contrato contrato = getContratoPropio(contratoId);

        contratoImpuestoTemplateRepository.deleteByContratoId(contratoId);

        List<ContratoImpuestoTemplate> nuevos = dtos.stream().map(dto -> {
            ContratoImpuestoTemplate t = new ContratoImpuestoTemplate();
            t.setContrato(contrato);
            t.setTipoImpuesto(normalizarTipo(dto.getTipoImpuesto()));
            t.setDescripcion(dto.getDescripcion());
            t.setEmpresa(dto.getEmpresa());
            t.setNumeroCliente(dto.getNumeroCliente());
            t.setNumeroMedidor(dto.getNumeroMedidor());
            t.setMontoBase(dto.getMontoBase() == null ? BigDecimal.ZERO : dto.getMontoBase());
            t.setPorcentaje(dto.getPorcentaje());
            t.setActivo(dto.getActivo() == null || dto.getActivo());
            return t;
        }).toList();

        return contratoImpuestoTemplateRepository.saveAll(nuevos).stream().map(this::toDto).toList();
    }

    @Transactional
    public List<ContratoImpuestoTemplateDto> listarTemplates(Long contratoId) {
        getContratoPropio(contratoId);
        return contratoImpuestoTemplateRepository.findByContratoId(contratoId).stream().map(this::toDto).toList();
    }

    private Contrato getContratoPropio(Long contratoId) {
        Long userId = authUtil.extractUserId();
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (contrato.getUsuario() == null || !contrato.getUsuario().getId().equals(userId)) {
            throw new AccessDeniedException("No tenés permisos sobre este contrato");
        }
        return contrato;
    }

    private int normalizarDia(int dia) {
        return Math.max(1, Math.min(28, dia));
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("tipoImpuesto obligatorio");
        return tipo.trim().toUpperCase(Locale.ROOT);
    }

    private ContratoImpuestoTemplateDto toDto(ContratoImpuestoTemplate t) {
        ContratoImpuestoTemplateDto dto = new ContratoImpuestoTemplateDto();
        dto.setId(t.getId());
        dto.setTipoImpuesto(t.getTipoImpuesto());
        dto.setDescripcion(t.getDescripcion());
        dto.setEmpresa(t.getEmpresa());
        dto.setNumeroCliente(t.getNumeroCliente());
        dto.setNumeroMedidor(t.getNumeroMedidor());
        dto.setMontoBase(t.getMontoBase());
        dto.setPorcentaje(t.getPorcentaje());
        dto.setActivo(t.isActivo());
        return dto;
    }
}
