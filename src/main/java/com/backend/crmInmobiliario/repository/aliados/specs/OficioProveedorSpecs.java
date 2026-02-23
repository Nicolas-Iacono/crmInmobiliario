package com.backend.crmInmobiliario.repository.aliados.specs;

import com.backend.crmInmobiliario.entity.OficioProveedor;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class OficioProveedorSpecs {

    public static Specification<OficioProveedor> porProvincia(String provincia) {
        return (root, query, cb) ->
                provincia == null || provincia.isBlank()
                        ? cb.conjunction()
                        : cb.equal(cb.lower(root.get("provincia")), provincia.toLowerCase());
    }

    public static Specification<OficioProveedor> porLocalidad(String localidad) {
        return (root, query, cb) ->
                localidad == null || localidad.isBlank()
                        ? cb.conjunction()
                        : cb.equal(cb.lower(root.get("localidad")), localidad.toLowerCase());
    }

    public static Specification<OficioProveedor> porCategoria(String categoria) {
        return (root, query, cb) -> {
            if (categoria == null || categoria.isBlank()) {
                return cb.conjunction();
            }
            Join<OficioProveedor, String> join = root.join("categorias");
            return cb.equal(cb.lower(join), categoria.toLowerCase());
        };
    }
}