# TaskTracker

## Dados do aluno

- **Nome:** André Joaquim Soares de Andrade
- **RA:** 22552746
- **Curso:** Ciência da Computação
- **Disciplina:** Bootcamp II

## Descrição do projeto

O TaskTracker é uma aplicação simples de linha de comando (CLI) para cadastrar e visualizar tarefas durante a execução do programa. As tarefas ficam armazenadas em uma lista de dicionários enquanto o programa está aberto.

O projeto foi desenvolvido como parte da **Fase 2 - Etapa Intermediária do Bootcamp II**.

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

O projeto não possui persistência em arquivo, banco de dados, login, edição ou exclusão de tarefas, pois esses recursos não fazem parte desta fase.

## Tecnologias utilizadas

- Python 3
- Git
- GitHub

Não são necessárias bibliotecas externas nem instalação com pip.

## Estrutura de diretórios

```text
tasktracker-projeto/
├── README.md
├── .gitignore
├── docs/
│   └── planejamento_logico.pdf
└── src/
    └── main.py
```

## Como executar

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
