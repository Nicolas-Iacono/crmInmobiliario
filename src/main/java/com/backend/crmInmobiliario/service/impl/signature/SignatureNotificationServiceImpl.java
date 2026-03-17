package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.signature.SignatureRequestStatus;
import com.backend.crmInmobiliario.entity.signature.SignatureSigner;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.NotificacionRepository;
import com.backend.crmInmobiliario.service.signature.SignatureNotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SignatureNotificationServiceImpl implements SignatureNotificationService {

    private final NotificacionRepository notificacionRepository;
    private final PropietarioRepository propietarioRepository;
    private final InquilinoRepository inquilinoRepository;
    private final GaranteRepository garanteRepository;

    public SignatureNotificationServiceImpl(NotificacionRepository notificacionRepository,
                                            PropietarioRepository propietarioRepository,
                                            InquilinoRepository inquilinoRepository,
                                            GaranteRepository garanteRepository) {
        this.notificacionRepository = notificacionRepository;
        this.propietarioRepository = propietarioRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.garanteRepository = garanteRepository;
    }

    @Override
    public void notifySignerInvitation(SignatureSigner signer) {
        Usuario signerUser = resolveSignerUser(signer);
        if (signerUser == null) {
            return;
        }

        String message = "Tenés una solicitud de firma pendiente del contrato " + signer.getContract().getNombreContrato();
        saveNotification(signerUser, signer.getContract(), TipoNotificacion.FIRMA_SOLICITADA, message);
    }

    @Override
    public void notifyAgencySignerSigned(SignatureSigner signer) {
        Usuario agencyUser = signer.getSignatureRequest().getCreatedByUser();
        String message = "Firmó " + signer.getFullName() + " (" + signer.getSignerRoleType() + ") en el contrato "
                + signer.getContract().getNombreContrato();
        saveNotification(agencyUser, signer.getContract(), TipoNotificacion.FIRMA_PARCIAL, message);
    }

    @Override
    public void notifyAgencyRequestCompleted(SignatureSigner signer) {
        if (signer.getSignatureRequest().getStatus() != SignatureRequestStatus.SIGNED) {
            return;
        }
        Usuario agencyUser = signer.getSignatureRequest().getCreatedByUser();
        String message = "Se completó la firma de todos los participantes para el contrato "
                + signer.getContract().getNombreContrato();
        saveNotification(agencyUser, signer.getContract(), TipoNotificacion.FIRMA_COMPLETADA, message);
    }

    private Usuario resolveSignerUser(SignatureSigner signer) {
        return switch (signer.getSignerRoleType()) {
            case PROPIETARIO -> propietarioRepository.findById(signer.getRelatedEntityId())
                    .map(Propietario::getUsuarioCuentaPropietario)
                    .orElse(null);
            case INQUILINO -> inquilinoRepository.findById(signer.getRelatedEntityId())
                    .map(Inquilino::getUsuarioCuentaInquilino)
                    .orElse(null);
            case GARANTE -> garanteRepository.findById(signer.getRelatedEntityId())
                    .map(Garante::getUsuarioCuentaGarante)
                    .orElse(null);
        };
    }

    private void saveNotification(Usuario targetUser,
                                  Contrato contract,
                                  TipoNotificacion type,
                                  String message) {
        if (targetUser == null) {
            return;
        }

        Notificacion n = new Notificacion();
        n.setUsuario(targetUser);
        n.setContrato(contract);
        n.setTipo(type);
        n.setEstado(EstadoNotificacion.PENDIENTE);
        n.setMensaje(message);
        n.setFechaCreacion(LocalDate.now());
        notificacionRepository.save(n);
    }
}
