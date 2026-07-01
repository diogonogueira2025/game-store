package com.diogonogueira.gamestore.resources;

import com.diogonogueira.gamestore.dtos.genre.GenreRequest;
import com.diogonogueira.gamestore.dtos.genre.GenreResponse;
import com.diogonogueira.gamestore.resources.exceptions.ResourceExceptionHandler;
import com.diogonogueira.gamestore.services.GenreService;
import com.diogonogueira.gamestore.services.exceptions.BusinessRuleException;
import com.diogonogueira.gamestore.services.exceptions.DatabaseException;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({GenreResource.class, ResourceExceptionHandler.class})
class GenreResourceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @Test
    void shouldReturnStatus200AndGenreResponseWhenIdExists() throws Exception {
        UUID id = UUID.randomUUID();
        GenreResponse genreResponse = new GenreResponse(id, "FPS");

        when(genreService.findById(id))
                .thenReturn(genreResponse);

        mockMvc.perform(get("/genres/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("FPS"));
    }

    @Test
    void shouldReturnStatus404WhenIdDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        when(genreService.findById(id))
                .thenThrow(new ResourceNotFoundException(id));

        mockMvc.perform(get("/genres/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Resource not found. Id: " + id))
                .andExpect(jsonPath("$.path").value("/genres/" + id));
    }

    @Test
    void shouldReturnStatus400WhenIdFormatIsInvalid() throws Exception {
        String invalidId = "id-invalid-123";

        mockMvc.perform(get("/genres/" + invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatus200AndGenrePageWhenFindAllIsCalled() throws Exception {
        UUID id = UUID.randomUUID();
        GenreResponse genreResponse = new GenreResponse(id, "FPS");

        List<GenreResponse> list = List.of(genreResponse);

        Page<GenreResponse> page = new PageImpl<>(list);

        when(genreService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].name").value("FPS"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnStatus200AndEmptyPageWhenNoGenresExist() throws Exception {
        Page<GenreResponse> page = Page.empty();

        when(genreService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnStatus201AndGenreResponseWhenRequestIsValidOnSave() throws Exception {
        UUID id = UUID.randomUUID();
        GenreResponse genreResponse = new GenreResponse(id, "FPS");

        when(genreService.save(any(GenreRequest.class)))
                .thenReturn(genreResponse);

        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("FPS"));
    }

    @Test
    void shouldReturnStatus400WhenRequestIsInvalidOnSave() throws Exception {
        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name":""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatus422WhenGenreNameAlreadyExistsOnSave() throws Exception {
        when(genreService.save(any(GenreRequest.class)))
                .thenThrow(new BusinessRuleException("Genre already exists with name: FPS"));

        mockMvc.perform(post("/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule error"))
                .andExpect(jsonPath("$.message").value("Genre already exists with name: FPS"))
                .andExpect(jsonPath("$.path").value("/genres"));
    }

    @Test
    void shouldReturnStatus200AndGenreResponseWhenUpdateIsSuccessful() throws Exception {
        UUID id = UUID.randomUUID();
        GenreResponse genreResponse = new GenreResponse(id, "FPS");

        when(genreService.update(eq(id), any(GenreRequest.class)))
                .thenReturn(genreResponse);

        mockMvc.perform(put("/genres/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("FPS"));
    }

    @Test
    void shouldReturnStatus404WhenIdDoesNotExistOnUpdate() throws Exception {
        UUID id = UUID.randomUUID();

        when(genreService.update(eq(id), any(GenreRequest.class)))
                .thenThrow(new ResourceNotFoundException(id));

        mockMvc.perform(put("/genres/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("Resource not found. Id: " + id))
                .andExpect(jsonPath("$.path").value("/genres/" + id));
    }

    @Test
    void shouldReturnStatus400WhenIdFormatIsInvalidOnUpdate() throws Exception {
        String invalidId = "invalid-id-123";

        mockMvc.perform(put("/genres/" + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(genreService, never()).update(any(), any());
    }

    @Test
    void shouldReturnStatus400WhenRequestBodyIsInvalidOnUpdate() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/genres/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
        verify(genreService, never()).update(any(), any());
    }

    @Test
    void shouldReturnStatus422WhenGenreNameAlreadyExistsOnUpdate() throws Exception {
        UUID id = UUID.randomUUID();

        when(genreService.update(eq(id), any(GenreRequest.class)))
                .thenThrow(new BusinessRuleException("Genre already exists with name: FPS"));

        mockMvc.perform(put("/genres/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "FPS"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule error"))
                .andExpect(jsonPath("$.message").value("Genre already exists with name: FPS"))
                .andExpect(jsonPath("$.path").value("/genres/" + id));
    }

    @Test
    void shouldReturnStatus204WhenDeleteIsSuccessful() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/genres/" + id))
                .andExpect(status().isNoContent());

        verify(genreService).deleteById(id);
    }

    @Test
    void shouldReturnStatus404WhenIdDoesNotExistOnDelete() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new ResourceNotFoundException(id))
                .when(genreService).deleteById(id);

        mockMvc.perform(delete("/genres/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found. Id: " + id));

        verify(genreService).deleteById(id);
    }

    @Test
    void shouldReturnStatus409WhenGenreHasAssociatedGamesOnDelete() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new DatabaseException("Cannot delete genre because it has associated games"))
                .when(genreService).deleteById(id);

        mockMvc.perform(delete("/genres/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot delete genre because it has associated games"));

        verify(genreService).deleteById(id);
    }

    @Test
    void shouldReturnStatus400WhenIdFormatIsInvalidOnDelete() throws Exception {
        String invalidId = "invalid-id-123";

        mockMvc.perform(delete("/genres/" + invalidId))
                .andExpect(status().isBadRequest());

        verify(genreService, never()).deleteById(any());
    }

    @Test
    void shouldReturnStatus404WhenGenreIsDeletedConcurrently() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new ResourceNotFoundException(id))
                .when(genreService).deleteById(id);

        mockMvc.perform(delete("/genres/" + id))
                .andExpect(status().isNotFound());

        verify(genreService).deleteById(id);
    }
}