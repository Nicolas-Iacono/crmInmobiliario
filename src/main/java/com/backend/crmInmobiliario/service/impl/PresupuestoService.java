package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.PresupuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PresupuestoSalidaDto;
import com.backend.crmInmobiliario.entity.Presupuesto;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.PresupuestoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IPresupuestoService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresupuestoService implements IPresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    @PostConstruct
    void configureMapping() {
        // configuración básica (no intentes mapear usuarioId -> Usuario aquí)
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);

        // Si querés, solo este typeMap para salida (usuarioId desde entidad)
        modelMapper.typeMap(Presupuesto.class, PresupuestoSalidaDto.class)
                .addMappings(m -> m.map(src -> src.getUsuario() != null ? src.getUsuario().getId() : null,
                        PresupuestoSalidaDto::setUsuarioId));
        // No definas: PresupuestoEntradaDto.usuarioId -> Presupuesto.setUsuario (tipos incompatibles)
    }

    @Override
    public PresupuestoSalidaDto crear(PresupuestoEntradaDto dto) throws ResourceNotFoundException {
        Presupuesto p = mapToEntity(dto, null);
        Presupuesto guardado = presupuestoRepository.save(p);
        return mapToSalida(guardado);
    }

    @Override
    public PresupuestoSalidaDto actualizar(Long id, PresupuestoEntradaDto dto) throws ResourceNotFoundException {
        Presupuesto existente = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));
        Presupuesto actualizado = mapToEntity(dto, existente);
        Presupuesto guardado = presupuestoRepository.save(actualizado);
        return mapToSalida(guardado);
    }

    @Override
    public PresupuestoSalidaDto buscarPorId(Long id) throws ResourceNotFoundException {
        Presupuesto p = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));
        return mapToSalida(p);
    }

    @Override
    public List<PresupuestoSalidaDto> listar() {
        return presupuestoRepository.findAll().stream()
                .map(this::mapToSalida) // ✅ evitamos NPE del mapper
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PresupuestoSalidaDto> listarPorUsuarioId(Long id) {
        return presupuestoRepository.findByUsuarioId(id)
                .stream()
                .map(this::mapToSalida) // ✅ evitamos NPE del mapper
                .toList();
    }

    @Override
    public void eliminar(Long id) throws ResourceNotFoundException {
        Presupuesto p = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));
        presupuestoRepository.delete(p);
    }

    // ===== Helpers =====

    private Presupuesto mapToEntity(PresupuestoEntradaDto dto, Presupuesto target) throws ResourceNotFoundException {
        Presupuesto p = (target != null) ? target : new Presupuesto();

        if (dto.getUsuarioId() != null) {
            Usuario u = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            p.setUsuario(u);
        } else {
            p.setUsuario(null);
        }

        p.setTitulo(dto.getTitulo());
        p.setMonto(dto.getMonto());
        p.setPorcentajeContrato(dto.getPorcentajeContrato());
        p.setPorcentajeSello(dto.getPorcentajeSello());
        p.setDuracion(dto.getDuracion());
        p.setGastosExtras(dto.getGastosExtras());

        return p;
    }

    private PresupuestoSalidaDto mapToSalida(Presupuesto p) {
        PresupuestoSalidaDto out = new PresupuestoSalidaDto();
        out.setId(p.getId());
        out.setUsuarioId(p.getUsuario() != null ? p.getUsuario().getId() : null);
        out.setTitulo(p.getTitulo());
        out.setMonto(p.getMonto());
        out.setPorcentajeContrato(p.getPorcentajeContrato());
        out.setPorcentajeSello(p.getPorcentajeSello());
        out.setDuracion(p.getDuracion());
        out.setGastosExtras(p.getGastosExtras());

        BigDecimal monto = bd(p.getMonto());
        BigDecimal meses = BigDecimal.valueOf(p.getDuracion());
        BigDecimal pctContrato = pct(p.getPorcentajeContrato());
        BigDecimal pctSello = pct(p.getPorcentajeSello());
        BigDecimal extras = bd(p.getGastosExtras());

        BigDecimal deposito  = monto.setScale(SCALE, RM);
        BigDecimal primerMes = monto.setScale(SCALE, RM);
        BigDecimal sellado   = monto.multiply(meses).multiply(pctSello).setScale(SCALE, RM);
        BigDecimal honorarios= monto.multiply(meses).multiply(pctContrato).setScale(SCALE, RM);
        BigDecimal total     = primerMes.add(sellado).add(honorarios).add(extras).add(deposito).setScale(SCALE, RM);

        out.setPrimerMes(primerMes);
        out.setSellado(sellado);
        out.setHonorarios(honorarios);
        out.setDeposito(deposito);
        out.setTotal(total);

        return out;
    }

    private BigDecimal bd(Double v) {
        return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v);
    }

    private BigDecimal pct(String s) {
        if (s == null) return BigDecimal.ZERO;
        String norm = s.trim().replace("%", "").replace(",", ".");
        if (norm.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(norm).movePointLeft(2); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }
}
