package br.com.fiap.bairron;

import br.com.fiap.bairron.config.CachesPainel;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class BairronApiTest {

    private static final String SENHA = "Senha@123";
    private static final String ADMIN = "admin@bairron.com";
    private static final String MODERADOR = "moderador@bairron.com";
    private static final String MARIA = "maria@bairron.com";
    private static final String JOAO = "joao@bairron.com";
    private static final String ANA = "ana@bairron.com";
    private static final String CARLOS = "carlos@bairron.com";
    private static final String PAULA = "paula@bairron.com";
    private static final String RAFAEL = "rafael@bairron.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void limparCaches() {
        cacheManager.getCacheNames().forEach(nome -> {
            Cache cache = cacheManager.getCache(nome);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    // Auxiliares

    private String corpo(MvcResult resultado) throws Exception {
        return resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private long id(MvcResult resultado) throws Exception {
        Number id = JsonPath.read(corpo(resultado), "$.id");
        return id.longValue();
    }

    private String token(String email) throws Exception {
        MvcResult resultado = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, SENHA)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(corpo(resultado), "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private long idBairro(String nome) throws Exception {
        MvcResult resultado = mvc.perform(get("/bairros").param("nome", nome))
                .andExpect(status().isOk())
                .andReturn();
        Number id = JsonPath.read(corpo(resultado), "$._embedded.bairros[0].id");
        return id.longValue();
    }

    private long idCategoria(String nome) throws Exception {
        MvcResult resultado = mvc.perform(get("/categorias").param("nome", nome))
                .andExpect(status().isOk())
                .andReturn();
        Number id = JsonPath.read(corpo(resultado), "$._embedded.categorias[0].id");
        return id.longValue();
    }

    private String corpoOcorrencia(String titulo, long categoriaId, long bairroId) {
        return """
                {"titulo":"%s","descricao":"Descrição do problema relatado","latitude":-23.55,"longitude":-46.63,"categoriaId":%d,"bairroId":%d}"""
                .formatted(titulo, categoriaId, bairroId);
    }

    private long criarOcorrencia(String email, String titulo, long categoriaId, long bairroId) throws Exception {
        MvcResult resultado = mvc.perform(post("/ocorrencias")
                        .header(AUTHORIZATION, bearer(token(email)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOcorrencia(titulo, categoriaId, bairroId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        return id(resultado);
    }

    private ResultActions confirmar(String email, long ocorrenciaId, String situacao) throws Exception {
        return mvc.perform(post("/ocorrencias/{id}/confirmacoes", ocorrenciaId)
                .header(AUTHORIZATION, bearer(token(email)))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\":\"%s\"}".formatted(situacao)));
    }

    private ResultActions consultar(String email, long ocorrenciaId) throws Exception {
        return mvc.perform(get("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(email))));
    }

    // Autenticação e papéis

    @Test
    void cadastraELogaMorador() throws Exception {
        long bairroId = idBairro("Mooca");
        String cadastro = """
                {"nome":"Fernanda Lima","email":"fernanda@exemplo.com","senha":"Senha@123","bairroId":%d}""".formatted(bairroId);

        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.papel").value("MORADOR"))
                .andExpect(jsonPath("$.email").value("fernanda@exemplo.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));

        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"fernanda@exemplo.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.papel").value("MORADOR"))
                .andReturn();
        String token = JsonPath.read(corpo(login), "$.token");

        mvc.perform(get("/moradores/eu").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fernanda@exemplo.com"))
                .andExpect(jsonPath("$.bairroNome").value("Mooca"));
    }

    @Test
    void validaCadastroELogin() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"123\",\"bairroId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Fulano\",\"email\":\"fulano@exemplo.com\",\"senha\":\"Senha@123\",\"bairroId\":999999}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@bairron.com\",\"senha\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@bairron.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exigeTokenValido() throws Exception {
        mvc.perform(get("/ocorrencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/ocorrencias").header(AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/moradores/eu"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void somenteModeradorGerenciaMoradores() throws Exception {
        mvc.perform(get("/moradores").header(AUTHORIZATION, bearer(token(MARIA))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/moradores").header(AUTHORIZATION, bearer(token(MODERADOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.moradores.length()").value(8))
                .andExpect(jsonPath("$._embedded.moradores[*].senha").doesNotHaveJsonPath());
        mvc.perform(get("/moradores").header(AUTHORIZATION, bearer(token(ADMIN))))
                .andExpect(status().isOk());
        mvc.perform(get("/moradores").param("sort", "senha,asc").header(AUTHORIZATION, bearer(token(MODERADOR))))
                .andExpect(status().isBadRequest());
    }

    // Bairros e categorias

    @Test
    void crudDeBairro() throws Exception {
        String bairro = "{\"nome\":\"Vila Nova\",\"cidade\":\"Campinas\",\"uf\":\"sp\"}";

        mvc.perform(post("/bairros").contentType(MediaType.APPLICATION_JSON).content(bairro))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/bairros").header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON).content(bairro))
                .andExpect(status().isForbidden());

        String moderador = bearer(token(MODERADOR));
        MvcResult criado = mvc.perform(post("/bairros").header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON).content(bairro))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resumo.href").exists())
                .andReturn();
        long id = id(criado);

        mvc.perform(post("/bairros").header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON).content(bairro))
                .andExpect(status().isConflict());

        mvc.perform(get("/bairros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Vila Nova"));

        mvc.perform(put("/bairros/{id}", id).header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Vila Nova Campinas\",\"cidade\":\"Campinas\",\"uf\":\"SP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Vila Nova Campinas"));

        mvc.perform(delete("/bairros/{id}", id).header(AUTHORIZATION, moderador))
                .andExpect(status().isNoContent());
        mvc.perform(get("/bairros/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(delete("/bairros/{id}", id).header(AUTHORIZATION, moderador))
                .andExpect(status().isNotFound());
    }

    @Test
    void validaBairro() throws Exception {
        mvc.perform(post("/bairros").header(AUTHORIZATION, bearer(token(MODERADOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"cidade\":\"\",\"uf\":\"SPP\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cidade").exists())
                .andExpect(jsonPath("$.campos.uf").exists());
    }

    @Test
    void naoExcluiBairroComOcorrencias() throws Exception {
        mvc.perform(delete("/bairros/{id}", idBairro("Mooca")).header(AUTHORIZATION, bearer(token(MODERADOR))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listaBairrosComPaginacaoEOrdenacao() throws Exception {
        mvc.perform(get("/bairros").param("size", "2").param("sort", "nome,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bairros.length()").value(2))
                .andExpect(jsonPath("$._embedded.bairros[0].nome").value("Santana"))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$._links.next.href").exists());
        mvc.perform(get("/bairros").param("sort", "invalido,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void crudDeCategoriaERecalculoDePrioridade() throws Exception {
        String moderador = bearer(token(MODERADOR));
        mvc.perform(post("/categorias").header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sinalização\",\"descricao\":\"Placas e semáforos\",\"gravidade\":2}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/categorias").header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sinalização\",\"descricao\":\"Placas e semáforos\",\"gravidade\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.gravidade").exists());

        MvcResult criada = mvc.perform(post("/categorias").header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sinalização\",\"descricao\":\"Placas e semáforos\",\"gravidade\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gravidade").value(2))
                .andReturn();
        long categoriaId = id(criada);
        mvc.perform(post("/categorias").header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"sinalização\",\"descricao\":\"Outra\",\"gravidade\":1}"))
                .andExpect(status().isConflict());

        long ocorrenciaId = criarOcorrencia(MARIA, "Placa de pare caída", categoriaId, idBairro("Mooca"));
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.pontuacao").value(20))
                .andExpect(jsonPath("$.prioridade").value("BAIXA"));
        mvc.perform(put("/categorias/{id}", categoriaId).header(AUTHORIZATION, moderador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sinalização\",\"descricao\":\"Placas e semáforos\",\"gravidade\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gravidade").value(5));
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.pontuacao").value(50))
                .andExpect(jsonPath("$.prioridade").value("MEDIA"));

        mvc.perform(delete("/categorias/{id}", categoriaId).header(AUTHORIZATION, moderador))
                .andExpect(status().isConflict());
        mvc.perform(delete("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, moderador))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/categorias/{id}", categoriaId).header(AUTHORIZATION, moderador))
                .andExpect(status().isNoContent());
        mvc.perform(get("/categorias/{id}", categoriaId))
                .andExpect(status().isNotFound());
    }

    // Ocorrências

    @Test
    void validaOcorrencia() throws Exception {
        String maria = bearer(token(MARIA));
        mvc.perform(post("/ocorrencias").header(AUTHORIZATION, maria).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"descricao\":\"\",\"latitude\":200,\"longitude\":-46.6,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists())
                .andExpect(jsonPath("$.campos.latitude").exists())
                .andExpect(jsonPath("$.campos.bairroId").exists());
        mvc.perform(post("/ocorrencias").header(AUTHORIZATION, maria).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoOcorrencia("Teste", 999999, idBairro("Mooca"))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/ocorrencias").header(AUTHORIZATION, maria).contentType(MediaType.APPLICATION_JSON)
                        .content("{json quebrado"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void prioridadeSobeComConfirmacoes() throws Exception {
        long ocorrenciaId = criarOcorrencia(MARIA, "Buraco na rua lateral",
                idCategoria("Buraco"), idBairro("Mooca"));
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.pontuacao").value(30))
                .andExpect(jsonPath("$.prioridade").value("BAIXA"))
                .andExpect(jsonPath("$.totalAindaExiste").value(0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.bairro.href").exists())
                .andExpect(jsonPath("$._links.categoria.href").exists())
                .andExpect(jsonPath("$._links.confirmar.href", containsString("/ocorrencias/" + ocorrenciaId + "/confirmacoes")))
                .andExpect(jsonPath("$._links.ocorrencias.href", containsString("/ocorrencias")))
                .andExpect(jsonPath("$._links.excluir.href", containsString("/ocorrencias/" + ocorrenciaId)));

        confirmar(JOAO, ocorrenciaId, "AINDA_EXISTE").andExpect(status().isCreated());
        confirmar(ANA, ocorrenciaId, "AINDA_EXISTE")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pontuacaoOcorrencia").value(40))
                .andExpect(jsonPath("$.prioridadeOcorrencia").value("MEDIA"));

        confirmar(CARLOS, ocorrenciaId, "AINDA_EXISTE");
        confirmar(PAULA, ocorrenciaId, "AINDA_EXISTE");
        confirmar(RAFAEL, ocorrenciaId, "AINDA_EXISTE");
        confirmar(MODERADOR, ocorrenciaId, "AINDA_EXISTE")
                .andExpect(jsonPath("$.pontuacaoOcorrencia").value(60))
                .andExpect(jsonPath("$.prioridadeOcorrencia").value("ALTA"));

        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.totalAindaExiste").value(6))
                .andExpect(jsonPath("$.totalResolvido").value(0))
                .andExpect(jsonPath("$.prioridade").value("ALTA"));
    }

    @Test
    void confirmacaoDoMesmoMoradorSubstituiAAnterior() throws Exception {
        long ocorrenciaId = criarOcorrencia(MARIA, "Poste apagado na esquina",
                idCategoria("Iluminação"), idBairro("Santana"));

        confirmar(JOAO, ocorrenciaId, "AINDA_EXISTE").andExpect(status().isCreated());
        confirmar(JOAO, ocorrenciaId, "AINDA_EXISTE").andExpect(status().isOk());
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.totalAindaExiste").value(1))
                .andExpect(jsonPath("$.totalResolvido").value(0));

        confirmar(JOAO, ocorrenciaId, "RESOLVIDO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("RESOLVIDO"));
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.totalAindaExiste").value(0))
                .andExpect(jsonPath("$.totalResolvido").value(1));

        mvc.perform(get("/ocorrencias/{id}/confirmacoes", ocorrenciaId)
                        .header(AUTHORIZATION, bearer(token(MARIA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.confirmacoes[0].moradorId").exists());

        mvc.perform(post("/ocorrencias/{id}/confirmacoes", ocorrenciaId)
                        .header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"TALVEZ\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/ocorrencias/{id}/confirmacoes", ocorrenciaId)
                        .header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.situacao").exists());
        mvc.perform(post("/ocorrencias/{id}/confirmacoes", 999999)
                        .header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\":\"RESOLVIDO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ocorrenciaViraResolvidaEPodeReabrir() throws Exception {
        long ocorrenciaId = criarOcorrencia(MARIA, "Lixo na calçada",
                idCategoria("Lixo"), idBairro("Mooca"));

        confirmar(JOAO, ocorrenciaId, "RESOLVIDO");
        confirmar(ANA, ocorrenciaId, "RESOLVIDO");
        consultar(MARIA, ocorrenciaId).andExpect(jsonPath("$.status").value("ATIVA"));

        confirmar(CARLOS, ocorrenciaId, "RESOLVIDO")
                .andExpect(jsonPath("$.statusOcorrencia").value("RESOLVIDA"))
                .andExpect(jsonPath("$.prioridadeOcorrencia").value("BAIXA"))
                .andExpect(jsonPath("$.pontuacaoOcorrencia").value(0));
        consultar(MARIA, ocorrenciaId)
                .andExpect(jsonPath("$.status").value("RESOLVIDA"))
                .andExpect(jsonPath("$.totalResolvido").value(3));
        confirmar(CARLOS, ocorrenciaId, "AINDA_EXISTE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusOcorrencia").value("ATIVA"));
    }

    @Test
    void listaComFiltrosPaginacaoEOrdenacao() throws Exception {
        String token = bearer(token(MARIA));

        mvc.perform(get("/ocorrencias").param("prioridade", "CRITICA").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias[0].titulo").value("Rua com alagamento recorrente"))
                .andExpect(jsonPath("$._embedded.ocorrencias[*].prioridade", everyItem(is("CRITICA"))));

        mvc.perform(get("/ocorrencias").param("status", "RESOLVIDA").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias[*].status", everyItem(is("RESOLVIDA"))));

        mvc.perform(get("/ocorrencias").param("texto", "ALAGAMENTO").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias[0].titulo", containsString("alagamento")));

        mvc.perform(get("/ocorrencias").param("bairroId", String.valueOf(idBairro("Santana")))
                        .header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias[*].bairroNome", everyItem(is("Santana"))));

        mvc.perform(get("/ocorrencias").param("size", "2").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$._links.next.href").exists())
                .andExpect(jsonPath("$._embedded.ocorrencias[0].pontuacao", greaterThanOrEqualTo(80)));
        mvc.perform(get("/ocorrencias").param("sort", "pontuacao,asc").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias[0].pontuacao").value(0))
                .andExpect(jsonPath("$._embedded.ocorrencias[0].status").value("RESOLVIDA"));

        mvc.perform(get("/ocorrencias").param("sort", "morador,asc").header(AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/ocorrencias").param("status", "INEXISTENTE").header(AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/ocorrencias").param("size", "1000").header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(50));
    }

    @Test
    void somenteAutorOuModeradorAlteramEExcluem() throws Exception {
        long categoriaId = idCategoria("Calçada");
        long bairroId = idBairro("Mooca");
        long ocorrenciaId = criarOcorrencia(MARIA, "Calçada com buraco", categoriaId, bairroId);
        String corpo = corpoOcorrencia("Calçada com buraco grande", categoriaId, bairroId);

        mvc.perform(put("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(JOAO)))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(delete("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(JOAO))))
                .andExpect(status().isForbidden());

        consultar(JOAO, ocorrenciaId)
                .andExpect(jsonPath("$._links.atualizar").doesNotExist())
                .andExpect(jsonPath("$._links.excluir").doesNotExist())
                .andExpect(jsonPath("$._links.confirmar.href").exists());

        mvc.perform(put("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(MARIA)))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Calçada com buraco grande"));
        mvc.perform(put("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(MODERADOR)))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());

        confirmar(JOAO, ocorrenciaId, "AINDA_EXISTE").andExpect(status().isCreated());
        mvc.perform(delete("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, bearer(token(MARIA))))
                .andExpect(status().isNoContent());
        consultar(MARIA, ocorrenciaId).andExpect(status().isNotFound());
        mvc.perform(get("/ocorrencias/{id}/confirmacoes", ocorrenciaId)
                        .header(AUTHORIZATION, bearer(token(MARIA))))
                .andExpect(status().isNotFound());
    }

    // Painel e cache

    @Test
    void painelDoBairro() throws Exception {
        String token = bearer(token(MARIA));
        long mooca = idBairro("Mooca");

        mvc.perform(get("/bairros/{id}/resumo", mooca).header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bairroNome").value("Mooca"))
                .andExpect(jsonPath("$.totalAtivas").value(1))
                .andExpect(jsonPath("$.totalResolvidas").value(1))
                .andExpect(jsonPath("$.porPrioridade.CRITICA").value(1))
                .andExpect(jsonPath("$.porPrioridade.BAIXA").value(0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.bairro.href").exists())
                .andExpect(jsonPath("$._links.prioritarias.href").exists());

        mvc.perform(get("/bairros/{id}/ocorrencias/prioritarias", mooca).header(AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocorrencias.length()").value(1))
                .andExpect(jsonPath("$._embedded.ocorrencias[0].prioridade").value("CRITICA"))
                .andExpect(jsonPath("$._links.resumo.href").exists());

        mvc.perform(get("/bairros/{id}/ocorrencias/prioritarias", mooca).param("limite", "0")
                        .header(AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/bairros/{id}/resumo", 999999).header(AUTHORIZATION, token))
                .andExpect(status().isNotFound());
        mvc.perform(get("/bairros/{id}/resumo", mooca))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void escritasInvalidamOCacheDoPainel() throws Exception {
        String token = bearer(token(MARIA));
        long mooca = idBairro("Mooca");

        Cache cacheResumo = cacheManager.getCache(CachesPainel.RESUMO);
        assertNull(cacheResumo.get(mooca));
        mvc.perform(get("/bairros/{id}/resumo", mooca).header(AUTHORIZATION, token))
                .andExpect(jsonPath("$.totalAtivas").value(1));
        assertNotNull(cacheResumo.get(mooca));
        mvc.perform(get("/bairros/{id}/resumo", mooca).header(AUTHORIZATION, token))
                .andExpect(jsonPath("$.totalAtivas").value(1));

        long novaId = criarOcorrencia(MARIA, "Árvore caída", idCategoria("Árvore"), mooca);
        assertNull(cacheResumo.get(mooca));
        mvc.perform(get("/bairros/{id}/resumo", mooca).header(AUTHORIZATION, token))
                .andExpect(jsonPath("$.totalAtivas").value(2));

        confirmar(JOAO, novaId, "RESOLVIDO");
        confirmar(ANA, novaId, "RESOLVIDO");
        confirmar(CARLOS, novaId, "RESOLVIDO");
        mvc.perform(get("/bairros/{id}/resumo", mooca).header(AUTHORIZATION, token))
                .andExpect(jsonPath("$.totalAtivas").value(1))
                .andExpect(jsonPath("$.totalResolvidas").value(2));
    }

    // Actuator, documentação e erros

    @Test
    void actuatorHealthPublicoInfoEMetricsSoAdmin() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").exists());

        for (String rota : List.of("/actuator/info", "/actuator/metrics", "/actuator/metrics/jvm.memory.used")) {
            mvc.perform(get(rota))
                    .andExpect(status().isUnauthorized());
            mvc.perform(get(rota).header(AUTHORIZATION, bearer(token(MARIA))))
                    .andExpect(status().isForbidden());
            mvc.perform(get(rota).header(AUTHORIZATION, bearer(token(MODERADOR))))
                    .andExpect(status().isForbidden());
        }

        String admin = bearer(token(ADMIN));
        mvc.perform(get("/actuator/info").header(AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("API BairrOn"))
                .andExpect(jsonPath("$.app.description").exists())
                .andExpect(jsonPath("$.app.version").value("1.0.0"))
                .andExpect(jsonPath("$.java.version").exists())
                .andExpect(jsonPath("$.os.name").exists());
        mvc.perform(get("/actuator/metrics").header(AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
        mvc.perform(get("/actuator/metrics/jvm.memory.used").header(AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jvm.memory.used"));
    }

    @Test
    void adminTemOsPoderesDeModeracao() throws Exception {
        String admin = bearer(token(ADMIN));
        MvcResult criado = mvc.perform(post("/bairros").header(AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Jardim Azul\",\"cidade\":\"Santos\",\"uf\":\"SP\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        mvc.perform(delete("/bairros/{id}", id(criado)).header(AUTHORIZATION, admin))
                .andExpect(status().isNoContent());

        long ocorrenciaId = criarOcorrencia(MARIA, "Lixo na praça", idCategoria("Lixo"), idBairro("Mooca"));
        mvc.perform(delete("/ocorrencias/{id}", ocorrenciaId).header(AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void documentacaoPublicaEEsquemaDeSeguranca() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/ocorrencias']").exists())
                .andExpect(jsonPath("$.paths['/ocorrencias/{ocorrenciaId}/confirmacoes']").exists())
                .andExpect(jsonPath("$.paths['/bairros/{bairroId}/resumo']").exists())
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.components.securitySchemes['bearer-jwt'].scheme").value("bearer"))
                .andExpect(jsonPath("$.components.schemas.Ocorrencia").doesNotExist());
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void errosMantemProblemDetail() throws Exception {
        String token = bearer(token(MARIA));
        mvc.perform(patch("/ocorrencias/1").header(AUTHORIZATION, token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405));
        mvc.perform(get("/rota-inexistente").header(AUTHORIZATION, token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/ocorrencias/abc").header(AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", not(containsString("Exception"))));
    }
}
