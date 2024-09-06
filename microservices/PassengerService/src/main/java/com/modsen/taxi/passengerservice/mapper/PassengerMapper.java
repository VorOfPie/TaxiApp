package com.modsen.taxi.passengerservice.mapper;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    Passenger toPassenger(PassengerRequest request);

    PassengerResponse toPassengerResponse(Passenger passenger);

    void updatePassengerFromRequest(PassengerRequest request, @MappingTarget Passenger passenger);
}
