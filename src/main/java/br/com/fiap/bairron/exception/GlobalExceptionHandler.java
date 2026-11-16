package br.com.fiap.bairron.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({BairroNaoEncontradoException.class, CategoriaNaoEncontradaException.class,
            MoradorNaoEncontradoException.class, OcorrenciaNaoEncontradaException.class})
    public ResponseEntity<ProblemDetail> naoEncontrado(RuntimeException exception) {
        return erro(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> conflito(ConflitoException exception) {
        return erro(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integridade(DataIntegrityViolationException exception) {
        return erro(HttpStatus.CONFLICT, "Os dados informados conflitam com registros existentes.");
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ProblemDetail> acessoNegado(AcessoNegadoException exception) {
        return erro(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ProblemDetail> credenciaisInvalidas(CredenciaisInvalidasException exception) {
        return erro(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ProblemDetail> parametroInvalido(ParametroInvalidoException exception) {
        return erro(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Confira os campos informados.");
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        problema.setProperty("campos", campos);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> requisicaoInvalida(Exception exception) {
        return erro(HttpStatus.BAD_REQUEST, "JSON ou parâmetro inválido.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> metodoNaoPermitido(HttpRequestMethodNotSupportedException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(exception.getHeaders()).body(problema);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> erroInesperado(Exception exception) {
        if (exception instanceof ErrorResponse erro && erro.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(erro.getStatusCode()).headers(erro.getHeaders()).body(erro.getBody());
        }
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<ProblemDetail> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, mensagem));
    }
}
