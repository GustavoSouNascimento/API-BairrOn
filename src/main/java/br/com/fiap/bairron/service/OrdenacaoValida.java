package br.com.fiap.bairron.service;

import br.com.fiap.bairron.exception.ParametroInvalidoException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.TreeSet;

public final class OrdenacaoValida {
    private OrdenacaoValida() {
    }

    public static void verificar(Pageable pageable, Set<String> permitidas) {
        for (Sort.Order ordem : pageable.getSort()) {
            if (!permitidas.contains(ordem.getProperty())) {
                throw new ParametroInvalidoException("Ordenação inválida: " + ordem.getProperty()
                        + ". Use um destes campos: " + String.join(", ", new TreeSet<>(permitidas)) + ".");
            }
        }
    }
}
