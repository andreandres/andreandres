package br.com.trabalho.estoque.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "produtos", uniqueConstraints = @UniqueConstraint(name = "uk_produto_nome", columnNames = "nome"))
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120, columnDefinition = "VARCHAR_IGNORECASE(120)")
    private String nome;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Integer quantidadeEstoque;
    @Column(length = 80)
    private String categoria;
    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;

    protected Produto() {}

    public Produto(String nome, BigDecimal preco, Integer quantidadeEstoque, String categoria) {
        atualizar(nome, preco, quantidadeEstoque, categoria);
    }

    @PrePersist
    void registrarCriacao() { dataCriacao = Instant.now().truncatedTo(ChronoUnit.MICROS); }

    public void atualizar(String nome, BigDecimal preco, Integer quantidadeEstoque, String categoria) {
        this.nome = nome.strip();
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.categoria = categoria == null ? null : categoria.strip();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public String getCategoria() { return categoria; }
    public Instant getDataCriacao() { return dataCriacao; }
}
