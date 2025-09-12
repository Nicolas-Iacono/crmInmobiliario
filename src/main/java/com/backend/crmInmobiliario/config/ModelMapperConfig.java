package com.backend.crmInmobiliario.config;

import com.backend.crmInmobiliario.DTO.entrada.UserAdminEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestosGeneralSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.utils.GaranteConverter;
import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper(GaranteConverter garanteConverter) {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true)
                .setPropertyCondition(Conditions.isNotNull());

        // ✅ Converter para Hibernate PersistentBag → List
        if (modelMapper.getTypeMap(PersistentBag.class, List.class) == null) {
            modelMapper.createTypeMap(PersistentBag.class, List.class)
                    .setConverter(context -> new ArrayList<>((PersistentBag) context.getSource()));
        }

        modelMapper.typeMap(UserAdminEntradaDto.class, Usuario.class)
                .addMappings(m -> {
                    m.map(UserAdminEntradaDto::getUsername,     Usuario::setUsername);
                    m.map(UserAdminEntradaDto::getPassword,     Usuario::setPassword); // la encriptás luego
                    m.map(UserAdminEntradaDto::getNombreNegocio,Usuario::setNombreNegocio);
                    m.map(UserAdminEntradaDto::getEmail,        Usuario::setEmail);
                    m.map(UserAdminEntradaDto::getMatricula,    Usuario::setMatricula);
                    m.map(UserAdminEntradaDto::getRazonSocial,  Usuario::setRazonSocial);
                    m.map(UserAdminEntradaDto::getLocalidad,    Usuario::setLocalidad);
                    m.map(UserAdminEntradaDto::getPartido,      Usuario::setPartido);
                    m.map(UserAdminEntradaDto::getProvincia,    Usuario::setProvincia);
                    m.map(UserAdminEntradaDto::getCuit,         Usuario::setCuit);
                    m.map(UserAdminEntradaDto::getTelefono,     Usuario::setTelefono);
                    // No mapeamos nada relacionado con UserDetails (enabled, etc.)
                });

        // ✅ Converter personalizado para Garante
        modelMapper.addConverter(garanteConverter);

        // 🔁 Skip mapeo automático de campos problemáticos en GaranteSalidaDto
        modelMapper.typeMap(Garante.class, GaranteSalidaDto.class)
                .addMappings(mapper -> {
                    mapper.skip(GaranteSalidaDto::setUsuarioDtoSalida); // Omitir si usuario genera ciclos
                });

        // 🔁 Skip campos sensibles en Recibo ➝ ReciboSalidaDto
        modelMapper.typeMap(Recibo.class, ReciboSalidaDto.class)
                .addMappings(mapper -> {
                    mapper.skip(ReciboSalidaDto::setContratoId);
                });

        // 🎯 Mapeo específico de Impuesto ➝ ImpuestosGeneralSalidaDto
        modelMapper.typeMap(Impuesto.class, ImpuestosGeneralSalidaDto.class)
                .addMappings(mapper -> {
                    mapper.map(Impuesto::getTipoImpuesto, ImpuestosGeneralSalidaDto::setTipo);
                });

        // 🧨 Evitar ciclo: Contrato ➝ ContratoSalidaDto (no mapeamos garantes automáticamente)
        modelMapper.typeMap(Contrato.class, ContratoSalidaDto.class)
                .addMappings(mapper -> {
                    mapper.skip(ContratoSalidaDto::setGarantes); // 👈 Esto rompe el ciclo
                    mapper.map(Contrato::getRecibos, ContratoSalidaDto::setRecibos);
                    mapper.map(Contrato::getUsuario, ContratoSalidaDto::setUsuarioDtoSalida);
                });

        return modelMapper;
    }
}
