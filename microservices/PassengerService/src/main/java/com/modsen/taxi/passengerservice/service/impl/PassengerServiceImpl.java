package com.modsen.taxi.passengerservice.service.impl;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.mapper.PassengerMapper;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import com.modsen.taxi.passengerservice.service.PassengerService;
import dto.PassengerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper = PassengerMapper.INSTANCE;


    @Override
    public PassengerDTO createPassenger(PassengerDTO passengerDTO) {
        Passenger passenger = passengerMapper.toPassenger(passengerDTO);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.toPassengerDTO(savedPassenger);
    }

    @Override
    public Optional<PassengerDTO> updatePassenger(Long id, PassengerDTO passengerDTO) {
        return passengerRepository.findByIdAndIsDeletedFalse(id)
                .map(existingPassenger -> {
                    Passenger updatedPassenger = passengerMapper.toPassenger(passengerDTO);
                    updatedPassenger.setId(existingPassenger.getId());
                    Passenger savedPassenger = passengerRepository.save(updatedPassenger);
                    return passengerMapper.toPassengerDTO(savedPassenger);
                });
    }

    @Override
    public Optional<PassengerDTO> getPassengerById(Long id) {
        return passengerRepository.findByIdAndIsDeletedFalse(id)
                .map(passengerMapper::toPassengerDTO);
    }

    @Override
    public List<PassengerDTO> getAllPassengers() {
        return passengerRepository.findAllByIsDeletedFalse()
                .stream()
                .map(passengerMapper::toPassengerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePassenger(Long id) {
        passengerRepository.findByIdAndIsDeletedFalse(id)
                .ifPresent(passenger -> {
                    passenger.setIsDeleted(true);
                    passengerRepository.save(passenger);
                });
    }
}
