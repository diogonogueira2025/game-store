package com.diogonogueira.gamestore.mappers;

import com.diogonogueira.gamestore.dtos.game.GameRequest;
import com.diogonogueira.gamestore.dtos.game.GameResponse;
import com.diogonogueira.gamestore.entities.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GameMapper {
    @Mapping(source = "publisher", target = "publisherResponse")
    @Mapping(source = "genres", target = "genreResponses")
    GameResponse toResponse(Game game);

    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "genres", ignore = true)
    Game toEntity(GameRequest gameRequest);

    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "genres", ignore = true)
    void updateEntityFromRequest(GameRequest gameRequest, @MappingTarget Game game);
}