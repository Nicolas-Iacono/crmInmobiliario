package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoGasEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoGasSalidaDto;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.GasRepository;
import com.backend.crmInmobiliario.service.IGasService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GasService implements IGasService {
    private final Logger LOGGER = LoggerFactory.getLogger(GasService.class);
    private ModelMapper modelMapper;
    private GasRepository gasRepository;

    public GasService(ModelMapper modelMapper, GasRepository gasRepository) {
        this.modelMapper = modelMapper;
        this.gasRepository = gasRepository;
    }

    @Override
    public List<ImpuestoGasSalidaDto> listarImpuestoGas() {
        List<Gas> impuestosGas = gasRepository.findAll();
        return impuestosGas.stream()
                .map(impuestoGas->modelMapper.map(impuestoGas, ImpuestoGasSalidaDto.class))
                .toList();
    }

    @Override
    public ImpuestoGasSalidaDto crearImpuestoGas(ImpuestoGasEntradaDto impuestoGasEntradaDto) throws ResourceNotFoundException {
        Gas gasServicio = new Gas();
        gasServicio.setEmpresa(impuestoGasEntradaDto.getEmpresa());
        gasServicio.setPorcentaje(impuestoGasEntradaDto.getPorcentaje());
        gasServicio.setDescripcion(impuestoGasEntradaDto.getDescripcion());
        gasServicio.setNumeroMedidor(impuestoGasEntradaDto.getNumeroMedidor());
        gasServicio.setNumeroCliente(impuestoGasEntradaDto.getNumeroCliente());
        gasServicio.setMontoAPagar(impuestoGasEntradaDto.getMontoAPagar());
        gasServicio.setFechaFactura(impuestoGasEntradaDto.getFechaFactura());

        Gas servicioToSave = gasRepository.save(gasServicio);
        ImpuestoGasSalidaDto gasSalidaDto = modelMapper.map(servicioToSave, ImpuestoGasSalidaDto.class);
        return gasSalidaDto;
    }

    @Override
    public void eliminarImpuestoGas(Long id) throws ResourceNotFoundException {
        Gas gas = gasRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el servicio de gas con el id proporcionado!!"));
        gasRepository.delete(gas);
    }

    @Override
    public ImpuestoGasSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException {
        Gas gas = gasRepository.findById(id).orElse(null);
        ImpuestoGasSalidaDto gasSalidaDto = null;
        if(gas!=null){
            gasSalidaDto = modelMapper.map(gas, ImpuestoGasSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("No se encontró el servcio de gas con el ID proporcionado");
        }
        return gasSalidaDto;
    }

}
