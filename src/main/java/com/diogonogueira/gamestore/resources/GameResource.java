package com.diogonogueira.gamestore.resources;

import com.diogonogueira.gamestore.dtos.game.GameRequest;
import com.diogonogueira.gamestore.dtos.game.GameResponse;
import com.diogonogueira.gamestore.services.GameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameResource {
    private final GameService gameService;

    public GameResource(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<Page<GameResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(gameService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GameResponse> save(@RequestBody @Valid GameRequest gameRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.save(gameRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponse> update(@PathVariable UUID id, @RequestBody @Valid GameRequest gameRequest) {
        return ResponseEntity.ok(gameService.update(id, gameRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        gameService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
