package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoMunicipalEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoMunicipalSalidaDto;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.MunicipalRepository;
import com.backend.crmInmobiliario.service.IMunicipalService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MunicipalService implements IMunicipalService {

    private final Logger LOGGER = LoggerFactory.getLogger(MunicipalService.class);
    private ModelMapper modelMapper;
    private MunicipalRepository municipalRepository;


    public MunicipalService(ModelMapper modelMapper, MunicipalRepository municipalRepository) {
        this.modelMapper = modelMapper;
        this.municipalRepository = municipalRepository;
    }

    @Override
    public List<ImpuestoMunicipalSalidaDto> listarImpuestoMunicipal() {
        List<Municipal> impuestosMunicipal = municipalRepository.findAll();
        return impuestosMunicipal.stream()
                .map(impuestoMunicipal->modelMapper.map(impuestoMunicipal, ImpuestoMunicipalSalidaDto.class))
                .toList();
    }

    @Override
    public ImpuestoMunicipalSalidaDto crearImpuestoMunicipal(ImpuestoMunicipalEntradaDto impuestoMunicipalEntradaDto) throws ResourceNotFoundException {
        Municipal municipalServicio = new Municipal();
        municipalServicio.setEmpresa(impuestoMunicipalEntradaDto.getEmpresa());
        municipalServicio.setPorcentaje(impuestoMunicipalEntradaDto.getPorcentaje());
        municipalServicio.setDescripcion(impuestoMunicipalEntradaDto.getDescripcion());
        municipalServicio.setNumeroMedidor(impuestoMunicipalEntradaDto.getNumeroMedidor());
        municipalServicio.setNumeroCliente(impuestoMunicipalEntradaDto.getNumeroCliente());
        municipalServicio.setMontoAPagar(impuestoMunicipalEntradaDto.getMontoAPagar());
        municipalServicio.setFechaFactura(impuestoMunicipalEntradaDto.getFechaFactura());

        Municipal servicioToSave = municipalRepository.save(municipalServicio);
        ImpuestoMunicipalSalidaDto municipalSalidaDto = modelMapper.map(servicioToSave, ImpuestoMunicipalSalidaDto.class);
        return municipalSalidaDto;
    }

    @Override
    public void eliminarImpuestoMunicipal(Long id) throws ResourceNotFoundException {
        Municipal municipal = municipalRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el servicio de luz con el id proporcionado!!"));
        municipalRepository.delete(municipal);
    }

    @Override
    public ImpuestoMunicipalSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException {
        Municipal municipal = municipalRepository.findById(id).orElse(null);
        ImpuestoMunicipalSalidaDto municipalSalidaDto = null;
        if(municipal!=null){
            municipalSalidaDto = modelMapper.map(municipal, ImpuestoMunicipalSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("No se encontró el servcio de luz con el ID proporcionado");
        }
        return municipalSalidaDto;
    }
}
