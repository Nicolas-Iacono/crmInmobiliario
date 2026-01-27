package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.entity.Nota;
import com.backend.crmInmobiliario.repository.NotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotaSyncService {

    @Autowired
    private NotaRepository notaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarNotaEnSupabasePorId(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));
        // llamar tu método real
        // guardarNotaEnSupabase(nota);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertNotaEmbeddingPorId(Long notaId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada"));
        // upsertNotaEmbedding(nota);
    }
}