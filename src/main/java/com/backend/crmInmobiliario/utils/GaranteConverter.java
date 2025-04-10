package com.backend.crmInmobiliario.utils;

import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.Garante;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class GaranteConverter implements Converter<Garante, GaranteSalidaDto> {

    @Override
    public GaranteSalidaDto convert(MappingContext<Garante, GaranteSalidaDto> context) {
        Garante source = context.getSource();
        GaranteSalidaDto dto = new GaranteSalidaDto();

        dto.setId(source.getId());
        dto.setPronombre(source.getPronombre());
        dto.setNombre(source.getNombre());
        dto.setApellido(source.getApellido());
        dto.setTelefono(source.getTelefono());
        dto.setEmail(source.getEmail());
        dto.setDni(source.getDni());
        dto.setCuit(source.getCuit());
        dto.setDireccionResidencial(source.getDireccionResidencial());
        dto.setNacionalidad(source.getNacionalidad());
        dto.setEstadoCivil(source.getEstadoCivil());
        dto.setNombreEmpresa(source.getNombreEmpresa());
        dto.setSectorActual(source.getSectorActual());
        dto.setCargoActual(source.getCargoActual());
        dto.setLegajo(source.getLegajo());
        dto.setCuitEmpresa(source.getCuitEmpresa());
        dto.setTipoGarantia(source.getTipoGarantia());
        dto.setPartidaInmobiliaria(source.getPartidaInmobiliaria());
        dto.setDireccion(source.getDireccion());
        dto.setInfoCatastral(source.getInfoCatastral());
        dto.setEstadoOcupacion(source.getEstadoOcupacion());
        dto.setTipoPropiedad(source.getTipoPropiedad());
        dto.setInformeDominio(source.getInformeDominio());
        dto.setInformeInhibicion(source.getInformeInhibicion());

        // Puedes mapear usuario si lo necesitas, o saltártelo si es complejo
        return dto;
    }
}
