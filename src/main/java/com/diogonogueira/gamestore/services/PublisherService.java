package com.diogonogueira.gamestore.services;

import com.diogonogueira.gamestore.dtos.publisher.PublisherRequest;
import com.diogonogueira.gamestore.dtos.publisher.PublisherResponse;
import com.diogonogueira.gamestore.entities.Publisher;
import com.diogonogueira.gamestore.mappers.PublisherMapper;
import com.diogonogueira.gamestore.repositories.PublisherRepository;
import com.diogonogueira.gamestore.services.exceptions.DatabaseException;
import com.diogonogueira.gamestore.services.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    public PublisherService(PublisherRepository publisherRepository, PublisherMapper publisherMapper) {
        this.publisherRepository = publisherRepository;
        this.publisherMapper = publisherMapper;
    }

    public Page<PublisherResponse> findAll(Pageable pageable) {
        return publisherRepository.findAll(pageable)
                .map(publisherMapper::toResponse);
    }

    private Publisher findEntityById(UUID id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public PublisherResponse findById(UUID id) {
        Publisher publisher = findEntityById(id);
        return publisherMapper.toResponse(publisher);
    }

    @Transactional
    public PublisherResponse save(PublisherRequest publisherRequest) {
        Publisher publisher = publisherMapper.toEntity(publisherRequest);
        Publisher savedPublisher = publisherRepository.save(publisher);
        return publisherMapper.toResponse(savedPublisher);
    }

    @Transactional
    public PublisherResponse update(UUID id, PublisherRequest publisherRequest) {
        Publisher publisher = findEntityById(id);
        publisherMapper.updateEntityFromRequest(publisherRequest, publisher);
        return publisherMapper.toResponse(publisher);
    }

    @Transactional
    public void deleteById(UUID id) {
        Publisher publisher = findEntityById(id);
        if (!publisher.getGames().isEmpty()) {
            throw new DatabaseException("Cannot delete publisher because it has associated games");
        }
        publisherRepository.delete(publisher);
    }
}
