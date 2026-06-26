package com.diogonogueira.gamestore.dtos.game;

import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.dtos.publisher.PublisherResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record GameResponse(UUID id,
                           String name,
                           LocalDate releaseDate,
                           BigDecimal price,
                           PublisherResponse publisherResponse,
                           Set<GenreResponse> genreResponses) {
}