package br.com.fiap.bairron.service;

import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraPrioridadeTest {

    private final CalculadoraPrioridade calculadora = new CalculadoraPrioridade();

    @Test
    void prioridadeInicialVemDaGravidadeDaCategoria() {
        assertEquals(new CalculadoraPrioridade.Resultado(20, Prioridade.BAIXA), calculadora.calcular(2, 0, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(30, Prioridade.BAIXA), calculadora.calcular(3, 0, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(40, Prioridade.MEDIA), calculadora.calcular(4, 0, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(50, Prioridade.MEDIA), calculadora.calcular(5, 0, 0));
    }

    @Test
    void confirmacoesAindaExisteAumentamAPontuacao() {
        assertEquals(new CalculadoraPrioridade.Resultado(60, Prioridade.ALTA), calculadora.calcular(5, 2, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(80, Prioridade.CRITICA), calculadora.calcular(5, 6, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(45, Prioridade.MEDIA), calculadora.calcular(3, 3, 0));
    }

    @Test
    void confirmacoesResolvidoReduzemAPontuacao() {
        assertEquals(new CalculadoraPrioridade.Resultado(45, Prioridade.MEDIA), calculadora.calcular(3, 4, 1));
        assertEquals(new CalculadoraPrioridade.Resultado(25, Prioridade.BAIXA), calculadora.calcular(3, 0, 1));
        assertEquals(new CalculadoraPrioridade.Resultado(0, Prioridade.BAIXA), calculadora.calcular(1, 0, 2));
    }

    @Test
    void cadaTipoDeConfirmacaoConta_noMaximo_dez() {
        assertEquals(calculadora.calcular(3, 10, 0), calculadora.calcular(3, 50, 0));
        assertEquals(new CalculadoraPrioridade.Resultado(80, Prioridade.CRITICA), calculadora.calcular(3, 50, 0));
    }

    @Test
    void faixasDeClassificacao() {
        assertEquals(Prioridade.BAIXA, calculadora.classificar(39));
        assertEquals(Prioridade.MEDIA, calculadora.classificar(40));
        assertEquals(Prioridade.MEDIA, calculadora.classificar(59));
        assertEquals(Prioridade.ALTA, calculadora.classificar(60));
        assertEquals(Prioridade.ALTA, calculadora.classificar(79));
        assertEquals(Prioridade.CRITICA, calculadora.classificar(80));
    }

    @Test
    void resolvidaExigeTresConfirmacoesQueSuperemAsAindaExiste() {
        assertEquals(StatusOcorrencia.ATIVA, calculadora.definirStatus(0, 2));
        assertEquals(StatusOcorrencia.RESOLVIDA, calculadora.definirStatus(0, 3));
        assertEquals(StatusOcorrencia.ATIVA, calculadora.definirStatus(3, 3));
        assertEquals(StatusOcorrencia.RESOLVIDA, calculadora.definirStatus(3, 4));
    }

    @Test
    void ocorrenciaResolvidaTemPontuacaoZero() {
        assertEquals(new CalculadoraPrioridade.Resultado(0, Prioridade.BAIXA), calculadora.calcular(5, 0, 3));
    }
}
