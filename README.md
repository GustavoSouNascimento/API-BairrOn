# BairrOn - API REST do BairrOn
 
Neste README será documentado tudo sobre a API do BairrOn, o BairrOn um mapa colaborativo em que os moradores registram problemas do bairro (alagamentos, buracos, iluminação quebrada, lixo acumulado e outros).
O sistema calcula a prioridade de cada problema (crítica, alta, média ou baixa) e os moradores confirmam se ele ainda existe ou já foi resolvido.

## 1. Integrantes
| Nome | RM | Turma |
|------|----|-------|
| Murillo Kenzo | RM566948 | 2TDSPA |
| Kauan Prado | RM566990 | 2TDSPA |
| Cauã Rocha | RM567593 | 2TDSPR |
| Gustavo Souza | RM567134 | 2TDSPR |

## 2. Vídeo pitch
 
https://youtu.be/DwwF5jys8YU

## 3. Como rodar
 
Requisito: **JDK 25**. 
 
```bash

.\mvnw.cmd spring-boot:run      
```
 
A API sobe em `http://localhost:8080` com um banco **H2** (`./data/bairron`) criado e populado na primeira execução com bairros, categorias, moradores e ocorrências de exemplo (classe `DadosIniciais`).
Nas execuções seguintes os dados são mantidos. Para recomeçar do zero, pare a aplicação e apague a pasta `data/`.
 
| Recurso | Endereço |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
 
### Chave do JWT
 
Por padrão a API usa uma chave de desenvolvimento definida em `application.properties`, que **não protege nada**. Fora do ambiente local, defina a variável de ambiente `JWT_SECRET` (mínimo de 32 caracteres):
 
```powershell
$env:JWT_SECRET="chave"   
```
 
Nenhuma senha ou chave real fica no repositório.
 
### Primeiros passos no Swagger
 
1. Abra `http://localhost:8080/swagger-ui.html`.
2. Em `POST /auth/login`, use um dos usuários de teste e copie o campo `token` da resposta.
3. Clique em **Authorize** e cole o token (sem a palavra `Bearer`).
4. Os demais endpoints passam a aceitar a requisição de acordo com o papel do usuário.
Exemplos de corpos de requisição estão na pasta `exemplos/` (`login.json`, `cadastro.json`, `bairro.json`, `categoria.json`, `ocorrencia.json` e `confirmacao.json`).
 
## 4. Usuários de teste por papel
 
Todos usam a senha **`Senha@123`** (apenas para ambiente local).
 
| Papel | E-mail | Bairro |
|---|---|---|
| ADMIN | admin@bairron.com | Mooca |
| MODERADOR | moderador@bairron.com | Mooca |
| MORADOR | maria@bairron.com | Mooca |
| MORADOR | joao@bairron.com | Pinheiros |
| MORADOR | ana@bairron.com | Santana |
| MORADOR | carlos@bairron.com | Mooca |
| MORADOR | paula@bairron.com | Pinheiros |
| MORADOR | rafael@bairron.com | Santana |
 
| Papel | O que pode fazer |
|---|---|
| **MORADOR** | registrar ocorrências, confirmar (ainda existe ou resolvido), editar e excluir **as próprias** ocorrências, ver o painel do bairro |
| **MODERADOR** | tudo do morador, mais gerenciar bairros e categorias, editar e excluir qualquer ocorrência e consultar moradores |
| **ADMIN** | tudo do moderador, mais acessar `info` e `metrics` do Actuator |
 
Novos moradores criados em `POST /auth/cadastro` sempre recebem o papel **MORADOR**.
 
## 4. Diagrama das entidades
 
 
## 5. Justificativa da modelagem dos relacionamentos
 
