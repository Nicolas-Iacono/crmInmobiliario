package com.backend.crmInmobiliario.repository.USER_REPO;

import com.backend.crmInmobiliario.entity.UsuarioGoogleAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioGoogleAccountRepository extends JpaRepository<UsuarioGoogleAccount, Long> {
    Optional<UsuarioGoogleAccount> findByGoogleSub(String googleSub);
    Optional<UsuarioGoogleAccount> findByUsuarioId(Long usuarioId);
    boolean existsByGoogleSub(String googleSub);

}
