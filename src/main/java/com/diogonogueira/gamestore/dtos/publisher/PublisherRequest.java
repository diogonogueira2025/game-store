package com.diogonogueira.gamestore.dtos.publisher;

import jakarta.validation.constraints.NotBlank;

public record PublisherRequest(@NotBlank String name) {
}
