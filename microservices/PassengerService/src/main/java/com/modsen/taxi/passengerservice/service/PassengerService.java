package com.modsen.taxi.passengerservice.service;


import dto.PassengerDTO;

import java.util.List;
import java.util.Optional;

public interface PassengerService {

    PassengerDTO createPassenger(PassengerDTO passengerDTO);

    Optional<PassengerDTO> updatePassenger(Long id, PassengerDTO passengerDTO);

    Optional<PassengerDTO> getPassengerById(Long id);

    List<PassengerDTO> getAllPassengers();

    void deletePassenger(Long id);
}
