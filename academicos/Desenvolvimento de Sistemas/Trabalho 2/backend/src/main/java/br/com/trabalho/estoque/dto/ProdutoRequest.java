package br.com.trabalho.estoque.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequest(
    @NotBlank(message = "Informe o nome do produto")
    @Size(max = 120, message = "Use no máximo 120 caracteres") String nome,
    @NotNull(message = "Informe o preço")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Use até 10 dígitos inteiros e 2 casas decimais") BigDecimal preco,
    @NotNull(message = "Informe a quantidade em estoque")
    @Min(value = 0, message = "A quantidade não pode ser negativa") Integer quantidadeEstoque,
    @NotNull(message = "Selecione uma categoria")
    @Positive(message = "Selecione uma categoria válida") Long categoriaId
) {}
