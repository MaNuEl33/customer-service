package com.vinsguru.mapper;

import com.vinsguru.dto.CustomerDto;
import com.vinsguru.dto.MovieDto;
import com.vinsguru.entity.Customer;

import java.util.List;

public class EntityDtoMapper {

    private EntityDtoMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static CustomerDto toDto(Customer customer, List<MovieDto> movies) {
        return new  CustomerDto(
                customer.getId(),
                customer.getName().toUpperCase(),
                customer.getFavoriteGenre(),
                movies
        );
    }
}
