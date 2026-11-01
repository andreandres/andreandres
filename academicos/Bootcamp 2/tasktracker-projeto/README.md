# TaskTracker

## Dados do aluno

- **Nome:** André Joaquim Soares de Andrade
- **Curso:** Ciência da Computação
- **Disciplina:** Bootcamp II

## Descrição do projeto

O TaskTracker é uma aplicação simples de linha de comando (CLI) para cadastrar e visualizar tarefas durante a execução do programa. As tarefas ficam armazenadas em uma lista de dicionários enquanto o programa está aberto.

O projeto foi desenvolvido como parte da **Fase 2 - Etapa Intermediária do Bootcamp II** e preparado para a **Fase 3 - Entrega Final**, com conteinerização em Docker.

## Funcionalidades implementadas

- Exibição contínua do menu principal.
- Cadastro de tarefas com título, descrição, prioridade e data limite.
- Validação de título obrigatório.
- Validação e padronização das prioridades Alta, Média e Baixa.
- Status definido automaticamente como Pendente.
- Visualização organizada de todas as tarefas cadastradas.
- Mensagem para lista vazia.
- Tratamento de opções inválidas do menu.
- Encerramento educado do programa.

As tarefas são mantidas em memória e são perdidas quando o programa é encerrado.

O projeto não possui persistência em arquivo, banco de dados, login, edição ou exclusão de tarefas, pois esses recursos não fazem parte desta fase.

## Tecnologias utilizadas

- Python 3
- Git
- GitHub
- Docker
- Docker Compose

Não são necessárias bibliotecas externas nem instalação com pip.

## Estrutura de diretórios

```text
tasktracker-projeto/
├── README.md
├── .gitignore
├── .dockerignore
├── docs/
│   └── planejamento_logico.pdf
├── Dockerfile
├── docker-compose.yml
└── src/
    └── main.py
```

## Execução local com Python

1. Abra a pasta `tasktracker-projeto` no VS Code.
2. Confirme que o Python 3 está instalado:

   ```bash
   python --version
   ```

3. Abra o terminal integrado do VS Code em **Terminal > New Terminal**.
4. Execute o programa na raiz do projeto:

   ```bash
   python src/main.py
   ```

5. Use as opções do menu para cadastrar, visualizar ou encerrar o programa.

## Execução com Docker

### Pré-requisitos

É necessário ter o mecanismo do Docker em execução. No Windows, o Docker Desktop deve estar instalado e configurado para utilizar contêineres Linux.

Verifique as versões instaladas:

```bash
docker --version
docker compose version
```

Construa a imagem:

```bash
docker build -t tasktracker:latest .
```

Execute diretamente:

```bash
docker run --rm -it tasktracker:latest
```

Execute pelo Docker Compose:

```bash
docker compose run --build --rm tasktracker
```

Valide o arquivo Compose sem iniciar o programa:

```bash
docker compose config
```

O comando recomendado para este projeto é `docker compose run`, porque o TaskTracker é interativo e utiliza `input()` para receber dados pelo terminal. O comando `docker compose up` não é recomendado para a interação com o menu neste projeto.

Uma **imagem** é o pacote imutável usado como base para executar a aplicação. Um **contêiner** é uma execução isolada dessa imagem.

O projeto utiliza `python:3.11-slim` por fornecer Python 3.11 em uma imagem menor, adequada a uma aplicação que usa somente a biblioteca padrão. As opções `-i` e `-t` do `docker run` mantêm a entrada interativa e disponibilizam um terminal. No Compose, `stdin_open: true` mantém a entrada padrão aberta e `tty: true` cria um terminal para a interação.

A opção `--rm` remove o contêiner quando o programa termina, sem apagar a imagem.

## Validações existentes

- Título vazio ou formado apenas por espaços é rejeitado.
- A prioridade aceita `Alta`, `Média`, `Media` ou `Baixa`, sem diferenciar maiúsculas e minúsculas.
- A prioridade é salva padronizada como `Alta`, `Média` ou `Baixa`.
- Uma opção diferente de 1, 2 ou 3 exibe uma mensagem de erro e retorna ao menu.
- A data limite é texto livre; o formato recomendado é `DD/MM/AAAA`.

## Roteiro de demonstração no terminal

1. Executar o programa.
2. Tentar visualizar tarefas antes de cadastrar qualquer uma e mostrar a mensagem de lista vazia.
3. Escolher cadastrar nova tarefa.
4. Digitar um título vazio para provar que a validação funciona.
5. Digitar um título válido.
6. Digitar uma prioridade inválida, como `Urgente`, para provar que a validação funciona.
7. Digitar uma prioridade válida, como `Alta`.
8. Finalizar o cadastro.
9. Escolher visualizar tarefas e mostrar todos os dados cadastrados.
10. Escolher uma opção inválida do menu, como `9`, e mostrar a mensagem de erro.
11. Escolher a opção 3 para encerrar.

Exemplo de sequência de entradas:

```text
2
1

Estudar Python
Revisar funções e listas
Urgente
Alta
30/10/2026
2
9
3
```

## Planejamento lógico

O arquivo [docs/planejamento_logico.pdf](docs/planejamento_logico.pdf) registra, de forma resumida, os dados, as funções e o fluxo principal da aplicação.