| Decisão | Como ficou | Por quê |
|---|---|---|
| Lado dono da relação | `@ManyToOne` em `Ocorrencia` (categoria, bairro e morador), `Morador` (bairro) e `Confirmacao` (ocorrência e morador), todos com `optional = false` e `@JoinColumn(nullable = false)` | a chave estrangeira fica no lado "muitos", e uma ocorrência ou confirmação não faz sentido sem os donos dela. |
| Lado inverso | `@OneToMany(mappedBy = ...)` em `Bairro`, `Categoria`, `Morador` e `Ocorrencia` | permite navegar do lado "um" para os "muitos" sem criar tabelas extras nem colunas duplicadas. |
| **Fetch** | todas as associações `@ManyToOne` são `FetchType.LAZY`; as `@OneToMany` usam o padrão `LAZY` do JPA | carregar uma ocorrência não traz bairro, categoria e morador junto se a resposta não precisar deles. |
| Controle de N+1 | `default_batch_fetch_size=20` em `application.properties` e `join fetch` na consulta `buscarPrioritarias` | quando a resposta precisa dos nomes de categoria e bairro, o Hibernate busca em lotes de 20 em vez de uma consulta por linha. |
| `open-in-view=false` | a conexão não fica aberta durante a serialização da resposta | os serviços montam os DTOs dentro de `@Transactional`, e o controller nunca devolve entidade. |
| **Cascade** | nenhum `cascade` e nenhum `orphanRemoval` | apagar um bairro, categoria ou morador não pode apagar dados em silêncio. Bairros e categorias em uso devolvem **409**. Só ao excluir uma ocorrência o serviço apaga antes as confirmações dela (`deleteAllByOcorrenciaId`), porque as confirmações são parte da ocorrência. |
| Unicidade | `Confirmacao` tem `UniqueConstraint(ocorrencia_id, morador_id)`; `Categoria.nome` e `Morador.email` são únicos | cada morador tem **uma** confirmação por ocorrência (reenviar troca a situação), e o banco garante isso mesmo em requisições simultâneas. |
| Enums | `@Enumerated(EnumType.STRING)` para papel, status, prioridade e situação | o texto no banco continua correto se a ordem dos valores do enum mudar. |
| Totais gravados | `totalAindaExiste` e `totalResolvido` ficam na `Ocorrencia` | veja 7. Justificativa das consultas. |
 
### Regra de prioridade
 
```
pontuação = gravidade da categoria x 10
          + confirmações "ainda existe" x 5
          - confirmações "resolvido" x 5
```
 
Cada tipo de confirmação conta no máximo 10, e a pontuação nunca fica abaixo de 0.
 
| Pontuação | Prioridade |
|---|---|
| 80 ou mais | CRITICA |
| 60 a 79 | ALTA |
| 40 a 59 | MEDIA |
| abaixo de 40 | BAIXA |
 
A ocorrência passa a **resolvida** quando há pelo menos 3 confirmações "resolvido" e elas superam as "ainda existe". Nesse caso a pontuação vai a 0 e a prioridade abaixa. Se os moradores mudarem de ideia, ela volta a **ativa**.
 
A regra fica em `CalculadoraPrioridade` e é recalculada quando a ocorrência é criada ou editada, quando chega uma confirmação e quando a gravidade da categoria muda.
 
Exemplos dos dados iniciais:
 
| Ocorrência | Gravidade | Confirmações | Pontuação | Resultado |
|---|---|---|---|---|
| Rua com alagamento recorrente | 5 | 6 "ainda existe" | 50 + 30 = 80 | CRITICA |
| Buraco grande na avenida | 3 | 3 "ainda existe" | 30 + 15 = 45 | MEDIA |
| Árvore inclinada sobre a calçada | 4 | 2 "ainda existe" | 40 + 10 = 50 | MEDIA |
| Poste apagado há semanas | 3 | 1 "ainda existe" | 30 + 5 = 35 | BAIXA |
| Calçada quebrada em frente à escola | 2 | nenhuma | 20 | BAIXA |
| Lixo acumulado na esquina | 2 | 3 "resolvido" | 0 | RESOLVIDA |
 
