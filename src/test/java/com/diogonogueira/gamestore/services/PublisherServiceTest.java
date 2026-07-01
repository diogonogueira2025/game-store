package com.diogonogueira.gamestore.services;

import com.diogonogueira.gamestore.dtos.publisher.PublisherRequest;
import com.diogonogueira.gamestore.dtos.publisher.PublisherResponse;
import com.diogonogueira.gamestore.entities.Publisher;
import com.diogonogueira.gamestore.mappers.PublisherMapper;
import com.diogonogueira.gamestore.repositories.PublisherRepository;
import com.diogonogueira.gamestore.services.exceptions.BusinessRuleException;
import com.diogonogueira.gamestore.services.exceptions.DatabaseException;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {
    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private PublisherMapper publisherMapper;

    @InjectMocks
    private PublisherService publisherService;

    @Test
    void shouldReturnPublisherWhenPublisherExists() {
        UUID id = UUID.randomUUID();
        Publisher publisher = new Publisher(id, "Mojang");
        PublisherResponse publisherResponse = new PublisherResponse(id, "Mojang");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));
        when(publisherMapper.toResponse(publisher))
                .thenReturn(publisherResponse);

        PublisherResponse result = publisherService.findById(id);
        assertEquals(publisherResponse, result);

        verify(publisherRepository).findById(id);
        verify(publisherMapper).toResponse(publisher);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPublisherDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(publisherRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publisherService.findById(id));

        verify(publisherRepository).findById(id);
        verify(publisherMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnPageOfPublisherResponsesWhenPublishersExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        UUID bandaiId = UUID.randomUUID();
        UUID squareEnixId = UUID.randomUUID();

        Publisher bandai = new Publisher(bandaiId, "Bandai");
        Publisher squareEnix = new Publisher(squareEnixId, "Square Enix");

        PublisherResponse bandaiResponse = new PublisherResponse(bandaiId, "Bandai");
        PublisherResponse squareEnixResponse = new PublisherResponse(squareEnixId, "Square Enix");

        Page<Publisher> publisherPage = new PageImpl<>(
                List.of(bandai, squareEnix),
                pageable,
                2
        );
        when(publisherRepository.findAll(pageable))
                .thenReturn(publisherPage);
        when(publisherMapper.toResponse(bandai))
                .thenReturn(bandaiResponse);
        when(publisherMapper.toResponse(squareEnix))
                .thenReturn(squareEnixResponse);

        Page<PublisherResponse> result = publisherService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(bandaiResponse, squareEnixResponse), result.getContent());

        verify(publisherRepository).findAll(pageable);
        verify(publisherMapper).toResponse(bandai);
        verify(publisherMapper).toResponse(squareEnix);
    }

    @Test
    void shouldSavePublisherAndReturnPublisherResponse() {
        UUID id = UUID.randomUUID();

        PublisherRequest publisherRequest = new PublisherRequest("Bandai");
        Publisher publisher = new Publisher(null, "Bandai");
        Publisher savedPublisher = new Publisher(id, "Bandai");
        PublisherResponse publisherResponse = new PublisherResponse(id, "Bandai");

        when(publisherRepository.existsByName("Bandai"))
                .thenReturn(false);
        when(publisherMapper.toEntity(publisherRequest))
                .thenReturn(publisher);
        when(publisherRepository.save(publisher))
                .thenReturn(savedPublisher);
        when(publisherMapper.toResponse(savedPublisher))
                .thenReturn(publisherResponse);

        PublisherResponse result = publisherService.save(publisherRequest);

        assertEquals(publisherResponse, result);

        verify(publisherRepository).existsByName("Bandai");
        verify(publisherMapper).toEntity(publisherRequest);
        verify(publisherRepository).save(publisher);
        verify(publisherMapper).toResponse(savedPublisher);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenPublisherDoesExist() {
        PublisherRequest publisherRequest = new PublisherRequest("Bandai");

        when(publisherRepository.existsByName("Bandai"))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> {
            publisherService.save(publisherRequest);
        });

        verify(publisherRepository).existsByName("Bandai");
        verify(publisherMapper, never()).toEntity(any());
        verify(publisherRepository, never()).save(any());
        verify(publisherMapper, never()).toResponse(any());
    }

    @Test
    void shouldUpdatePublisherAndReturnPublisherResponse() {
        UUID id = UUID.randomUUID();

        PublisherRequest publisherRequest = new PublisherRequest("Square Enix");
        Publisher publisher = new Publisher(id, "Bandai");
        PublisherResponse publisherResponse = new PublisherResponse(id, "Square Enix");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));
        when(publisherRepository.existsByName("Square Enix"))
                .thenReturn(false);
        when(publisherMapper.toResponse(publisher))
                .thenReturn(publisherResponse);

        PublisherResponse result = publisherService.update(id, publisherRequest);
        assertEquals(publisherResponse, result);

        verify(publisherRepository).findById(id);
        verify(publisherRepository).existsByName("Square Enix");
        verify(publisherMapper).updateEntityFromRequest(publisherRequest, publisher);
        verify(publisherMapper).toResponse(publisher);
    }

    @Test
    void shouldNotThrowExceptionWhenUpdatingWithSameName() {
        UUID id = UUID.randomUUID();

        PublisherRequest publisherRequest = new PublisherRequest("Bandai");
        Publisher publisher = new Publisher(id, "Bandai");
        PublisherResponse publisherResponse = new PublisherResponse(id, "Bandai");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));
        when(publisherMapper.toResponse(publisher))
                .thenReturn(publisherResponse);

        PublisherResponse result = publisherService.update(id, publisherRequest);
        assertEquals(publisherResponse, result);

        verify(publisherRepository).findById(id);
        verify(publisherRepository, never()).existsByName(any());
        verify(publisherMapper).updateEntityFromRequest(publisherRequest, publisher);
        verify(publisherMapper).toResponse(publisher);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenUpdatingToExistingName() {
        UUID id = UUID.randomUUID();
        Publisher existingPublisher = new Publisher(id, "Bandai");
        PublisherRequest publisherRequest = new PublisherRequest("Square Enix");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(existingPublisher));
        when(publisherRepository.existsByName("Square Enix"))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> {
            publisherService.update(id, publisherRequest);
        });

        verify(publisherRepository).findById(id);
        verify(publisherRepository).existsByName("Square Enix");
        verify(publisherMapper, never()).updateEntityFromRequest(any(), any());
        verify(publisherMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingPublisherDoesNotExist() {
        UUID id = UUID.randomUUID();
        PublisherRequest publisherRequest = new PublisherRequest("Bandai");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publisherService.update(id, publisherRequest));

        verify(publisherRepository).findById(id);
        verify(publisherRepository, never()).existsByName(any());
        verify(publisherMapper, never()).updateEntityFromRequest(any(), any());
        verify(publisherMapper, never()).toResponse(any());
    }

    @Test
    void shouldDeletePublisherWhenPublisherHasNoAssociatedGames() {
        UUID id = UUID.randomUUID();
        Publisher publisher = new Publisher(id, "Bandai");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));

        publisherService.deleteById(id);

        verify(publisherRepository).findById(id);
        verify(publisherRepository).deleteById(id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingPublisherDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(publisherRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publisherService.deleteById(id));

        verify(publisherRepository).findById(id);
        verify(publisherRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowDatabaseExceptionWhenDeletingPublisherWithAssociatedGames() {
        UUID id = UUID.randomUUID();
        Publisher publisher = new Publisher(id, "Bandai");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));

        doThrow(DataIntegrityViolationException.class)
                .when(publisherRepository).deleteById(id);

        assertThrows(DatabaseException.class, () -> publisherService.deleteById(id));

        verify(publisherRepository).findById(id);
        verify(publisherRepository).deleteById(id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPublisherIsDeletedConcurrently() {
        UUID id = UUID.randomUUID();
        Publisher publisher = new Publisher(id, "Square Enix");

        when(publisherRepository.findById(id))
                .thenReturn(Optional.of(publisher));

        doThrow(EmptyResultDataAccessException.class)
                .when(publisherRepository).deleteById(id);

        assertThrows(ResourceNotFoundException.class, () -> {
            publisherService.deleteById(id);
        });

        verify(publisherRepository).findById(id);
        verify(publisherRepository).deleteById(id);
    }
}