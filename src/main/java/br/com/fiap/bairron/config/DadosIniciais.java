package br.com.fiap.bairron.config;

import br.com.fiap.bairron.dto.ConfirmacaoRequest;
import br.com.fiap.bairron.dto.OcorrenciaRequest;
import br.com.fiap.bairron.entities.*;
import br.com.fiap.bairron.repository.BairroRepository;
import br.com.fiap.bairron.repository.CategoriaRepository;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.ConfirmacaoService;
import br.com.fiap.bairron.service.OcorrenciaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DadosIniciais implements CommandLineRunner {

    public static final String SENHA_TESTE = "Senha@123";

    private final BairroRepository bairroRepository;
    private final CategoriaRepository categoriaRepository;
    private final MoradorRepository moradorRepository;
    private final OcorrenciaService ocorrenciaService;
    private final ConfirmacaoService confirmacaoService;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(BairroRepository bairroRepository, CategoriaRepository categoriaRepository,
                         MoradorRepository moradorRepository, OcorrenciaService ocorrenciaService,
                         ConfirmacaoService confirmacaoService, PasswordEncoder passwordEncoder) {
        this.bairroRepository = bairroRepository;
        this.categoriaRepository = categoriaRepository;
        this.moradorRepository = moradorRepository;
        this.ocorrenciaService = ocorrenciaService;
        this.confirmacaoService = confirmacaoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (bairroRepository.count() != 0) {
            return;
        }

        Bairro mooca = bairro("Mooca", "São Paulo", "SP");
        Bairro pinheiros = bairro("Pinheiros", "São Paulo", "SP");
        Bairro santana = bairro("Santana", "São Paulo", "SP");

        Categoria alagamento = categoria("Alagamento", "Ruas e áreas que alagam com a chuva", 5);
        Categoria arvore = categoria("Árvore em risco de queda", "Árvores doentes ou inclinadas sobre vias e casas", 4);
        Categoria buraco = categoria("Buraco na via", "Buracos em ruas e avenidas que oferecem risco ao trânsito", 3);
        Categoria iluminacao = categoria("Iluminação pública", "Postes apagados ou com lâmpadas queimadas", 3);
        Categoria calcada = categoria("Calçada danificada", "Calçadas quebradas que dificultam a acessibilidade", 2);
        Categoria lixo = categoria("Lixo acumulado", "Descarte irregular e acúmulo de lixo nas ruas", 2);

        morador("Administração BairrOn", "admin@bairron.com", Papel.ADMIN, mooca);
        morador("Moderação BairrOn", "moderador@bairron.com", Papel.MODERADOR, mooca);
        Morador maria = morador("Maria Souza", "maria@bairron.com", Papel.MORADOR, mooca);
        Morador joao = morador("João Pereira", "joao@bairron.com", Papel.MORADOR, pinheiros);
        Morador ana = morador("Ana Ribeiro", "ana@bairron.com", Papel.MORADOR, santana);
        Morador carlos = morador("Carlos Lima", "carlos@bairron.com", Papel.MORADOR, mooca);
        Morador paula = morador("Paula Mendes", "paula@bairron.com", Papel.MORADOR, pinheiros);
        Morador rafael = morador("Rafael Costa", "rafael@bairron.com", Papel.MORADOR, santana);

        long alagamentoId = ocorrencia(maria, "Rua com alagamento recorrente",
                "A água sobe até a calçada a cada chuva forte e invade as garagens.", -23.5589, -46.5976, alagamento, mooca);
        confirmar(alagamentoId, SituacaoConfirmacao.AINDA_EXISTE, joao, ana, carlos, paula, rafael, maria);

        long buracoId = ocorrencia(joao, "Buraco grande na avenida",
                "O buraco ocupa metade da faixa e já causou furos de pneu.", -23.5614, -46.6921, buraco, pinheiros);
        confirmar(buracoId, SituacaoConfirmacao.AINDA_EXISTE, ana, carlos, paula);

        long posteId = ocorrencia(ana, "Poste apagado há semanas",
                "A rua fica totalmente escura à noite perto do ponto de ônibus.", -23.4987, -46.6256, iluminacao, santana);
        confirmar(posteId, SituacaoConfirmacao.AINDA_EXISTE, maria);

        long arvoreId = ocorrencia(paula, "Árvore inclinada sobre a calçada",
                "O tronco está rachado e a árvore se inclina para a rua.", -23.5667, -46.6835, arvore, pinheiros);
        confirmar(arvoreId, SituacaoConfirmacao.AINDA_EXISTE, joao, carlos);

        long lixoId = ocorrencia(carlos, "Lixo acumulado na esquina",
                "Sacos e entulho acumulados em frente ao terreno vazio.", -23.5601, -46.6001, lixo, mooca);
        confirmar(lixoId, SituacaoConfirmacao.RESOLVIDO, maria, joao, ana);

        ocorrencia(rafael, "Calçada quebrada em frente à escola",
                "As lajotas soltas dificultam a passagem de cadeiras de rodas.", -23.4975, -46.6290, calcada, santana);
    }

    private Bairro bairro(String nome, String cidade, String uf) {
        Bairro bairro = new Bairro();
        bairro.setNome(nome);
        bairro.setCidade(cidade);
        bairro.setUf(uf);
        return bairroRepository.save(bairro);
    }

    private Categoria categoria(String nome, String descricao, int gravidade) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categoria.setGravidade(gravidade);
        return categoriaRepository.save(categoria);
    }

    private Morador morador(String nome, String email, Papel papel, Bairro bairro) {
        Morador morador = new Morador();
        morador.setNome(nome);
        morador.setEmail(email);
        morador.setSenha(passwordEncoder.encode(SENHA_TESTE));
        morador.setPapel(papel);
        morador.setBairro(bairro);
        return moradorRepository.save(morador);
    }

    private long ocorrencia(Morador autor, String titulo, String descricao, double latitude, double longitude,
                            Categoria categoria, Bairro bairro) {
        OcorrenciaRequest dados = new OcorrenciaRequest(titulo, descricao, latitude, longitude,
                categoria.getId(), bairro.getId());
        return ocorrenciaService.cadastrar(dados, new UsuarioLogado(autor.getId(), autor.getPapel())).id();
    }

    private void confirmar(long ocorrenciaId, SituacaoConfirmacao situacao, Morador... moradores) {
        for (Morador morador : List.of(moradores)) {
            confirmacaoService.registrar(ocorrenciaId, new ConfirmacaoRequest(situacao),
                    new UsuarioLogado(morador.getId(), morador.getPapel()));
        }
    }
}
