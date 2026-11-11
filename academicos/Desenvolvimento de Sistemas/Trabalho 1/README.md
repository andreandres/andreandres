# Estoque API — Trabalho Prático

API REST de produtos com Java 17+, Spring Boot 3.5.7, Spring Web, Spring Data JPA, Bean Validation e H2. O banco contém **uma única entidade de domínio: Produto**. Não há interface gráfica nesta etapa.

## Executar via terminal

Pré-requisitos: **JDK 17 ou superior** e **Maven 3.6.3 ou superior**, disponíveis no PATH. Confira com `java -version` e `mvn -version` (o Maven também precisa usar o JDK 17+).

Na pasta que contém `pom.xml`:

```sh
mvn clean verify
mvn spring-boot:run
```

A API estará em `http://localhost:8080/api/produtos`. Encerre com Ctrl+C. Alternativamente, após compilar:

```sh
java -jar target/estoque-api-1.0.0.jar
```

No PowerShell, também é possível usar `./executar.ps1 test` para testar e `./executar.ps1 run` para iniciar. O script usa JDK/Maven portáteis da pasta `.tools`, caso existam; senão, usa as ferramentas instaladas. `.tools` não faz parte da entrega: em outro computador, instale os pré-requisitos acima. Caso a política local bloqueie scripts, use os comandos Maven diretamente.

O Hibernate cria a tabela automaticamente. O H2 grava os dados em `./data/estoque.mv.db`, preservando-os entre execuções iniciadas na mesma pasta. Usuário `sa`, senha vazia; banco local para fins didáticos. Não execute duas instâncias simultâneas usando o mesmo arquivo. Para mudar a porta: `java -jar target/estoque-api-1.0.0.jar --server.port=8081`.

## Entidade e regras

| Campo | Tipo | Regra |
|---|---|---|
| id | Long | Chave primária gerada por IDENTITY; somente leitura |
| nome | String | Obrigatório, não vazio, até 120 caracteres; único |
| preco | BigDecimal | Obrigatório, mínimo 0, até 10 dígitos inteiros e 2 decimais |
| quantidadeEstoque | Integer | Obrigatório, mínimo 0 |
| categoria | String | Opcional, até 80 caracteres |
| dataCriacao | Instant | Gerada no servidor em UTC; somente leitura |

Espaços nas extremidades de nome/categoria são removidos. Nomes são únicos sem diferenciar maiúsculas/minúsculas, com verificação na aplicação e restrição única no H2 usando `VARCHAR_IGNORECASE`. Esse tipo é específico do H2 e deve ser adaptado se o banco for trocado. O PUT exige todos os campos obrigatórios; se categoria for omitida, fica `null`. Campos desconhecidos (incluindo id e dataCriacao no corpo) são rejeitados. PATCH não foi implementado, pois é opcional no enunciado.

## Rotas

| Método | Rota | Sucesso | Erros principais |
|---|---|---|---|
| GET | /api/produtos | 200, lista JSON (ou `[]`) | — |
| GET | /api/produtos?nome=mouse | 200, busca por trecho sem diferenciar caixa | — |
| GET | /api/produtos/{id} | 200, objeto JSON | 400, 404 |
| POST | /api/produtos | 201, objeto JSON e cabeçalho Location | 400, 409 |
| PUT | /api/produtos/{id} | 200, objeto atualizado | 400, 404, 409 |
| DELETE | /api/produtos/{id} | 204, sem corpo | 400, 404 |

## Exemplos HTTP

Abra `requisicoes.http` na extensão REST Client do VS Code, ou use os comandos abaixo em Bash. No Windows PowerShell, use `curl.exe` no lugar de `curl`; para evitar diferenças de aspas entre versões, o arquivo `.http` é a alternativa mais simples. Os IDs são exemplos: use o ID retornado na criação.

### POST — criar

```sh
curl -i -X POST http://localhost:8080/api/produtos -H 'Content-Type: application/json' -d '{"nome":"Mouse","preco":49.90,"quantidadeEstoque":10,"categoria":"Informática"}'
```

