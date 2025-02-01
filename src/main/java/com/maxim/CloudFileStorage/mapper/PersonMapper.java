package com.maxim.CloudFileStorage.mapper;

import com.maxim.CloudFileStorage.dto.PersonDto;
import com.maxim.CloudFileStorage.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonMapper {
    Person toEntity(PersonDto personDto);
}