## 6. Endpoints
 
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/cadastro` | público | cadastra morador. (papel MORADOR) |
| POST | `/auth/login` | público | devolve o token JWT. |
| GET | `/bairros`, `/bairros/{id}` | público | consulta com paginação, ordenação e filtro `nome`. |
| POST, PUT, DELETE | `/bairros`, `/bairros/{id}` | MODERADOR ou ADMIN | gerencia bairros. |
| GET | `/categorias`, `/categorias/{id}` | público | consulta com paginação, ordenação e filtro `nome`. |
| POST, PUT, DELETE | `/categorias`, `/categorias/{id}` | MODERADOR ou ADMIN | gerencia categorias. |
| GET | `/moradores/eu` | autenticado | dados do morador logado. |
| GET | `/moradores`, `/moradores/{id}` | MODERADOR ou ADMIN | consulta moradores. |
| GET | `/ocorrencias` | autenticado | filtros `bairroId`, `categoriaId`, `status`, `prioridade`, `texto`; paginação e ordenação. |
| GET | `/ocorrencias/{id}` | autenticado | consulta uma ocorrência. |
| POST | `/ocorrencias` | autenticado | registra ocorrência. (201) |
| PUT, DELETE | `/ocorrencias/{id}` | autor, MODERADOR ou ADMIN | altera ou exclui. |
| POST | `/ocorrencias/{id}/confirmacoes` | autenticado | confirma "ainda existe" ou "resolvido". (201 na primeira, 200 ao trocar) |
| GET | `/ocorrencias/{id}/confirmacoes` | autenticado | lista confirmações. |
| GET | `/bairros/{id}/resumo` | autenticado | ativas por prioridade e total de resolvidas. (cache) |
| GET | `/bairros/{id}/ocorrencias/prioritarias?limite=5` | autenticado | ocorrências ativas, da mais para a menos prioritária. (cache) |
| GET | `/actuator/health` | público | saúde da aplicação, com detalhes. |
| GET | `/actuator/info`, `/actuator/metrics`, `/actuator/metrics/{nome}` | ADMIN | informações da API e métricas. |
 
### Status Code
 
| Código | Quando |
|---|---|
| 200 | consulta ou atualização com sucesso; confirmação que troca a situação anterior |
| 201 | recurso criado (cabeçalho `Location` aponta para ele) |
| 204 | exclusão com sucesso |
| 400 | validação, JSON ou parâmetro inválido, campo de ordenação não permitido |
| 401 | token ausente, inválido ou expirado; e-mail ou senha incorretos |
| 403 | papel sem permissão, ou morador tentando alterar ocorrência de outro |
| 404 | recurso inexistente |
| 405 | método HTTP não permitido na rota |
| 409 | nome duplicado, e-mail já cadastrado, bairro ou categoria em uso |
 
### Paginação e ordenação
 
Parâmetros `page`, `size` (máximo 50, padrão 10) e `sort=campo,asc|desc`. Cada recurso aceita ordenar apenas pelos campos abaixo; qualquer outro devolve 400.
 
| Recurso | Campos de ordenação | Ordem padrão |
|---|---|---|
| bairros | `id`, `nome`, `cidade`, `uf` | `nome` |
| categorias | `id`, `nome`, `gravidade` | `nome` |
| moradores | `id`, `nome`, `email`, `papel` | `nome` |
| ocorrências | `id`, `titulo`, `pontuacao`, `status`, `criadaEm` | `pontuacao` decrescente |
| confirmações | `id`, `confirmadaEm`, `situacao` | `confirmadaEm` decrescente |
 
A lista branca evita que um nome de campo qualquer na URL vire erro 500 ou exponha campos internos.
 
## 7. Justificativa das consultas
 
| Consulta | Tipo | Onde | Por quê |
|---|---|---|---|
| `findByEmailIgnoreCase`, `existsByEmailIgnoreCase` | query method derivado | `MoradorRepository` | login e checagem de e-mail duplicado sem escrever JPQL. |
| `findByNomeContainingIgnoreCase`, `existsByNomeIgnoreCase...` | query method derivado | `BairroRepository`, `CategoriaRepository` | filtro por nome e unicidade; a consulta é simples, então o nome do método basta. |
| `existsByBairroId`, `existsByCategoriaId` | query method derivado | `MoradorRepository`, `OcorrenciaRepository` | só precisa saber **se** há vínculo para devolver 409 ao excluir, sem carregar as linhas. |
| `findByOcorrenciaIdAndMoradorId`, `countByOcorrenciaIdAndSituacao`, `findByOcorrenciaId` | query method derivado | `ConfirmacaoRepository` | localiza a confirmação do morador, conta cada situação para recalcular a prioridade e lista as confirmações com paginação. |
| `buscarPrioritarias` | `@Query` JPQL | `OcorrenciaRepository` | ordena por `pontuacao desc` e, no empate, pela mais antiga (`criadaEm asc`), filtra por bairro e status e usa `join fetch` de categoria, bairro e morador para montar a resposta com **uma única consulta** (sem N+1). A ordenação e o `join fetch` não cabem em um nome de método. |
| `resumirPorPrioridade` | `@Query` JPQL com `group by` | `OcorrenciaRepository` | conta as ocorrências ativas por prioridade no banco, em vez de carregar tudo na memória. |
| `OcorrenciaSpecification.filtrar` | Specification | `OcorrenciaRepository` | a listagem aceita até cinco filtros combináveis (bairro, categoria, status, prioridade e texto no título ou na descrição). A Specification monta a consulta só com os filtros informados; com query methods seriam dezenas de combinações. |
 
Os totais `totalAindaExiste` e `totalResolvido` ficam gravados na ocorrência. A tabela `confirmacao` continua sendo a fonte da verdade: os totais são recontados a cada confirmação (`countByOcorrenciaIdAndSituacao`). Assim as listas mostram os números e ordenam por pontuação sem contar confirmações linha a linha.
 
## 8. Justificativa do cache
 
Cache **Caffeine** configurado em `ConfiguracoesDeCache` (`@EnableCaching` e um `CaffeineCacheManager` com no máximo 1000 entradas, expiração de 10 minutos após a escrita e `recordStats()`), nas duas consultas de leitura mais frequentes, as do painel do bairro:
 
| Cache | Método | Chave |
|---|---|---|
| `resumoBairro` | `PainelService.resumo` | id do bairro |
| `prioritariasBairro` | `PainelService.prioritarias` | id do bairro e limite |
 
**Por que essas duas:** são abertas a cada visita ao mapa do bairro, agregam várias linhas (agrupamento e ordenação) e mudam pouco em relação ao número de leituras. O resto da API é de escrita ou de leitura simples por id, onde o cache traria pouco ganho.
 
**Invalidação:** as escritas que alteram esses resultados invalidam os dois caches com `@CacheEvict(cacheNames = {...}, allEntries = true)`:
 
| Serviço | Operações com `@CacheEvict` |
|---|---|
| `OcorrenciaService` | cadastrar, atualizar e excluir |
| `ConfirmacaoService` | registrar |
| `BairroService` | atualizar e excluir |
| `CategoriaService` | atualizar e excluir |
 
Foi escolhida a invalidação total porque há poucas entradas e a prioridade de uma ocorrência pode mudar vários resultados ao mesmo tempo (por exemplo, a mudança de gravidade de uma categoria recalcula todas as ocorrências dela). A expiração de 10 minutos é uma segunda proteção contra dado velho.
 
**Como ver funcionando:** acompanhe o log da aplicação. As mensagens `Consultando o resumo do bairro ... no banco` e `Consultando as ocorrências prioritárias do bairro ... no banco` só aparecem quando a consulta vai ao banco. Chamar `GET /bairros/{id}/resumo` duas vezes registra o log apenas na primeira; depois de qualquer escrita (por exemplo, uma nova confirmação) o log volta a aparecer.
 
## 9. Segurança e fluxo do JWT
 
- Cadastro em `POST /auth/cadastro` e login em `POST /auth/login`.
- O token é um **JWT assinado com HS256**, com os dados `sub` (e-mail), `id`, `papel`, emissor `api-bairron` e validade de 60 minutos (`bairron.jwt.expiracao-minutos`).
- Senhas guardadas com **BCrypt**. A senha nunca volta nas respostas.
- Os papéis `MORADOR`, `MODERADOR` e `ADMIN` viram as autoridades `ROLE_MORADOR`, `ROLE_MODERADOR` e `ROLE_ADMIN`. As regras por rota estão em `SecurityConfig`; a regra de autoria (autor ou moderador) fica no serviço.
- Sessão stateless e CSRF desligado (API que usa token no cabeçalho).
- **CORS** liberado para o app Mobile pela propriedade `bairron.cors.origens` (padrão `*`, lista separada por vírgulas; restrinja em produção). Métodos `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS`; cabeçalhos `Authorization`, `Content-Type` e `Accept`.
 
### Erros padronizados
 
Todos os erros seguem `ProblemDetail` (`application/problem+json`), inclusive 401 e 403, que saem do filtro de segurança (`ProblemaSegurancaHandler`). A classe `GlobalExceptionHandler` (`@RestControllerAdvice`) trata:
 
| Situação | Código |
|---|---|
| validação do corpo (resposta traz o mapa `campos` com cada erro) | 400 |
| JSON malformado, parâmetro com tipo errado, campo de ordenação inválido | 400 |
| bairro, categoria, morador ou ocorrência inexistente | 404 |
| método HTTP não permitido | 405 |
| duplicidade, vínculo existente ou violação de integridade | 409 |
| acesso negado pela regra de autoria | 403 |
| e-mail ou senha inválidos | 401 |
| erro inesperado (mensagem genérica, sem detalhes internos) | 500 |
 
Exemplo de erro de validação (o texto de cada campo é a mensagem padrão do Bean Validation e depende do idioma do sistema):
 
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Confira os campos informados.",
  "campos": { "titulo": "must not be blank" }
}
```
 
