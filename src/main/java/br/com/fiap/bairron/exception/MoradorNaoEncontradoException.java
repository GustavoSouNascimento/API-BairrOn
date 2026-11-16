package br.com.fiap.bairron.exception;

public class MoradorNaoEncontradoException extends RuntimeException {
    public MoradorNaoEncontradoException(Long id) {
        super("Morador não encontrado: " + id);
    }
}
