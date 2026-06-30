package com.diogonogueira.gamestore.services;

import com.diogonogueira.gamestore.dtos.genre.GenreRequest;
import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.entities.Game;
import com.diogonogueira.gamestore.entities.Genre;
import com.diogonogueira.gamestore.mappers.GenreMapper;
import com.diogonogueira.gamestore.repositories.GenreRepository;
import com.diogonogueira.gamestore.services.exceptions.DatabaseException;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {
    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private GenreService genreService;

    @Test
    void shouldReturnGenreWhenGenreExists() {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre(id, "FPS");
        GenreResponse genreResponse = new GenreResponse(id, "FPS");

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(genre));
        when(genreMapper.toResponse(genre))
                .thenReturn(genreResponse);

        GenreResponse result = genreService.findById(id);
        assertEquals(genreResponse, result);

        verify(genreRepository).findById(id);
        verify(genreMapper).toResponse(genre);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGenreDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(genreRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.findById(id));

        verify(genreRepository).findById(id);
        verify(genreMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnPageOfGenreResponsesWhenGenresExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        UUID fpsID = UUID.randomUUID();
        UUID rpgID = UUID.randomUUID();

        Genre fps = new Genre(fpsID, "FPS");
        Genre rpg = new Genre(rpgID, "RPG");

        GenreResponse fpsResponse = new GenreResponse(fpsID, "FPS");
        GenreResponse rpgResponse = new GenreResponse(rpgID, "RPG");

        Page<Genre> genrePage = new PageImpl<>(List.of(fps, rpg), pageable, 2);

        when(genreRepository.findAll(pageable))
                .thenReturn(genrePage);
        when(genreMapper.toResponse(fps))
                .thenReturn(fpsResponse);
        when(genreMapper.toResponse(rpg))
                .thenReturn(rpgResponse);

        Page<GenreResponse> result = genreService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(fpsResponse, rpgResponse), result.getContent());

        verify(genreRepository).findAll(pageable);
        verify(genreMapper).toResponse(fps);
        verify(genreMapper).toResponse(rpg);
    }

    @Test
    void shouldSaveGenreAndReturnGenreResponse() {
        UUID id = UUID.randomUUID();
        GenreRequest genreRequest = new GenreRequest("MMORPG");
        Genre genre = new Genre(null, "MMORPG");
        Genre savedGenre = new Genre(id, "MMORPG");
        GenreResponse genreResponse = new GenreResponse(id, "MMORPG");

        when(genreMapper.toEntity(genreRequest))
                .thenReturn(genre);
        when(genreRepository.save(genre))
                .thenReturn(savedGenre);
        when(genreMapper.toResponse(savedGenre))
                .thenReturn(genreResponse);

        GenreResponse result = genreService.save(genreRequest);

        assertEquals(genreResponse, result);

        verify(genreMapper).toEntity(genreRequest);
        verify(genreRepository).save(genre);
        verify(genreMapper).toResponse(savedGenre);
    }

    @Test
    void shouldUpdateGenreAndReturnGenreResponse() {
        UUID id = UUID.randomUUID();
        GenreRequest genreRequest = new GenreRequest("Action");
        Genre genre = new Genre(id, "FPS");
        GenreResponse genreResponse = new GenreResponse(id, "Action");

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(genre));
        when(genreMapper.toResponse(genre))
                .thenReturn(genreResponse);

        GenreResponse result = genreService.update(id, genreRequest);
        assertEquals(genreResponse, result);

        verify(genreRepository).findById(id);
        verify(genreMapper).updateEntityFromRequest(genreRequest, genre);
        verify(genreMapper).toResponse(genre);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingGenreDoesNotExist() {
        UUID id = UUID.randomUUID();
        GenreRequest genreRequest = new GenreRequest("Action");
        when(genreRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.update(id, genreRequest));

        verify(genreRepository).findById(id);
        verify(genreMapper, never()).updateEntityFromRequest(any(), any());
        verify(genreMapper, never()).toResponse(any());
    }

    @Test
    void shouldDeleteGenreWhenGenreHasNoAssociatedGames() {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre(id, "Soulslike");

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(genre));

        genreService.deleteById(id);

        verify(genreRepository).findById(id);
        verify(genreRepository).delete(genre);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingGenreDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(genreRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.deleteById(id));

        verify(genreRepository).findById(id);
        verify(genreRepository, never()).delete(any());
    }

    @Test
    void shouldThrowDatabaseExceptionWhenDeletingGenreWithAssociatedGames() {
        UUID id = UUID.randomUUID();
        Genre genre = new Genre(id, "Multiplayer");
        Game game = new Game();
        genre.getGames().add(game);

        when(genreRepository.findById(id))
                .thenReturn(Optional.of(genre));

        assertThrows(DatabaseException.class, () -> genreService.deleteById(id));
        
        verify(genreRepository).findById(id);
        verify(genreRepository, never()).delete(any());
    }
}