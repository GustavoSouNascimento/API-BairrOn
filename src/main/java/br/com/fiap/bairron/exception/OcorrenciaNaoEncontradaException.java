package br.com.fiap.bairron.exception;

public class OcorrenciaNaoEncontradaException extends RuntimeException {
    public OcorrenciaNaoEncontradaException(Long id) {
        super("Ocorrência não encontrada: " + id);
    }
}
