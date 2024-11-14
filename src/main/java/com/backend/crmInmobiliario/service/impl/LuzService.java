package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoLuzEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoLuzSalidaDto;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.LuzRepository;
import com.backend.crmInmobiliario.service.IAguaService;
import com.backend.crmInmobiliario.service.ILuzService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LuzService implements ILuzService {
    private final Logger LOGGER = LoggerFactory.getLogger(LuzService.class);
    private ModelMapper modelMapper;
    private LuzRepository luzRepository;

    public LuzService(ModelMapper modelMapper, LuzRepository luzRepository) {
        this.modelMapper = modelMapper;
        this.luzRepository = luzRepository;
    }

    @Override
    public List<ImpuestoLuzSalidaDto> listarImpuestoLuz() {
        List<Luz> impuestosLuz = luzRepository.findAll();
        return impuestosLuz.stream()
                .map(impuestoLuz->modelMapper.map(impuestoLuz, ImpuestoLuzSalidaDto.class))
                .toList();
    }


    @Override
    public ImpuestoLuzSalidaDto crearImpuestoLuz(ImpuestoLuzEntradaDto impuestoLuzEntradaDto) throws ResourceNotFoundException {
        Luz luzServicio = new Luz();
        luzServicio.setEmpresa(impuestoLuzEntradaDto.getEmpresa());
        luzServicio.setPorcentaje(impuestoLuzEntradaDto.getPorcentaje());
        luzServicio.setDescripcion(impuestoLuzEntradaDto.getDescripcion());
        luzServicio.setNumeroMedidor(impuestoLuzEntradaDto.getNumeroMedidor());
        luzServicio.setNumeroCliente(impuestoLuzEntradaDto.getNumeroCliente());
        luzServicio.setMontoAPagar(impuestoLuzEntradaDto.getMontoAPagar());
        luzServicio.setFechaFactura(impuestoLuzEntradaDto.getFechaFactura());


        Luz servicioToSave = luzRepository.save(luzServicio);
        ImpuestoLuzSalidaDto luzSalidaDto = modelMapper.map(servicioToSave, ImpuestoLuzSalidaDto.class);
        return luzSalidaDto;
    }

    @Override
    public void eliminarImpuestoLuz(Long id) throws ResourceNotFoundException {
        Luz luz = luzRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el servicio de luz con el id proporcionado!!"));
        luzRepository.delete(luz);
    }

    @Override
    public ImpuestoLuzSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException {
        Luz luz = luzRepository.findById(id).orElse(null);
        ImpuestoLuzSalidaDto luzSalidaDto = null;
        if(luz!=null){
            luzSalidaDto = modelMapper.map(luz, ImpuestoLuzSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("No se encontró el servcio de luz con el ID proporcionado");
        }
        return luzSalidaDto;
    }
}
