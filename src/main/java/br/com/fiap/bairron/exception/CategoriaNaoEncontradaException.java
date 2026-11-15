package br.com.fiap.bairron.exception;

public class CategoriaNaoEncontradaException extends RuntimeException {
    public CategoriaNaoEncontradaException(Long id) {
        super("Categoria não encontrada: " + id);
    }
}
