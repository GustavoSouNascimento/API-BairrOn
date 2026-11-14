package br.com.fiap.bairron.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ocorrencia_id", "morador_id"}))
public class Confirmacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoConfirmacao situacao;

    @Column(nullable = false)
    private LocalDateTime confirmadaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    private Ocorrencia ocorrencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "morador_id", nullable = false)
    private Morador morador;

    public Confirmacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SituacaoConfirmacao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoConfirmacao situacao) {
        this.situacao = situacao;
    }

    public LocalDateTime getConfirmadaEm() {
        return confirmadaEm;
    }

    public void setConfirmadaEm(LocalDateTime confirmadaEm) {
        this.confirmadaEm = confirmadaEm;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public Morador getMorador() {
        return morador;
    }

    public void setMorador(Morador morador) {
        this.morador = morador;
    }
}
