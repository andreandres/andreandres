# Estoque — Trabalho Prático 2

Sistema de gestão de estoque com **API REST relacional e interface web**, continuando o tema do Trabalho 1. Backend em Java 17/Spring Boot 3.5.7, Spring Data JPA, Bean Validation e H2. Frontend em HTML, CSS e JavaScript puro, sem dependências de produção.

## Iniciar

Instale **JDK 17 ou 21** e **Maven 3.6.3+**, com `JAVA_HOME` e `PATH` configurados. Confira com `java -version` e `mvn -version`. A implementação foi verificada com JDK 21, gerando bytecode Java 17.

Na pasta `backend`:

```sh
mvn clean verify
mvn spring-boot:run
```

Abra **http://localhost:8080** no navegador. API e interface rodam juntas. Não é necessário instalar Node, iniciar outro servidor ou abrir `index.html` diretamente.

Alternativa no PowerShell, a partir da raiz do trabalho:

```powershell
.\executar.ps1 test
.\executar.ps1 run
```

O script também reconhece ferramentas portáteis em `.tools` ou na pasta do Trabalho 1, quando disponíveis localmente. Em outro computador, basta instalar os pré-requisitos; o projeto não depende desses diretórios locais. Se a política do PowerShell impedir scripts, use os comandos Maven diretamente.

Para mudar a porta: `./executar.ps1 run -Porta 8081` ou, na pasta backend, `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.

Para executar o pacote já compilado, na pasta backend:

```sh
java -jar target/estoque-api-2.0.0.jar
```

Encerre com Ctrl+C. Inicie sempre na pasta backend para manter a localização do banco consistente.

## Usar a interface

1. O banco começa vazio. Clique em **Categorias** e **Nova categoria**, por exemplo, Informática.
2. Em **Produtos**, clique em **Novo produto**. Informe nome, preço, quantidade e selecione a categoria carregada da API.
3. Também é possível criar uma categoria sem sair do formulário de produto, em **Criar uma categoria**.
4. A tabela mostra produto, categoria, preço, quantidade e situação do estoque. Use a busca e os filtros para localizar registros.
5. O ícone de lápis abre a edição. O ícone de lixeira solicita confirmação antes de excluir.
6. Uma categoria com produtos não pode ser excluída: a tela mostra o erro retornado pelo backend. Reclassifique ou exclua seus produtos primeiro.

Os indicadores mostram totais do catálogo completo; os filtros afetam apenas a tabela. Estoque de 1 a 5 unidades recebe o aviso **Estoque baixo**; zero recebe **Sem estoque**. Essas situações são informativas e não impedem cadastros. O valor em estoque corresponde à soma de preço × quantidade; o cálculo usa centavos inteiros. Nenhum dado demonstrativo é inserido automaticamente no banco local.

## Entidades e relacionamento

**Uma Categoria possui vários Produtos. Cada Produto pertence obrigatoriamente a uma Categoria.**

```text
categorias                          produtos
id (PK, IDENTITY)  <───────────────  categoria_id (FK, NOT NULL)
nome (único)                        id (PK, IDENTITY)
                                    nome (único)
                                    preco
                                    quantidade_estoque
                                    data_criacao
