import sqlite3

DB_NAME = "truco.db"

def get_connection():
    conn = sqlite3.connect(DB_NAME)
    conn.row_factory = sqlite3.Row
    return conn

def criar_tabela():
    conn = get_connection()
    conn.execute("""
        CREATE TABLE IF NOT EXISTS jogadores (
            id   INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT    NOT NULL
        )
    """)
    conn.commit()
    conn.close()