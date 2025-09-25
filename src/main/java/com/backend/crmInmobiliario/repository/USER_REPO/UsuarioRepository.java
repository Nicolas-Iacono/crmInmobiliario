package com.backend.crmInmobiliario.repository.USER_REPO;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {


    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Optional<Usuario> findUserByUsername(@Param("username") String username);

    Optional<Usuario> findByEmail(String email);


    boolean existsByUsername(String username);

    // Para evitar condiciones de carrera, bloqueamos el usuario en la transacción
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u left join fetch u.plan where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);


    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Usuario u WHERE u.username = :username")  // <-- "Usuario" es la entidad
    int deleteByUsername(@Param("username") String username);
}
