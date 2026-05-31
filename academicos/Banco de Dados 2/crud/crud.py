from database import get_connection

def inserir_jogador(nome: str):
    conn = get_connection()
    conn.execute("INSERT INTO jogadores (nome) VALUES (?)", (nome,))
    conn.commit()
    conn.close()
    print(f"\nJogador '{nome}' cadastrado com sucesso!")

def listar_jogadores():
    conn = get_connection()
    rows = conn.execute("SELECT * FROM jogadores ORDER BY id").fetchall()
    conn.close()
    if not rows:
        print("\nNenhum jogador cadastrado ainda.")
        return
    print("\n{:<5} {:<30}".format("ID", "NOME"))
    print("-" * 35)
    for row in rows:
        print("{:<5} {:<30}".format(row["id"], row["nome"]))

def buscar_jogador(id: int):
    conn = get_connection()
    row = conn.execute("SELECT * FROM jogadores WHERE id = ?", (id,)).fetchone()
    conn.close()
    if row:
        print(f"\nEncontrado -> ID: {row['id']} | Nome: {row['nome']}")
    else:
        print(f"\nJogador com ID {id} não encontrado.")

def atualizar_jogador(id: int, novo_nome: str):
    conn = get_connection()
    cursor = conn.execute(
        "UPDATE jogadores SET nome = ? WHERE id = ?", (novo_nome, id)
    )
    conn.commit()
    conn.close()
    if cursor.rowcount:
        print(f"\nJogador ID {id} atualizado para '{novo_nome}'.")
    else:
        print(f"\nJogador com ID {id} não encontrado.")

def deletar_jogador(id: int):
    conn = get_connection()
    cursor = conn.execute("DELETE FROM jogadores WHERE id = ?", (id,))
    conn.commit()
    conn.close()
    if cursor.rowcount:
        print(f"\nJogador ID {id} removido com sucesso.")
    else:
        print(f"\nJogador com ID {id} não encontrado.")