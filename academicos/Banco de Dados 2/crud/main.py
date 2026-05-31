import os
from database import criar_tabela
from crud import (
    inserir_jogador,
    listar_jogadores,
    buscar_jogador,
    atualizar_jogador,
    deletar_jogador,
)

def limpar_tela():
    os.system("cls" if os.name == "nt" else "clear")

def pausar():
    input("\nPressione Enter para voltar ao menu...")

def mostrar_menu():
    print("╔══════════════════════════════════╗")
    print("║   CRUD - Jogo de Truco (Python)  ║")
    print("║        Banco de Dados SQLite     ║")
    print("╚══════════════════════════════════╝")
    print("\n┌─────────────────────────┐")
    print("│         MENU            │")
    print("├─────────────────────────┤")
    print("│  1. Inserir jogador     │")
    print("│  2. Listar jogadores    │")
    print("│  3. Buscar por ID       │")
    print("│  4. Atualizar jogador   │")
    print("│  5. Deletar jogador     │")
    print("│  0. Sair                │")
    print("└─────────────────────────┘")

def menu():
    criar_tabela()

    while True:
        limpar_tela()
        mostrar_menu()
        opcao = input("Escolha uma opção: ").strip()

        limpar_tela()

        if opcao == "1":
            print("=== INSERIR JOGADOR ===")
            nome = input("Nome do jogador: ").strip()
            if nome:
                inserir_jogador(nome)
            else:
                print("Nome não pode ser vazio.")
            pausar()

        elif opcao == "2":
            print("=== LISTAR JOGADORES ===")
            listar_jogadores()
            pausar()

        elif opcao == "3":
            print("=== BUSCAR JOGADOR ===")
            try:
                id_ = int(input("ID do jogador: "))
                buscar_jogador(id_)
            except ValueError:
                print("ID inválido. Digite um número.")
            pausar()

        elif opcao == "4":
            print("=== ATUALIZAR JOGADOR ===")
            try:
                id_ = int(input("ID do jogador a atualizar: "))
                novo = input("Novo nome: ").strip()
                if novo:
                    atualizar_jogador(id_, novo)
                else:
                    print("Nome não pode ser vazio.")
            except ValueError:
                print("ID inválido. Digite um número.")
            pausar()

        elif opcao == "5":
            print("=== DELETAR JOGADOR ===")
            try:
                id_ = int(input("ID do jogador a deletar: "))
                confirma = input(f"Confirmar exclusão do ID {id_}? (s/n): ").strip().lower()
                if confirma == "s":
                    deletar_jogador(id_)
                else:
                    print("Operação cancelada.")
            except ValueError:
                print("ID inválido. Digite um número.")
            pausar()

        elif opcao == "0":
            print("Encerrando...2")
            break

        else:
            print("Opção inválida. Tente novamente.")
            pausar()

if __name__ == "__main__":
    menu()