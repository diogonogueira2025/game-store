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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    @Mock
    private GameRepository gameRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameService gameService;

    @Test
    void shouldReturnGameWhenGameExists() {
        UUID id = UUID.randomUUID();
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 6, 12),
                BigDecimal.valueOf(98.90),
                null);
        GameResponse gameResponse = new GameResponse(
                id,
                "Dark Souls",
                LocalDate.of(2010, 6, 12),
                BigDecimal.valueOf(98.90),
                null,
                null);
        when(gameRepository.findById(id)).thenReturn(Optional.of(game));
        when(gameMapper.toResponse(game)).thenReturn(gameResponse);

        GameResponse result = gameService.findById(id);

        assertEquals(gameResponse, result);
        verify(gameRepository).findById(id);
        verify(gameMapper).toResponse(game);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGameDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(gameRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.findById(id));

        verify(gameRepository).findById(id);
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnPageOfGameResponsesWhenGamesExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        UUID darkSoulsId = UUID.randomUUID();
        UUID superMarioId = UUID.randomUUID();

        Game darkSouls = new Game(darkSoulsId, "Dark Souls", LocalDate.of(2010, 12, 10), BigDecimal.valueOf(35.60), null);
        Game superMario = new Game(superMarioId, "Super Mario", LocalDate.of(2000, 2, 9), BigDecimal.valueOf(200.9), null);

        GameResponse darkSoulsResponse = new GameResponse(darkSoulsId, "Dark Souls", LocalDate.of(2010, 12, 10), BigDecimal.valueOf(35.60), null, null);
        GameResponse superMarioResponse = new GameResponse(superMarioId, "Super Mario", LocalDate.of(2000, 2, 9), BigDecimal.valueOf(200.9), null, null);

        Page<Game> gamePage = new PageImpl<>(List.of(darkSouls, superMario), pageable, 2);

        when(gameRepository.findAll(pageable))
                .thenReturn(gamePage);
        when(gameMapper.toResponse(darkSouls))
                .thenReturn(darkSoulsResponse);
        when(gameMapper.toResponse(superMario))
                .thenReturn(superMarioResponse);

        Page<GameResponse> result = gameService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(darkSoulsResponse, superMarioResponse), result.getContent());

        verify(gameRepository).findAll(pageable);
        verify(gameMapper).toResponse(darkSouls);
        verify(gameMapper).toResponse(superMario);
    }

    @Test
    void shouldSaveGameAndReturnGameResponse() {
        UUID id = UUID.randomUUID();
        UUID publisherID = UUID.randomUUID();
        Publisher publisher = new Publisher(publisherID, "Bandai");

        UUID fpsId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Set<UUID> genreIds = new HashSet<>(List.of(fpsId, actionId));
        Genre fps = new Genre(fpsId, "FPS");
        Genre action = new Genre(actionId, "Action");

        List<Genre> genresList = List.of(fps, action);
        GameRequest gameRequest = new GameRequest("Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                publisherID,
                genreIds);
        Game game = new Game(
                null,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);
        Game savedGame = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                publisher
        );
        GameResponse gameResponse = new GameResponse(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null,
                null
        );

        when(gameMapper.toEntity(gameRequest))
                .thenReturn(game);
        when(publisherRepository.findById(publisherID))
                .thenReturn(Optional.of(publisher));
        when(genreRepository.findAllById(genreIds))
                .thenReturn(genresList);
        when(gameRepository.save(game))
                .thenReturn(savedGame);
        when(gameMapper.toResponse(savedGame))
                .thenReturn(gameResponse);

        GameResponse result = gameService.save(gameRequest);

        assertEquals(gameResponse, result);

        verify(gameMapper).toEntity(gameRequest);
        verify(publisherRepository).findById(publisherID);
        verify(genreRepository).findAllById(genreIds);
        verify(gameRepository).save(game);
        verify(gameMapper).toResponse(savedGame);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingGameWithPublisherDoesNotExist() {
        UUID id = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        GameRequest gameRequest = new GameRequest(
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                publisherId,
                null);
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);

        when(gameMapper.toEntity(gameRequest))
                .thenReturn(game);
        when(publisherRepository.findById(publisherId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.save(gameRequest));

        verify(gameMapper).toEntity(gameRequest);
        verify(publisherRepository).findById(publisherId);
        verify(genreRepository, never()).findAllById(any());
        verify(gameRepository, never()).save(game);
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingGameWithOneOrMoreGenresDoNotExist() {
        UUID id = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        Publisher publisher = new Publisher(publisherId, "Bandai");
        UUID fpsId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Set<UUID> genreIds = new HashSet<>(List.of(fpsId, actionId));

        GameRequest gameRequest = new GameRequest(
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                publisherId,
                genreIds);
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);

        when(gameMapper.toEntity(gameRequest))
                .thenReturn(game);
        when(publisherRepository.findById(publisherId))
                .thenReturn(Optional.of(publisher));
        when(genreRepository.findAllById(genreIds))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> gameService.save(gameRequest));

        verify(gameMapper).toEntity(gameRequest);
        verify(publisherRepository).findById(publisherId);
        verify(genreRepository).findAllById(genreIds);
        verify(gameRepository, never()).save(game);
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldUpdateGameAndReturnGameResponse() {
        UUID id = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        Publisher publisher = new Publisher(publisherId, "Bandai");
        UUID fpsId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Set<UUID> genreIds = new HashSet<>(List.of(fpsId, actionId));
        Genre fps = new Genre(fpsId, "FPS");
        Genre action = new Genre(actionId, "Action");
        List<Genre> genresList = List.of(fps, action);

        GameRequest gameRequest = new GameRequest(
                "Super Mario",
                LocalDate.of(2002, 1, 20),
                BigDecimal.valueOf(145.90),
                publisherId,
                genreIds);
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);
        GameResponse gameResponse = new GameResponse(
                id,
                "Super Mario",
                LocalDate.of(2002, 1, 20),
                BigDecimal.valueOf(145.90),
                null,
                null);

        when(gameRepository.findById(id))
                .thenReturn(Optional.of(game));
        when(publisherRepository.findById(publisherId))
                .thenReturn(Optional.of(publisher));
        when(genreRepository.findAllById(genreIds))
                .thenReturn(genresList);
        when(gameMapper.toResponse(game))
                .thenReturn(gameResponse);

        GameResponse result = gameService.update(id, gameRequest);

        assertEquals(gameResponse, result);

        verify(gameRepository).findById(id);
        verify(gameMapper).updateEntityFromRequest(gameRequest, game);
        verify(publisherRepository).findById(publisherId);
        verify(genreRepository).findAllById(genreIds);
        verify(gameMapper).toResponse(game);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingGameDoesNotExist() {
        UUID id = UUID.randomUUID();
        GameRequest gameRequest = new GameRequest(
                "Super Mario",
                LocalDate.of(2002, 1, 20),
                BigDecimal.valueOf(145.90),
                null,
                null);
        when(gameRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.update(id, gameRequest));

        verify(gameRepository).findById(id);
        verify(gameMapper, never()).updateEntityFromRequest(any(), any());
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingGameWithPublisherDoesNotExist() {
        UUID id = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();

        GameRequest gameRequest = new GameRequest(
                "Super Mario",
                LocalDate.of(2002, 1, 20),
                BigDecimal.valueOf(145.90),
                publisherId,
                null);
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);

        when(gameRepository.findById(id))
                .thenReturn(Optional.of(game));
        when(publisherRepository.findById(publisherId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.update(id, gameRequest));

        verify(gameRepository).findById(id);
        verify(gameMapper).updateEntityFromRequest(gameRequest, game);
        verify(genreRepository, never()).findAllById(any());
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingGameWithOneOrMoreGenresDoNotExist() {
        UUID id = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        Publisher publisher = new Publisher(publisherId, "Nintendo");
        UUID fpsId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Set<UUID> genreIds = new HashSet<>(List.of(fpsId, actionId));

        GameRequest gameRequest = new GameRequest(
                "Super Mario",
                LocalDate.of(2002, 1, 20),
                BigDecimal.valueOf(145.90),
                publisherId,
                genreIds);
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);

        when(gameRepository.findById(id))
                .thenReturn(Optional.of(game));
        when(publisherRepository.findById(publisherId))
                .thenReturn(Optional.of(publisher));
        when(genreRepository.findAllById(genreIds))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> gameService.update(id, gameRequest));

        verify(gameRepository).findById(id);
        verify(gameMapper).updateEntityFromRequest(gameRequest, game);
        verify(genreRepository).findAllById(genreIds);
        verify(gameMapper, never()).toResponse(any());
    }

    @Test
    void shouldDeleteGameWhenGameExists() {
        UUID id = UUID.randomUUID();
        Game game = new Game(
                id,
                "Dark Souls",
                LocalDate.of(2010, 10, 12),
                BigDecimal.valueOf(210.90),
                null);

        when(gameRepository.findById(id))
                .thenReturn(Optional.of(game));

        gameService.deleteById(id);

        verify(gameRepository).findById(id);
        verify(gameRepository).delete(game);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingGameDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(gameRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.deleteById(id));

        verify(gameRepository).findById(id);
        verify(gameRepository, never()).delete(any());
    }
}