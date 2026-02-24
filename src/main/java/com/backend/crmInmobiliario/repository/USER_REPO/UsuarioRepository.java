package com.backend.crmInmobiliario.repository.USER_REPO;

import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.DatosCobroSoloUser;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByPropietarioId(Long propietarioId);

    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Optional<Usuario> findUserByUsername(@Param("username") String username);


    @Query("SELECT u FROM Usuario u WHERE u.nombreNegocio = :nombreNegocio")
    Optional<Usuario> findUserByNombreNegocio(@Param("nombreNegocio") String nombreNegocio);


    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);


    boolean existsByUsername(String username);

    // Para evitar condiciones de carrera, bloqueamos el usuario en la transacción
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u left join fetch u.plan where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);


    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Usuario u WHERE u.username = :username")  // <-- "Usuario" es la entidad
    int deleteByUsername(@Param("username") String username);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Usuario u WHERE u.nombreNegocio = :nombreNegocio")  // <-- "Usuario" es la entidad
    int deleteByNombreNegocio(@Param("nombreNegocio") String nombreNegocio);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.propietario WHERE u.id = :id")
    Optional<Usuario> findUserById(@Param("id") Long id);

    @Query(value = "SELECT u.username, u.password FROM usuario u WHERE u.propietario_id = :propietarioId", nativeQuery = true)
    Optional<Object[]> obtenerCredencialesPorPropietario(@Param("propietarioId") Long propietarioId);

    @Query("""
    SELECT u FROM Usuario u
    WHERE LOWER(u.email) = LOWER(:identifier)
       OR LOWER(u.nombreNegocio) = LOWER(:identifier)
""")
    Optional<Usuario> findByIdentifier(String identifier);

    @EntityGraph(attributePaths = {"roles", "roles.permisosList"})
    @Query("""
    SELECT u FROM Usuario u
    WHERE LOWER(u.email) = LOWER(:identifier)
       OR LOWER(u.nombreNegocio) = LOWER(:identifier)
""")
    Optional<Usuario> c(@Param("identifier") String identifier);



    @Query("""
select distinct u from Usuario u
left join fetch u.roles r
left join fetch r.permisosList p
where lower(u.username) = lower(:identifier)
   or lower(u.email) = lower(:identifier)
   or lower(u.nombreNegocio) = lower(:identifier)
""")
    Optional<Usuario> findByIdentifierWithRolesAndPerms(@Param("identifier") String identifier);


    @Query("""
  select distinct u
  from Usuario u
  left join fetch u.roles r
  left join fetch r.permisosList p
  where lower(u.email) = lower(:identifier)
     or lower(u.nombreNegocio) = lower(:identifier)
     or lower(u.username) = lower(:identifier)
""")
    Optional<Usuario> findByIdentifierWithRoles(String identifier);

    @Query("""
        select new com.backend.crmInmobiliario.DTO.mpDtos.transferencias.salida.DatosCobroSoloUser(
            u.mpAlias,
            u.mpCbu,
            u.mpTitular,
            u.mpCuit,
            u.mpBanco
        )
        from Usuario u
        where u.id = :userId
    """)
    Optional<DatosCobroSoloUser> findDatosCobroById(@Param("userId") Long userId);
}
