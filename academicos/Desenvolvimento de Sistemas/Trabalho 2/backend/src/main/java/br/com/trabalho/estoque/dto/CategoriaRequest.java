package br.com.trabalho.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank(message = "Informe o nome da categoria")
    @Size(max = 80, message = "Use no máximo 80 caracteres") String nome
) {}
