package com.modsen.taxi.tripservice.mapper;

import com.modsen.taxi.tripservice.domain.Trip;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TripMapper {

    Trip toEntity(TripRequest tripRequest);

    TripResponse toDTO(Trip trip);

    void updateTripFromRequest(TripRequest request, @MappingTarget Trip trip);

}
