package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.service.signature.SignatureResolvedSignerData;
import com.backend.crmInmobiliario.service.signature.SignatureSignerResolverService;
import org.springframework.stereotype.Service;

@Service
public class SignatureSignerResolverServiceImpl implements SignatureSignerResolverService {

    private final GaranteRepository garanteRepository;

    public SignatureSignerResolverServiceImpl(GaranteRepository garanteRepository) {
        this.garanteRepository = garanteRepository;
    }

    @Override
    public SignatureResolvedSignerData resolveAndValidate(Contrato contract,
                                                          SignatureSignerRoleType signerRoleType,
                                                          Long relatedEntityId) {
        return switch (signerRoleType) {
            case PROPIETARIO -> resolveOwner(contract, relatedEntityId);
            case INQUILINO -> resolveTenant(contract, relatedEntityId);
            case GARANTE -> resolveGuarantor(contract, relatedEntityId);
        };
    }

    private SignatureResolvedSignerData resolveOwner(Contrato contract, Long relatedEntityId) {
        Propietario owner = contract.getPropietario();
        if (owner == null || !owner.getId().equals(relatedEntityId)) {
            throw new IllegalArgumentException("El propietario no pertenece al contrato");
        }
        return build(SignatureSignerRoleType.PROPIETARIO, owner.getId(), owner.getNombre(), owner.getApellido(), owner.getEmail(), owner.getTelefono(), owner.getDni());
    }

    private SignatureResolvedSignerData resolveTenant(Contrato contract, Long relatedEntityId) {
        Inquilino tenant = contract.getInquilino();
        if (tenant == null || !tenant.getId().equals(relatedEntityId)) {
            throw new IllegalArgumentException("El inquilino no pertenece al contrato");
        }
        return build(SignatureSignerRoleType.INQUILINO, tenant.getId(), tenant.getNombre(), tenant.getApellido(), tenant.getEmail(), tenant.getTelefono(), tenant.getDni());
    }

    private SignatureResolvedSignerData resolveGuarantor(Contrato contract, Long relatedEntityId) {
        Garante guarantor = garanteRepository.findById(relatedEntityId)
                .orElseThrow(() -> new IllegalArgumentException("Garante inexistente"));

        if (guarantor.getContrato() == null || !contract.getId().equals(guarantor.getContrato().getId())) {
            throw new IllegalArgumentException("El garante no pertenece al contrato");
        }
        return build(SignatureSignerRoleType.GARANTE, guarantor.getId(), guarantor.getNombre(), guarantor.getApellido(), guarantor.getEmail(), guarantor.getTelefono(), guarantor.getDni());
    }

    private SignatureResolvedSignerData build(SignatureSignerRoleType role,
                                              Long relatedEntityId,
                                              String name,
                                              String lastName,
                                              String email,
                                              String phone,
                                              String dni) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El firmante de tipo " + role + " no tiene email");
        }

        String fullName = ((name == null ? "" : name) + " " + (lastName == null ? "" : lastName)).trim();
        if (fullName.isBlank()) {
            fullName = role.name() + "#" + relatedEntityId;
        }

        return SignatureResolvedSignerData.builder()
                .signerRoleType(role)
                .relatedEntityId(relatedEntityId)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .dni(dni)
                .build();
    }
}
