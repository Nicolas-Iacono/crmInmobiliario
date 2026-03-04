package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoBasicoDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoUser;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {

    Optional<Inquilino> findByNombreAndApellido(String nombre, String apellido);

    int countByUsuarioUsername(String username);

    @Query("SELECT i FROM Inquilino i WHERE i.dni = :dni OR i.email = :email")
    Optional<Inquilino> findByDniOrEmail(@Param("dni") String dni, @Param("email") String email);

//    Optional<Inquilino> findByUsuarioId(Long usuarioId);
    List<Inquilino> findByUsuarioId(Long userId);
    Optional<Inquilino> findByUsuarioCuentaInquilinoId(Long usuarioId);

    @Query("SELECT i FROM Inquilino i WHERE i.usuario.username = :username")
    List<Inquilino> findInquilinoByUsername(@Param("username") String username);

    @Query("SELECT i FROM Inquilino i WHERE i.usuarioCuentaInquilino.username = :username")
    Optional<Inquilino> findByUsuarioCuentaInquilinoUsername(@Param("username") String username);

    @Query("""
    SELECT i FROM Inquilino i
    WHERE i.usuarioCuentaInquilino.username = :dato
       OR i.usuarioCuentaInquilino.email = :dato
""")
    Optional<Inquilino> findByUsuarioCuentaInquilinoUsernameOrEmail(@Param("dato") String dato);

    @Query("SELECT u.username AS username, u.password AS password FROM Usuario u JOIN u.inquilino p WHERE p.id = :inquilinoId")
    Optional<InquilinoUser> obtenerCredencialesPorInquilino(@Param("inquilinoId") Long inquilinoId);

    @Modifying
    @Query("UPDATE Inquilino i SET i.usuarioCuentaInquilino = NULL WHERE i.usuarioCuentaInquilino.id = :usuarioId")
    void desvincularUsuarioCuenta(@Param("usuarioId") Long usuarioId);

    int countByUsuarioId(Long userId);

    List<Inquilino> findByNombreLikeIgnoreCaseOrApellidoLikeIgnoreCaseAndUsuarioId(
            String nombre, String apellido, Long userId);

    Optional<Inquilino> findByDniAndUsuarioId(String dni, Long userId);

    Optional<Inquilino> findByEmailIgnoreCaseAndUsuarioId(String email, Long userId);

    @Query("""
    SELECT new com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoBasicoDto(
        i.id, i.nombre, i.apellido, i.email
    )
    FROM Inquilino i
    WHERE i.usuarioCuentaInquilino.username = :dato
       OR i.usuarioCuentaInquilino.email = :dato
""")
    Optional<InquilinoBasicoDto> findBasicoByUsuarioCuenta(@Param("dato") String dato);


    @Query("""
    select i
    from Inquilino i
    where i.usuario.id = :userId
      and (
           upper(i.nombre) like upper(concat('%', :texto, '%'))
        or upper(i.apellido) like upper(concat('%', :texto, '%'))
      )
""")
    List<Inquilino> buscarPorNombreOApellido(@Param("texto") String texto, @Param("userId") Long userId);


    Page<Inquilino> findAllByUsuario_Id(Long userId, Pageable pageable);
}
