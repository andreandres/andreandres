def exibir_menu():
    """Exibe as opções disponíveis no sistema."""
    print("\n====================================")
    print("          TASKTRACKER")
    print("====================================")
    print("1 - Cadastrar nova tarefa")
    print("2 - Visualizar tarefas cadastradas")
    print("3 - Sair")
    print("====================================")


def obter_titulo_valido():
    """Solicita um título até que ele tenha algum conteúdo."""
    while True:
        titulo = input("Digite o título da tarefa: ").strip()
        if titulo:
            return titulo
        print("Erro: o título não pode ficar vazio.")


def obter_prioridade_valida():
    """Solicita e padroniza a prioridade da tarefa."""
    prioridades = {
        "alta": "Alta",
        "média": "Média",
        "media": "Media",
        "baixa": "Baixa",
    }

    while True:
        prioridade_informada = input(
            "Digite a prioridade (Alta, Média ou Baixa): "
        ).strip().lower()

        if prioridade_informada in prioridades:
            return prioridades[prioridade_informada]

        print("Erro: prioridade inválida. Escolha Alta, Média ou Baixa.")


def cadastrar_tarefa(tarefas):
    """Cria uma tarefa e adiciona o dicionário à lista recebida."""
    print("\n--- Cadastro de nova tarefa ---")
    titulo = obter_titulo_valido()
    descricao = input("Digite a descrição da tarefa: ").strip()
    prioridade = obter_prioridade_valida()
    data_limite = input(
        "Digite a data limite (formato recomendado: DD/MM/AAAA): "
    ).strip()

    tarefa = {
        "titulo": titulo,
        "descricao": descricao,
        "prioridade": prioridade,
        "data_limite": data_limite,
        "status": "Pendente",
    }

    tarefas.append(tarefa)
    print("Tarefa cadastrada com sucesso!")


def visualizar_tarefas(tarefas):
    """Exibe todas as tarefas sem alterar a lista."""
    if not tarefas:
        print("Nenhuma tarefa cadastrada no momento.")
        return

    print("\n========== TAREFAS CADASTRADAS ==========")
    for indice, tarefa in enumerate(tarefas, start=1):
        print(f"\n--- Tarefa {indice} ---")
        print(f"Título: {tarefa['titulo']}")
        print(f"Descrição: {tarefa['descricao']}")
        print(f"Prioridade: {tarefa['prioridade']}")
        print(f"Data limite: {tarefa['data_limite']}")
        print(f"Status: {tarefa['status']}")
        print("-----------------------------------------")


def main():
    """Executa o menu principal enquanto o usuário não escolher sair."""
    tarefas = []

    while True:
        exibir_menu()
        opcao = input("Escolha uma opção: ").strip()

        if opcao == "1":
            cadastrar_tarefa(tarefas)
        elif opcao == "2":
            visualizar_tarefas(tarefas)
        elif opcao == "3":
            print("Encerrando o TaskTracker. Até mais!")
            break
        else:
            print("Opção inválida. Digite 1, 2 ou 3.")


if __name__ == "__main__":
    main()
