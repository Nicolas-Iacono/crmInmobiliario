package com.backend.crmInmobiliario.mapper;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContratoMapper {

    // =========================
    // Contrato -> ContratoSalidaDto
    // =========================
    public ContratoSalidaDto toContratoSalidaDto(Contrato c) {
        if (c == null) return null;

        ContratoSalidaDto dto = new ContratoSalidaDto();

        dto.setId(c.getId());
        dto.setNombreContrato(c.getNombreContrato());
        dto.setFecha_inicio(c.getFecha_inicio());
        dto.setFecha_fin(c.getFecha_fin());

        dto.setActivo(c.isActivo());
        dto.setActualizacion(c.getActualizacion());
        dto.setMontoAlquiler(c.getMontoAlquiler());
        dto.setDuracion(c.getDuracion());

        dto.setAguaEmpresa(c.getAguaEmpresa());
        dto.setAguaPorcentaje(c.getAguaPorcentaje());
        dto.setLuzEmpresa(c.getLuzEmpresa());
        dto.setLuzPorcentaje(c.getLuzPorcentaje());
        dto.setGasEmpresa(c.getGasEmpresa());
        dto.setGasPorcentaje(c.getGasPorcentaje());
        dto.setMunicipalEmpresa(c.getMunicipalEmpresa());
        dto.setMunicipalPorcentaje(c.getMunicipalPorcentaje());

        dto.setIndiceAjuste(c.getIndiceAjuste());
        dto.setMontoAlquilerLetras(c.getMontoAlquilerLetras());
        dto.setMultaXDia(c.getMultaXDia());
        dto.setTiempoRestante(c.getTiempoRestante());
        dto.setDestino(c.getDestino());
        dto.setTipoGarantia(c.getTipoGarantia());

        dto.setContratoPdf(c.getPdfContratoTexto());

        dto.setComisionContratoPorc(c.getComisionContratoPorc());
        dto.setComisionMensualPorc(c.getComisionMensualPorc());

        dto.setComisionContratoMonto(c.getComisionContratoMonto());
        dto.setComisionMensualMonto(c.getComisionMensualMonto());
        dto.setMontoMensualPropietario(c.getLiquidacionPropietarioMensual());

        dto.setEstados(c.getEstados());

        // 🔹 Relaciones (mapeadas a DTOs)
        dto.setPropietario(toPropietarioContratoDto(c.getPropietario()));
        dto.setInquilino(toInquilinoContratoDto(c.getInquilino())); // ajustá si tu clase tiene otros campos
        dto.setPropiedad(toPropiedadContratoSalidaDto(c.getPropiedad()));
        dto.setGarantes(toGaranteSalidaDtoList(c.getGarantes()));

        // ✅ flag vencido (si lo querés en el DTO, agregalo al ContratoSalidaDto)
        // dto.setVencido(isVencido(c, LocalDate.now()));

        return dto;
    }

    // =========================
    // Propietario -> PropietarioContratoDtoSalida
    // =========================
    public PropietarioContratoDtoSalida toPropietarioContratoDto(Propietario p) {
        if (p == null) return null;

        PropietarioContratoDtoSalida dto = new PropietarioContratoDtoSalida();
        dto.setId(p.getId());
        dto.setPronombre(p.getPronombre());
        dto.setNombre(p.getNombre());
        dto.setApellido(p.getApellido());
        dto.setTelefono(p.getTelefono());
        dto.setEmail(p.getEmail());
        dto.setDni(p.getDni());
        dto.setDireccionResidencial(p.getDireccionResidencial());
        dto.setCuit(p.getCuit());
        dto.setNacionalidad(p.getNacionalidad());
        dto.setEstadoCivil(p.getEstadoCivil());
        return dto;
    }

    // =========================
    // Inquilino -> InquilinoContratoDtoSalida
    // =========================
    // ⚠️ No me pegaste InquilinoContratoDtoSalida, así que adapto al patrón:
    // cambiá el nombre del DTO y setters si no coinciden.
    public InquilinoContratoDtoSalida toInquilinoContratoDto(Inquilino i) {
        if (i == null) return null;

        var dto = new InquilinoContratoDtoSalida();
        dto.setId(i.getId());
        dto.setPronombre(i.getPronombre());
        dto.setNombre(i.getNombre());
        dto.setApellido(i.getApellido());
        dto.setTelefono(i.getTelefono());
        dto.setEmail(i.getEmail());
        dto.setDni(i.getDni());
        dto.setDireccionResidencial(i.getDireccionResidencial());
        dto.setCuit(i.getCuit());
        dto.setNacionalidad(i.getNacionalidad());
        dto.setEstadoCivil(i.getEstadoCivil());
        // si tu DTO tiene "activo":
        // dto.setActivo(i.isActivo());
        return dto;
    }

    // =========================
    // Propiedad -> PropiedadContratoSalidaDto
    // =========================
    public PropiedadContratoSalidaDto toPropiedadContratoSalidaDto(Propiedad p) {
        if (p == null) return null;

        PropiedadContratoSalidaDto dto = new PropiedadContratoSalidaDto();
        dto.setId(p.getId_propiedad());
        dto.setDireccion(p.getDireccion());
        dto.setLocalidad(p.getLocalidad());
        dto.setPartido(p.getPartido());
        dto.setProvincia(p.getProvincia());
        dto.setInventario(p.getInventario());
        dto.setTipo(p.getTipo());

        dto.setImagenes(toImgUrlSalidaDtoList(p.getImagenes()));
        return dto;
    }

    // =========================
    // ImageUrls -> ImgUrlSalidaDto
    // =========================
    public ImgUrlSalidaDto toImgUrlSalidaDto(ImageUrls img) {
        if (img == null) return null;

        ImgUrlSalidaDto dto = new ImgUrlSalidaDto();
        dto.setIdImage(img.getIdImage());
        dto.setImageUrl(img.getImageUrl());
        dto.setNombreOriginal(img.getNombreOriginal());
        dto.setTipoImagen(img.getTipoImagen());
        dto.setFechaSubida(img.getFechaSubida());
        return dto;
    }

    public List<ImgUrlSalidaDto> toImgUrlSalidaDtoList(List<ImageUrls> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) return List.of();
        return imagenes.stream().map(this::toImgUrlSalidaDto).toList();
    }

    // =========================
    // Garante -> GaranteSalidaDto
    // =========================
    public GaranteSalidaDto toGaranteSalidaDto(Garante g) {
        if (g == null) return null;

        GaranteSalidaDto dto = new GaranteSalidaDto();
        dto.setId(g.getId());
        dto.setPronombre(g.getPronombre());
        dto.setNombre(g.getNombre());
        dto.setApellido(g.getApellido());
        dto.setTelefono(g.getTelefono());
        dto.setEmail(g.getEmail());
        dto.setDni(g.getDni());
        dto.setCuit(g.getCuit());
        dto.setDireccionResidencial(g.getDireccionResidencial());
        dto.setNacionalidad(g.getNacionalidad());
        dto.setEstadoCivil(g.getEstadoCivil());

        dto.setNombreEmpresa(g.getNombreEmpresa());
        dto.setSectorActual(g.getSectorActual());
        dto.setCargoActual(g.getCargoActual());
        dto.setLegajo(g.getLegajo());
        dto.setCuitEmpresa(g.getCuitEmpresa());

        dto.setTipoGarantia(g.getTipoGarantia());
        dto.setPartidaInmobiliaria(g.getPartidaInmobiliaria());
        dto.setDireccion(g.getDireccion());
        dto.setInfoCatastral(g.getInfoCatastral());
        dto.setEstadoOcupacion(g.getEstadoOcupacion());
        dto.setTipoPropiedad(g.getTipoPropiedad());
        dto.setInformeDominio(g.getInformeDominio());
        dto.setInformeInhibicion(g.getInformeInhibicion());

        return dto;
    }

    public List<GaranteSalidaDto> toGaranteSalidaDtoList(List<Garante> garantes) {
        if (garantes == null || garantes.isEmpty()) return List.of();

        return garantes.stream()
                .map(this::toGaranteSalidaDto)
                .toList();
    }

    // =========================
    // Regla vencido (si querés)
    // =========================
    public boolean isVencido(Contrato c, LocalDate fechaActual) {
        return fechaActual.isAfter(c.getFecha_fin())
                && c.getEstado() == EstadoContrato.ACTIVO;
    }
}