package com.diogonogueira.gamestore.dtos.game;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record GameRequest(@NotBlank String name,
                          @NotNull LocalDate releaseDate,
                          @NotNull @PositiveOrZero BigDecimal price,
                          @NotNull UUID publisherId,
                          @NotEmpty Set<UUID> genreIds) {
}