```

| Entidade/campo | Tipo Java | Regras |
|---|---|---|
| Categoria.id | Long | Gerado automaticamente; somente leitura |
| Categoria.nome | String | Obrigatório, até 80 caracteres, único |
| Produto.id | Long | Gerado automaticamente; somente leitura |
| Produto.nome | String | Obrigatório, até 120 caracteres, único |
| Produto.preco | BigDecimal | Obrigatório, mínimo 0, até 10 dígitos inteiros e 2 decimais |
| Produto.quantidadeEstoque | Integer | Obrigatório, de 0 a 2147483647 |
| Produto.categoria | Categoria | `@ManyToOne`, chave estrangeira `categoria_id`, obrigatória |
| Produto.dataCriacao | Instant | Criada pelo servidor em UTC, preservada nas atualizações |

Nomes são normalizados removendo espaços nas extremidades. As restrições únicas usam `VARCHAR_IGNORECASE` do H2: nomes como Mouse e MOUSE entram em conflito, inclusive em requisições concorrentes. Esse tipo deve ser adaptado se houver troca de banco.

O relacionamento é unidirecional, de Produto para Categoria; não há lista de produtos dentro de cada categoria na resposta. DTOs de saída controlam o JSON e evitam ciclos de serialização. A listagem usa `@EntityGraph` para carregar a categoria junto com o produto, sem uma consulta adicional para cada linha. `spring.jpa.open-in-view=false` mantém a conversão dos dados dentro da camada de serviço/transação.

O Hibernate cria/atualiza as duas tabelas automaticamente (`ddl-auto=update`). Dados locais ficam em `backend/data/estoque-v2.mv.db`, com conexão `jdbc:h2:file:./data/estoque-v2;WRITE_DELAY=0`, usuário `sa`, senha vazia. `WRITE_DELAY=0` desativa o atraso de escrita do H2, para que commits não fiquem aguardando a próxima gravação periódica ([documentação do H2](https://h2database.com/html/commands.html#set_write_delay)). Trata-se de uma aplicação acadêmica de execução local. Testes automatizados usam H2 em memória separado.

O Trabalho 2 utiliza um banco novo. O antigo campo `categoria` de texto do Trabalho 1 foi substituído pelo relacionamento; não copie o banco antigo para este projeto. Os fontes e o banco do Trabalho 1 permanecem em sua pasta original.

## Rotas

| Método | Rota | Sucesso | Erros principais |
|---|---|---|---|
| GET | /api/produtos | 200, lista de produtos com categoria | 400 se filtro inválido |
| GET | /api/produtos?nome=mouse&categoriaId=1 | 200, filtros combináveis | 400 |
| GET | /api/produtos/{id} | 200, produto com categoria | 400, 404 |
| POST | /api/produtos | 201, objeto e Location | 400, 404, 409 |
| PUT | /api/produtos/{id} | 200, objeto atualizado | 400, 404, 409 |
| DELETE | /api/produtos/{id} | 204, sem corpo | 400, 404 |
| GET | /api/categorias | 200, lista ordenada por nome | — |
| GET | /api/categorias/{id} | 200, categoria | 400, 404 |
| POST | /api/categorias | 201, objeto e Location | 400, 409 |
| PUT | /api/categorias/{id} | 200, categoria atualizada | 400, 404, 409 |
| DELETE | /api/categorias/{id} | 204, sem corpo | 400, 404, 409 se vinculada |

PUT exige todos os campos obrigatórios. PATCH é opcional no enunciado e não foi implementado. O CRUD completo de Categoria vai além do mínimo exigido de GET/POST. IDs positivos são obrigatórios; IDs textuais e parâmetros inválidos retornam 400. O filtro por nome permite trechos sem diferenciar maiúsculas/minúsculas; `%` e `_` têm a semântica de curingas do SQL na API. Na interface a busca é literal e ocorre sobre a listagem recebida.

## JSON e testes manuais

Use `requisicoes.http` com a extensão REST Client do VS Code, ou importe as requisições no Postman/Insomnia. Ajuste `categoriaId` e `produtoId` com os IDs efetivamente retornados. GET e DELETE não precisam de corpo. Exemplos cURL abaixo usam Bash; no Windows use o arquivo `.http` ou `curl.exe` com JSON em arquivo para evitar diferenças nas aspas do PowerShell.

**Criar categoria:**

```sh
curl -i -X POST http://localhost:8080/api/categorias -H 'Content-Type: application/json' -d '{"nome":"Informática"}'
```

Resposta 201, `Location: http://localhost:8080/api/categorias/1` e:

```json
{"id":1,"nome":"Informática"}
```

**Criar produto:**

