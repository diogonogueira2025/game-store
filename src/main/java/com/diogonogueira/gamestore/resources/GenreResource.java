package com.diogonogueira.gamestore.resources;

import com.diogonogueira.gamestore.dtos.genre.GenreRequest;
import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.services.GenreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/genres")
public class GenreResource {
    private final GenreService genreService;

    public GenreResource(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<Page<GenreResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(genreService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(genreService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GenreResponse> save(@RequestBody @Valid GenreRequest genreRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.save(genreRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> update(@PathVariable UUID id, @RequestBody @Valid GenreRequest genreRequest) {
        return ResponseEntity.ok(genreService.update(id, genreRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        genreService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}