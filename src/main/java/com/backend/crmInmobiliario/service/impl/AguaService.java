package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoAguaEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoAguaSalidaDto;
import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.AguaRepository;
import com.backend.crmInmobiliario.service.IAguaService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AguaService implements IAguaService {
    private final Logger LOGGER = LoggerFactory.getLogger(AguaService.class);
    private ModelMapper modelMapper;
    private AguaRepository aguaRepository;

    public AguaService( ModelMapper modelMapper, AguaRepository aguaRepository) {
        this.modelMapper = modelMapper;
        this.aguaRepository = aguaRepository;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true); // Ignorar ambigüedad en el mapeo.
    }

    @Override
    public List<ImpuestoAguaSalidaDto> listarImpuestoAgua() {
        List<Agua> impuestosAgua = aguaRepository.findAll();
        return impuestosAgua.stream()
                .map(impuestoAgua->modelMapper.map(impuestoAgua, ImpuestoAguaSalidaDto.class))
                .toList();
    }

    @Override
    public ImpuestoAguaSalidaDto crearImpuestoAgua(ImpuestoAguaEntradaDto impuestoAguaEntradaDto) throws ResourceNotFoundException {
       Agua aguaServicio = new Agua();
       aguaServicio.setEmpresa(impuestoAguaEntradaDto.getEmpresa());
       aguaServicio.setPorcentaje(impuestoAguaEntradaDto.getPorcentaje());
       aguaServicio.setDescripcion(impuestoAguaEntradaDto.getDescripcion());
       aguaServicio.setNumeroMedidor(impuestoAguaEntradaDto.getNumeroMedidor());
       aguaServicio.setNumeroCliente(impuestoAguaEntradaDto.getNumeroCliente());
       aguaServicio.setMontoAPagar(impuestoAguaEntradaDto.getMontoAPagar());
       aguaServicio.setFechaFactura(impuestoAguaEntradaDto.getFechaFactura());


       Agua servicioToSave = aguaRepository.save(aguaServicio);
       ImpuestoAguaSalidaDto aguaSalidaDto = modelMapper.map(servicioToSave, ImpuestoAguaSalidaDto.class);
       return aguaSalidaDto;
    }

    @Override
    public void eliminarImpuestoAgua(Long id) throws ResourceNotFoundException {
        Agua agua = aguaRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el servicio de agua con el id proporcionado!!"));
        aguaRepository.delete(agua);
    }

    @Override
    public ImpuestoAguaSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException {
       Agua agua = aguaRepository.findById(id).orElse(null);
       ImpuestoAguaSalidaDto aguaSalidaDto = null;
       if(agua!=null){
           aguaSalidaDto = modelMapper.map(agua, ImpuestoAguaSalidaDto.class);
       }else{
           throw new ResourceNotFoundException("No se encontró el servcio de agua con el ID proporcionado");
       }

        return aguaSalidaDto;
    }
}