A validação usa Bean Validation nos DTOs de entrada (`@NotBlank`, `@Size`, `@Email`, `@NotNull`, `@Min`/`@Max` na gravidade de 1 a 5, `@DecimalMin`/`@DecimalMax` em latitude e longitude e `@Pattern` na UF com duas letras).
 
## 10. HATEOAS
 
Os links são montados com `linkTo(methodOn(...))` nos assemblers (`assembler/`), então acompanham as rotas dos controllers. As respostas de bairros, categorias, ocorrências e do resumo do bairro seguem o formato HAL (`application/hal+json`) com `_links`:
 
| Recurso | Links |
|---|---|
| bairro | `self`, `bairros`, `resumo`, `prioritarias` |
| categoria | `self` e `categorias` |
| ocorrência | `self`, `ocorrencias`, `bairro`, `categoria`, `confirmacoes` e a ação `confirmar` |
| ocorrência (autor, moderador ou admin) | também `atualizar` e `excluir` |
| confirmação | `ocorrencia` e `confirmacoes` |
| resumo do bairro | `self`, `bairro`, `prioritarias` |
| ocorrências prioritárias do bairro | `self`, `bairro`, `resumo` |
 
As **ações possíveis** mudam conforme o usuário: `confirmar` aparece para todos, mas `atualizar` e `excluir` só para quem pode alterar a ocorrência. O cabeçalho `Location` dos `POST` vem do link `self` do recurso criado. As listas vêm paginadas com `_embedded`, `_links` (`next`, `prev`) e `page`.
 
