package br.com.trabalho.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(name = "uk_categoria_nome", columnNames = "nome"))
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, columnDefinition = "VARCHAR_IGNORECASE(80)")
    private String nome;

    protected Categoria() {}
    public Categoria(String nome) { atualizar(nome); }
    public void atualizar(String nome) { this.nome = nome.strip(); }
    public Long getId() { return id; }
    public String getNome() { return nome; }
}
