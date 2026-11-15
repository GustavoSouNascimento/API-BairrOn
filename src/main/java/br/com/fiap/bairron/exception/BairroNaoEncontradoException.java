package br.com.fiap.bairron.exception;

public class BairroNaoEncontradoException extends RuntimeException {
    public BairroNaoEncontradoException(Long id) {
        super("Bairro não encontrado: " + id);
    }
}
