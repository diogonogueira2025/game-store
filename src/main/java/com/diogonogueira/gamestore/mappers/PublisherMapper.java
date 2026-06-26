package com.diogonogueira.gamestore.mappers;

import com.diogonogueira.gamestore.dtos.publisher.PublisherRequest;
import com.diogonogueira.gamestore.dtos.publisher.PublisherResponse;
import com.diogonogueira.gamestore.entities.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    PublisherResponse toResponse(Publisher publisher);
    Publisher toEntity(PublisherRequest publisherRequest);
    void updateEntityFromRequest(PublisherRequest publisherRequest, @MappingTarget Publisher publisher);
}
