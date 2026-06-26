package com.diogonogueira.gamestore.dtos.genre;

import java.util.UUID;

public record GenreResponse(UUID id,
                            String name) {
}