package com.modsen.taxi.passengerservice.mapper;

import com.modsen.taxi.passengerservice.domain.Passenger;
import dto.PassengerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PassengerMapper {
    PassengerMapper INSTANCE = Mappers.getMapper(PassengerMapper.class);

    PassengerDTO toPassengerDTO(Passenger passenger);

    Passenger toPassenger(PassengerDTO passengerDTO);

    List<PassengerDTO> toPassengerDTOs(List<Passenger> passengers);

    List<Passenger> toPassengers(List<PassengerDTO> passengerDTOs);
}