Resposta: **201 Created**, `Location: http://localhost:8080/api/produtos/1` e corpo (id/data ilustrativos):

```json
{"id":1,"nome":"Mouse","preco":49.90,"quantidadeEstoque":10,"categoria":"Informática","dataCriacao":"2026-11-11T12:00:00Z"}
```

### GET — listar e buscar

```sh
curl -i http://localhost:8080/api/produtos
curl -i 'http://localhost:8080/api/produtos?nome=mouse'
curl -i http://localhost:8080/api/produtos/1
```

As duas primeiras retornam **200** e uma lista:

```json
[{"id":1,"nome":"Mouse","preco":49.90,"quantidadeEstoque":10,"categoria":"Informática","dataCriacao":"2026-11-11T12:00:00Z"}]
```

A busca por ID retorna **200** e o objeto individual, no mesmo formato do POST. GET não exige corpo de requisição.

### PUT — substituir

```sh
curl -i -X PUT http://localhost:8080/api/produtos/1 -H 'Content-Type: application/json' -d '{"nome":"Mouse sem fio","preco":79.90,"quantidadeEstoque":5,"categoria":"Informática"}'
```

Resposta **200 OK**:

```json
{"id":1,"nome":"Mouse sem fio","preco":79.90,"quantidadeEstoque":5,"categoria":"Informática","dataCriacao":"2026-11-11T12:00:00Z"}
```

### DELETE — excluir

```sh
curl -i -X DELETE http://localhost:8080/api/produtos/1
```

Resposta **204 No Content**, sem corpo. Não exige corpo de requisição. Buscar, atualizar ou excluir esse ID novamente retorna **404**.

## Erros padronizados

As respostas usam Problem Detail, com `type`, `title`, `status`, `detail` e `instance`. Erros de validação acrescentam `campos`. Exemplo de requisição inválida:

```sh
curl -i -X POST http://localhost:8080/api/produtos -H 'Content-Type: application/json' -d '{"nome":"","preco":-1,"quantidadeEstoque":-1}'
```

Resposta **400 Bad Request** (mensagens padrão podem variar com o idioma do ambiente):

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"Dados inválidos","instance":"/api/produtos","campos":{"nome":"O nome é obrigatório","preco":"must be greater than or equal to 0.00","quantidadeEstoque":"must be greater than or equal to 0"}}
```

ID inexistente: **404**, `detail: "Produto não encontrado"`. Nome repetido: **409**, `detail: "Já existe um produto com esse nome"`. JSON malformado/campo desconhecido: **400**. ID textual como `/api/produtos/abc`: **400**. Para testar 409, envie o mesmo POST válido duas vezes antes de excluir o produto.

## Testes e organização

`mvn clean verify` executa os testes de integração por HTTP real com servidor em porta aleatória e H2 em memória separado do banco local. Os testes cobrem CRUD, filtro, Location, preservação da data, PUT completo, categoria opcional, validações, JSON inválido, ID inválido, 404 e 409. Relatórios: `target/surefire-reports/`.

- `controller`: rotas e códigos HTTP.
- `dto`: dados de entrada e Bean Validation.
- `service`: regras de negócio e transações.
- `repository`: persistência via JpaRepository.
- `model`: única entidade JPA.
- `exception`: respostas padronizadas de erro.

Para entregar, envie o código e este README em um repositório ou ZIP. Não inclua `.tools/`, `target/` ou `data/`; são ferramentas locais, saídas de compilação e dados gerados.

## Evidências incluídas

- `evidencias/testes.txt`: resultado dos cinco testes HTTP, sem falhas.
- `evidencias/http-cli.txt`: respostas reais de POST, GET, PUT, DELETE e GET após exclusão, obtidas com cURL contra o JAR executado no terminal na porta 18080. O uso dessa porta foi apenas para verificação; a porta padrão é 8080.
