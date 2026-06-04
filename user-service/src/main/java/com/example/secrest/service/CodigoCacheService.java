package com.example.secrest.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {

    private static class CodigoInfo {
        String codigo;
        LocalDateTime expiracao;

        CodigoInfo(String codigo, LocalDateTime expiracao) {
            this.codigo = codigo;
            this.expiracao = expiracao;
        }
    }

    private final Map<String, CodigoInfo> cache = new ConcurrentHashMap<>();

    public void salvarCodigo(String email, String codigo) {
        cache.put(
                email,
                new CodigoInfo(
                        codigo,
                        LocalDateTime.now().plusMinutes(5)
                )
        );
    }

    public boolean validarCodigo(String email, String codigo) {

        CodigoInfo info = cache.get(email);

        if (info == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(info.expiracao)) {
            cache.remove(email);
            return false;
        }

        return info.codigo.equals(codigo);
    }

    public void removerCodigo(String email) {
        cache.remove(email);
    }
}