```sh
curl -i -X POST http://localhost:8080/api/produtos -H 'Content-Type: application/json' -d '{"nome":"Mouse","preco":49.90,"quantidadeEstoque":10,"categoriaId":1}'
```

Resposta 201, `Location: http://localhost:8080/api/produtos/1` e (ID/data ilustrativos):

```json
{"id":1,"nome":"Mouse","preco":49.90,"quantidadeEstoque":10,"categoria":{"id":1,"nome":"Informática"},"dataCriacao":"2026-11-24T12:00:00Z"}
```

**Consultar:**

```sh
curl -i http://localhost:8080/api/produtos
curl -i http://localhost:8080/api/produtos/1
curl -i http://localhost:8080/api/categorias
curl -i http://localhost:8080/api/categorias/1
```

Todas retornam 200: buscas por ID devolvem o objeto mostrado acima; listagens devolvem um array desses objetos ou `[]`.

**Atualizar:**

```sh
curl -i -X PUT http://localhost:8080/api/produtos/1 -H 'Content-Type: application/json' -d '{"nome":"Mouse sem fio","preco":79.90,"quantidadeEstoque":5,"categoriaId":1}'
curl -i -X PUT http://localhost:8080/api/categorias/1 -H 'Content-Type: application/json' -d '{"nome":"Tecnologia"}'
```

Cada PUT retorna 200 e o objeto atualizado no formato das respostas de criação. Ao renomear a categoria, a próxima consulta de produtos já mostra o novo nome.

**Excluir:**

```sh
curl -i -X DELETE http://localhost:8080/api/categorias/1
curl -i -X DELETE http://localhost:8080/api/produtos/1
curl -i -X DELETE http://localhost:8080/api/categorias/1
```

A primeira tentativa retorna 409 enquanto houver produto vinculado. As duas seguintes retornam 204, sem corpo. Buscar um ID excluído retorna 404.

## Validações e erros na GUI

A API usa Bean Validation nos DTOs e respostas Problem Detail. Exemplo de POST inválido:

```json
{"nome":" ","preco":-1,"quantidadeEstoque":-1,"categoriaId":null}
```

Resposta 400:

```json
{
  "type":"about:blank",
  "title":"Bad Request",
  "status":400,
  "detail":"Confira os campos informados",
  "instance":"/api/produtos",
  "campos":{
    "nome":"Informe o nome do produto",
    "preco":"O preço não pode ser negativo",
    "quantidadeEstoque":"A quantidade não pode ser negativa",
    "categoriaId":"Selecione uma categoria"
  }
}
```

A GUI também valida campos básicos com HTML e exibe os erros do backend junto aos campos e em alertas no formulário. Trata conflitos 409, IDs inexistentes 404, indisponibilidade de rede e tempo de espera. Botões ficam bloqueados durante gravações para evitar envios repetidos. As chamadas são assíncronas e usam apenas HTTP (`fetch('/api/...')`); o JavaScript não acessa o banco.

Campos JSON desconhecidos, propriedades de saída como `id`/`dataCriacao`, JSON malformado, conteúdo extra e quantidades fracionárias são rejeitados. Nomes recebidos são inseridos na página com `textContent`, evitando interpretar HTML do usuário. Não há exclusão em cascata de produtos ao remover categoria; a regra de serviço e a FK protegem os vínculos.

## Organização e como foi feito

```text
Trabalho 2/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/br/com/trabalho/estoque/
│       │   ├── controller/  # HTTP, validação de entrada e status
│       │   ├── dto/         # JSON de entrada e saída
│       │   ├── exception/   # tratamento centralizado de erros
│       │   ├── model/       # entidades Produto e Categoria
│       │   ├── repository/  # JpaRepository e consultas
│       │   └── service/     # regras de negócio e transações
│       └── test/           # testes HTTP e integridade relacional
├── frontend/
│   ├── index.html          # telas, tabela, formulários e diálogos
│   ├── styles.css          # layout responsivo e estados visuais
│   └── app.js              # consumo HTTP e atualização da interface
├── testes/                # testes de navegador e reinicialização do banco
├── evidencias/             # relatórios e imagens dos testes
├── executar.ps1
└── requisicoes.http
```

