package com.diogonogueira.gamestore.services;

import com.diogonogueira.gamestore.dtos.game.GameRequest;
import com.diogonogueira.gamestore.dtos.game.GameResponse;
import com.diogonogueira.gamestore.entities.Game;
import com.diogonogueira.gamestore.entities.Genre;
import com.diogonogueira.gamestore.entities.Publisher;
import com.diogonogueira.gamestore.mappers.GameMapper;
import com.diogonogueira.gamestore.repositories.GameRepository;
import com.diogonogueira.gamestore.repositories.GenreRepository;
import com.diogonogueira.gamestore.repositories.PublisherRepository;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GameService {
    private final GameRepository gameRepository;
    private final PublisherRepository publisherRepository;
    private final GenreRepository genreRepository;
    private final GameMapper gameMapper;

    public GameService(GameMapper gameMapper, GameRepository gameRepository, PublisherRepository publisherRepository, GenreRepository genreRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        this.publisherRepository = publisherRepository;
        this.genreRepository = genreRepository;
    }

    public Page<GameResponse> findAll(Pageable pageable) {
        return gameRepository.findAll(pageable)
                .map(gameMapper::toResponse);
    }

    private Game findEntityById(UUID id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public GameResponse findById(UUID id) {
        return gameMapper.toResponse(findEntityById(id));
    }

    private void assignRelations(Game game, GameRequest gameRequest) {
        Publisher publisher = publisherRepository
                .findById(gameRequest.publisherId())
                .orElseThrow(() -> new ResourceNotFoundException(gameRequest.publisherId()));
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(gameRequest.genreIds()));
        if (genres.size() != gameRequest.genreIds().size()) {
            throw new ResourceNotFoundException("One or more genres not found");
        }
        game.setPublisher(publisher);
        game.setGenres(genres);
    }

    @Transactional
    public GameResponse save(GameRequest gameRequest) {
        Game game = gameMapper.toEntity(gameRequest);
        assignRelations(game, gameRequest);
        Game savedGame = gameRepository.save(game);
        return gameMapper.toResponse(savedGame);
    }

    @Transactional
    public GameResponse update(UUID id, GameRequest gameRequest) {
        Game game = findEntityById(id);
        gameMapper.updateEntityFromRequest(gameRequest, game);
        assignRelations(game, gameRequest);
        return gameMapper.toResponse(game);
    }

    @Transactional
    public void deleteById(UUID id) {
        Game game = findEntityById(id);
        gameRepository.delete(game);
    }
}