## 11. Actuator
 
Dependência `spring-boot-starter-actuator`, com os endpoints `health`, `info` e `metrics` expostos (`management.endpoints.web.exposure.include`).
 
| Endpoint | Acesso | O que mostra |
|---|---|---|
| `GET /actuator/health` | público | `{"status":"UP"}` e os detalhes dos componentes (`management.endpoint.health.show-details=always`) |
| `GET /actuator/info` | ADMIN | nome, descrição e versão da API (`info.app.*`), além de dados de Java e do sistema operacional |
| `GET /actuator/metrics` | ADMIN | lista dos nomes de métricas |
| `GET /actuator/metrics/{nome}` | ADMIN | por exemplo `jvm.memory.used`, `process.cpu.usage`, `process.uptime` e `http.server.requests` (aceita `?tag=method:GET&tag=status:200`) |
 
As regras ficam em `SecurityConfig`, com `EndpointRequest.to("health")` liberado e `EndpointRequest.to("info", "metrics")` restrito a `ROLE_ADMIN`. O Actuator não expõe senhas, tokens nem chaves: os valores sensíveis não estão em propriedades expostas.
 
## 12. Swagger / OpenAPI
 
A documentação é gerada com **springdoc-openapi** e fica em `http://localhost:8080/swagger-ui.html` (JSON em `/v3/api-docs`). Todos os endpoints têm resumo (`@Operation`) e as respostas possíveis (`@ApiResponse`), inclusive os erros em `application/problem+json`. Os controllers são agrupados por `@Tag` (autenticação, bairros, categorias, moradores, ocorrências, confirmações e painel do bairro) e os DTOs de entrada trazem exemplos de valores (`@Schema`).
 
O esquema de segurança `bearer-jwt` (HTTP Bearer, formato JWT) é aplicado a todas as rotas. As rotas públicas (`/auth/**`, `GET /bairros` e `GET /categorias`) aparecem sem cadeado. O botão **Authorize** guarda o token para as chamadas seguintes.
 
## Testes
 
```bash
./mvnw test
```
 
Os testes usam H2 (`jdbc:h2:mem:teste`, `ddl-auto=create-drop`) e não mexem no banco em arquivo.
 
| Classe | O que cobre |
|---|---|
| `CalculadoraPrioridadeTest` (7 testes) | prioridade inicial pela gravidade, efeito das confirmações, limite de 10 por tipo, faixas, condição para resolver e pontuação zero |
| `BairronApiTest` (21 testes) | cadastro e login, token inválido, papéis (inclusive ADMIN), CRUD de bairros e categorias, validações, 409 ao excluir bairro em uso, recálculo ao mudar a gravidade, confirmações (troca da anterior, resolver e reabrir), filtros, paginação e ordenação, regra de autoria, painel do bairro, **invalidação do cache nas escritas**, Actuator (health público; info e metrics só ADMIN), OpenAPI e Swagger UI, e o formato `ProblemDetail` dos erros |