1. A base do Trabalho 1 foi mantida: CRUD de Produto, preço decimal, estoque, data de criação, filtro, validação e erros HTTP.
2. Categoria passou de texto para entidade com repositório, serviço e controller próprios. Produto recebeu `@ManyToOne` e `@JoinColumn` obrigatórios.
3. DTOs de resposta acrescentam a categoria aninhada, sem expor entidades/proxies diretamente ao Jackson.
4. Os serviços verificam duplicações e referências e executam alterações em transações. O banco também aplica unicidade e chave estrangeira.
5. A interface consulta a API, monta as linhas da tabela, preenche o select de categorias e envia POST/PUT/DELETE. Os indicadores usam os dados retornados.
6. O Maven copia `frontend/` para os recursos estáticos no build. O Spring Boot entrega os arquivos por HTTP; a separação do código cliente/servidor continua explícita e não exige CORS.

## Testes automatizados

`mvn clean verify`, na pasta backend, executa **13 testes** com servidor HTTP em porta aleatória e H2 em memória. Cobrem CRUD das duas entidades, categoria no JSON, troca de categoria, preservação da data, validações, JSON inválido, filtros, IDs inválidos/inexistentes, duplicação, bloqueio de exclusão, FK no próprio banco, concorrência e entrega HTTP da interface. Relatórios completos em `backend/target/surefire-reports/`.

Para repetir o teste de navegador, é necessário Node.js, Chrome e Playwright apenas como ferramentas de teste:

```sh
npm install --prefix .tools/browser-qa --no-save --package-lock=false playwright
```

Inicie um **banco de testes vazio** e uma porta separada. Na pasta backend, após compilar:

```sh
java -jar target/estoque-api-2.0.0.jar --server.port=18082 "--spring.datasource.url=jdbc:h2:mem:interface;DB_CLOSE_DELAY=-1" --spring.jpa.hibernate.ddl-auto=create-drop
```

Em outro terminal, na raiz, execute `node testes/interface.cjs`. O script usa Chrome no caminho padrão do Windows. Em outro ambiente, configure `CHROME_EXECUTABLE` com o executável do Chrome/Chromium e, se necessário, `TEST_BASE_URL`. O teste cria dados demonstrativos apenas no banco isolado e gera capturas desktop/mobile; nunca o execute contra o banco com seus cadastros reais.

`node testes/persistencia.cjs` verifica a persistência após encerrar e reiniciar o processo Java. Ele usa um banco em uma pasta temporária e a porta 18083, recusando uma porta que já esteja em uso. Requer Node e Java 17/21; aceita `JAVA_EXE` para informar o executável Java e `TEST_PORT` para escolher outra porta. Não precisa de Playwright.

## Entrega

Publique a pasta do Trabalho 2 em seu repositório GitHub, mantendo `backend/`, `frontend/`, testes, README e requisições. `.gitignore` exclui ferramentas, dependências locais, banco gerado, logs, compilados e ZIPs. A pasta de evidências é complementar e pode acompanhar o código.

## Evidências desta versão

- `evidencias/testes-backend.txt`: 13 testes de integração, sem falhas nem erros.
- `evidencias/testes-interface.txt`: 15 verificações aprovadas no Chrome, incluindo erros reais 400/409 e recuperação de falha de rede.
- `evidencias/testes-persistencia.txt`: dados e relacionamento recuperados após reiniciar o processo com H2 em arquivo.
- `evidencias/nulidade.txt`: compilação adicional pelo Eclipse JDT com análise de nulidade, sem avisos.
- `evidencias/interface-desktop.png` e `interface-mobile.png`: telas com dados demonstrativos criados somente no banco isolado de testes.
