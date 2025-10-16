package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.salida.IngresoMensualResumenDto;
import com.backend.crmInmobiliario.DTO.salida.IngresoMensualSalidaDto;
import com.backend.crmInmobiliario.controller.ContratoController;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.IngresoMensual;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.IngresoMensualRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngresoMensualService {

    private final ContratoRepository contratoRepository;
    private final IngresoMensualRepository ingresoMensualRepository;
    private final ModelMapper mapper;
    private final UsuarioRepository usuarioRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);

    public IngresoMensualService(ContratoRepository contratoRepository,
                                 IngresoMensualRepository ingresoMensualRepository,
                                 ModelMapper mapper, UsuarioRepository usuarioRepository) {
        this.contratoRepository = contratoRepository;
        this.ingresoMensualRepository = ingresoMensualRepository;
        this.mapper = mapper;
        this.usuarioRepository = usuarioRepository;
    }


    @Transactional
    public void generarIngresosDelMesActual(String username) {
        Usuario usuario = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        List<Contrato> contratosUsuario = contratoRepository.findByUsuario(usuario);

        for (Contrato contrato : contratosUsuario) {
            LocalDate fechaInicio = contrato.getFecha_inicio();
            if (fechaInicio == null) continue;

            int anioInicio = fechaInicio.getYear();
            int mesInicio = fechaInicio.getMonthValue();

            // Si el contrato empieza en el futuro, lo saltamos
            if (fechaInicio.isAfter(hoy)) continue;

            BigDecimal montoAlquiler = BigDecimal.valueOf(
                    contrato.getMontoAlquiler() != null ? contrato.getMontoAlquiler() : 0.0
            );

            BigDecimal comisionContrato = contrato.getComisionContratoPorc() != null
                    ? contrato.getComisionContratoPorc() : BigDecimal.ZERO;
            BigDecimal comisionMensual = contrato.getComisionMensualPorc() != null
                    ? contrato.getComisionMensualPorc() : BigDecimal.ZERO;

            // Normalización si vino en formato 500 → 5%
            if (comisionContrato.compareTo(BigDecimal.valueOf(100)) > 0)
                comisionContrato = comisionContrato.divide(BigDecimal.valueOf(100));
            if (comisionMensual.compareTo(BigDecimal.valueOf(100)) > 0)
                comisionMensual = comisionMensual.divide(BigDecimal.valueOf(100));

            int duracion = contrato.getDuracion() > 0 ? contrato.getDuracion() : 1;

            // Generar ingresos desde el mes de inicio hasta el actual
            LocalDate fechaIter = fechaInicio;
            while (!fechaIter.isAfter(hoy)) {

                int mes = fechaIter.getMonthValue();
                int anio = fechaIter.getYear();

                // Evitar duplicados
                boolean yaExiste = ingresoMensualRepository.existsByContratoAndAnioAndMes(contrato, anio, mes);
                if (yaExiste) {
                    fechaIter = fechaIter.plusMonths(1);
                    continue;
                }

                // Cálculo mensual
                BigDecimal ingresoPorMes = montoAlquiler
                        .multiply(comisionMensual)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // Cálculo del ingreso por contrato (solo el primer mes)
                BigDecimal ingresoPorContrato = BigDecimal.ZERO;
                if (anio == anioInicio && mes == mesInicio) {
                    ingresoPorContrato = montoAlquiler
                            .multiply(BigDecimal.valueOf(duracion))
                            .multiply(comisionContrato)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                IngresoMensual ingreso = IngresoMensual.builder()
                        .contrato(contrato)
                        .usuario(usuario)
                        .anio(anio)
                        .mes(mes)
                        .montoAlquiler(montoAlquiler)
                        .porcentajeComisionContrato(comisionContrato)
                        .porcentajeComisionMensual(comisionMensual)
                        .ingresoCalculadoPorContrato(ingresoPorContrato)
                        .ingresoCalculadoPorMes(ingresoPorMes)
                        .fechaRegistro(LocalDateTime.now())
                        .build();

                ingresoMensualRepository.save(ingreso);

                LOGGER.info("✅ Ingreso generado para contrato={} mes={}/{} usuario={}",
                        contrato.getId_contrato(), mes, anio, usuario.getUsername());

                // Avanzamos al siguiente mes
                fechaIter = fechaIter.plusMonths(1);
            }
        }

    }



    @Transactional(readOnly = true)
    public List<IngresoMensualSalidaDto> obtenerPorMesYAnio(String username, int mes, int anio) {
        Usuario usuario = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<IngresoMensual> ingresos = ingresoMensualRepository.findByUsuarioAndMesAndAnio(usuario, mes, anio);

        return ingresos.stream()
                .map(ing -> IngresoMensualSalidaDto.builder()
                        .id(ing.getId())
                        .mes(ing.getMes())
                        .anio(ing.getAnio())
                        .montoAlquiler(ing.getMontoAlquiler())
                        .porcentajeComisionContrato(ing.getPorcentajeComisionContrato())
                        .porcentajeComisionMensual(ing.getPorcentajeComisionMensual())
                        .ingresoCalculadoPorContrato(ing.getIngresoCalculadoPorContrato())
                        .ingresoCalculadoPorMes(ing.getIngresoCalculadoPorMes())
                        .nombreContrato(ing.getContrato().getNombreContrato())
                        .nombreUsuario(ing.getUsuario().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    public List<IngresoMensualResumenDto> obtenerResumenAnual(String username, int anio) {
        Usuario usuario = usuarioRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Object[]> resultados = ingresoMensualRepository.obtenerTotalesAgrupadosPorMes(usuario, anio);

        return resultados.stream()
                .map(r -> new IngresoMensualResumenDto(
                        (int) r[1],                       // mes
                        (int) r[0],                       // anio (dependiendo del orden en la query)
                        (BigDecimal) r[2],                // total mensual
                        (BigDecimal) r[3]                 // total por contrato
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void generarParaContrato(Contrato contrato) {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        // Evitar duplicados del mismo contrato/mes
        if (ingresoMensualRepository.existsByContratoAndAnioAndMes(contrato, anio, mes)) {
            return;
        }

        // Base de cálculos (todos BigDecimal)
        BigDecimal montoAlquiler = BigDecimal.valueOf(
                contrato.getMontoAlquiler() != null ? contrato.getMontoAlquiler() : 0.0
        );

        // Porcentajes en PORCENTAJE (5.00 => 5.00)
        BigDecimal porcContrato = contrato.getComisionContratoPorc() != null
                ? contrato.getComisionContratoPorc() : BigDecimal.ZERO;

        BigDecimal porcMensual = contrato.getComisionMensualPorc() != null
                ? contrato.getComisionMensualPorc() : BigDecimal.ZERO;

        // Cálculos (ya tenés helpers, uso los tuyos para consistencia)
        BigDecimal ingresoPorMes = contrato.getComisionMensualMonto();     // alquiler * %mensual
        BigDecimal ingresoPorContrato = BigDecimal.ZERO;

        // Honorarios por contrato SOLO si el inicio es en el mes actual y no existe aún
        LocalDate fi = contrato.getFecha_inicio();
        boolean esContratoDelMes = (fi != null && fi.getMonthValue() == mes && fi.getYear() == anio);
        if (esContratoDelMes) {
            ingresoPorContrato = contrato.getComisionContratoMonto();      // (alquiler*duración)*%contrato
        }

        IngresoMensual ingreso = IngresoMensual.builder()
                .contrato(contrato)
                .usuario(contrato.getUsuario())
                .mes(mes)
                .anio(anio)
                .montoAlquiler(montoAlquiler)
                .porcentajeComisionContrato(porcContrato)   // BigDecimal en DB (5.00)
                .porcentajeComisionMensual(porcMensual)     // BigDecimal en DB (5.00)
                .ingresoCalculadoPorContrato(ingresoPorContrato)
                .ingresoCalculadoPorMes(ingresoPorMes)
                .fechaRegistro(java.time.LocalDateTime.now())
                .build();

        ingresoMensualRepository.save(ingreso);
    }
}
