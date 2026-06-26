package com.diogonogueira.gamestore.mappers;

import com.diogonogueira.gamestore.dtos.genre.GenreRequest;
import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.entities.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreResponse toResponse(Genre genre);
    Genre toEntity(GenreRequest genreRequest);
    void updateEntityFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}
