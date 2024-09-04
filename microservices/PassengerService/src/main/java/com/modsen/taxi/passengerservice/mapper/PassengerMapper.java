package com.modsen.taxi.passengerservice.mapper;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PassengerMapper {
    PassengerMapper INSTANCE = Mappers.getMapper(PassengerMapper.class);

    Passenger toPassenger(PassengerRequest request);

    PassengerResponse toPassengerResponse(Passenger passenger);

    void updatePassengerFromRequest(PassengerRequest request, @MappingTarget Passenger passenger);
}
