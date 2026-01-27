package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.PlantillaContratoDtoEntrada;
import com.backend.crmInmobiliario.DTO.salida.PlantillaContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.PlantillaContrato;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.PlantillaContratoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantillaContratoService {

    private final PlantillaContratoRepository plantillaRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    // ✅ Crear plantilla
    @Transactional
    public PlantillaContratoDtoSalida crearPlantilla(PlantillaContratoDtoEntrada dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setNombre(dto.getNombre());
        plantilla.setDescripcion(dto.getDescripcion());
        plantilla.setContenidoHtml(dto.getContenidoHtml());
        plantilla.setUsuario(usuario);

        PlantillaContrato guardada = plantillaRepository.save(plantilla);

        return new PlantillaContratoDtoSalida(
                guardada.getId(),
                guardada.getNombre(),
                guardada.getDescripcion(),
                guardada.getContenidoHtml(),
                usuario.getId(),
                usuario.getNombreNegocio()
        );
    }

    // ✅ Listar plantillas por usuario
    @Transactional(readOnly = true)
    public List<PlantillaContratoDtoSalida> listarPlantillas(Long usuarioId) {
        return plantillaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(p -> new PlantillaContratoDtoSalida(
                        p.getId(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getContenidoHtml(),
                        p.getUsuario().getId(),
                        p.getUsuario().getNombreNegocio()
                ))
                .toList();
    }

    // ✅ Método para obtener ENTIDAD (uso interno, delete, etc.)
    @Transactional(readOnly = true)
    public PlantillaContrato obtenerPlantillaEntidad(Long usuarioId, Long plantillaId) {
        PlantillaContrato plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plantilla no encontrada con ID: " + plantillaId));

        if (!plantilla.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tiene permisos para acceder a esta plantilla.");
        }

        return plantilla;
    }

    // ✅ Método para obtener DTO (uso externo / frontend)
    @Transactional(readOnly = true)
    public PlantillaContratoDtoSalida obtenerPlantillaPorId(Long usuarioId, Long plantillaId) {
        PlantillaContrato plantilla = obtenerPlantillaEntidad(usuarioId, plantillaId);

        return new PlantillaContratoDtoSalida(
                plantilla.getId(),
                plantilla.getNombre(),
                plantilla.getDescripcion(),
                plantilla.getContenidoHtml(),
                plantilla.getUsuario().getId(),
                plantilla.getUsuario().getNombreNegocio()
        );
    }

    // ✅ Eliminar plantilla (usa el método ENTIDAD)
    @Transactional
    public void eliminarPlantilla(Long usuarioId, Long plantillaId) {
        PlantillaContrato plantilla = obtenerPlantillaEntidad(usuarioId, plantillaId);
        plantillaRepository.delete(plantilla);
    }

    // ✅ Generar contrato desde plantilla (reemplaza placeholders)
    @Transactional
    public String generarContratoDesdePlantilla(Long contratoId, Long plantillaId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + contratoId));

        PlantillaContrato plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada con ID: " + plantillaId));

        ContratoSalidaDto dto = modelMapper.map(contrato, ContratoSalidaDto.class);
        String htmlFinal = reemplazarCamposPlantilla(plantilla.getContenidoHtml(), dto);

        contrato.setPdfContratoTexto(htmlFinal);
        contratoRepository.save(contrato);

        return htmlFinal;
    }

    // ✅ Reemplaza todos los placeholders dinámicos del HTML
    private String reemplazarCamposPlantilla(String html, ContratoSalidaDto dto) {
        return html
                .replace("{inquilino_nombre}", safe(dto.getInquilino().getNombre() + " " + dto.getInquilino().getApellido()))
                .replace("{inquilino_dni}", safe(dto.getInquilino().getDni()))
                .replace("{propietario_nombre}", safe(dto.getPropietario().getNombre() + " " + dto.getPropietario().getApellido()))
                .replace("{propiedad_direccion}", safe(dto.getPropiedad().getDireccion()))
                .replace("{propiedad_localidad}", safe(dto.getPropiedad().getLocalidad()))
                .replace("{propiedad_partido}", safe(dto.getPropiedad().getPartido()))
                .replace("{contrato_monto}", safe(String.valueOf(dto.getMontoAlquiler())))
                .replace("{contrato_monto_letras}", safe(dto.getMontoAlquilerLetras()))
                .replace("{contrato_fecha_inicio}", safe(dto.getFecha_inicio().toString()))
                .replace("{contrato_fecha_fin}", safe(dto.getFecha_fin().toString()))
                .replace("{contrato_duracion}", safe(String.valueOf(dto.getDuracion())))
                .replace("{contrato_destino}", safe(dto.getDestino()))
                .replace("{usuario_nombre_negocio}", safe(dto.getUsuarioDtoSalida().getNombreNegocio()))
                .replace("{usuario_localidad}", safe(dto.getUsuarioDtoSalida().getLocalidad()))
                .replace("{usuario_partido}", safe(dto.getUsuarioDtoSalida().getPartido()))
                .replace("{usuario_razon_social}", safe(dto.getUsuarioDtoSalida().getRazonSocial()))
                .replace("{agua_empresa}", safe(dto.getAguaEmpresa()))
                .replace("{luz_empresa}", safe(dto.getLuzEmpresa()))
                .replace("{gas_empresa}", safe(dto.getGasEmpresa()))
                .replace("{municipal_empresa}", safe(dto.getMunicipalEmpresa()));
    }

    // ✅ Evita NullPointerException en los replace()
    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}
