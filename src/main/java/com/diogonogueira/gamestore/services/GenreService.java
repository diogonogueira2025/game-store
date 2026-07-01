package com.diogonogueira.gamestore.services;

import com.diogonogueira.gamestore.dtos.genre.GenreRequest;
import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.entities.Genre;
import com.diogonogueira.gamestore.mappers.GenreMapper;
import com.diogonogueira.gamestore.repositories.GenreRepository;
import com.diogonogueira.gamestore.services.exceptions.BusinessRuleException;
import com.diogonogueira.gamestore.services.exceptions.DatabaseException;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public GenreService(GenreRepository genreRepository, GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }

    public Page<GenreResponse> findAll(Pageable pageable) {
        return genreRepository.findAll(pageable)
                .map(genreMapper::toResponse);
    }

    private Genre findEntityById(UUID id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public GenreResponse findById(UUID id) {
        Genre genre = findEntityById(id);
        return genreMapper.toResponse(genre);
    }

    @Transactional
    public GenreResponse save(GenreRequest genreRequest) {
        if (genreRepository.existsByName(genreRequest.name())) {
            throw new BusinessRuleException("Genre already exists with name: " + genreRequest.name());
        }
        Genre genre = genreMapper.toEntity(genreRequest);
        Genre savedGenre = genreRepository.save(genre);
        return genreMapper.toResponse(savedGenre);
    }

    @Transactional
    public GenreResponse update(UUID id, GenreRequest genreRequest) {
        Genre genre = findEntityById(id);

        if (!genre.getName().equals(genreRequest.name())
                && genreRepository.existsByName(genreRequest.name())) {
            throw new BusinessRuleException("Genre already exists with name: " + genreRequest.name());
        }

        genreMapper.updateEntityFromRequest(genreRequest, genre);
        return genreMapper.toResponse(genre);
    }

    @Transactional
    public void deleteById(UUID id) {
        findEntityById(id);

        try {
            genreRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Cannot delete genre because it has associated games");
        }
    }
}