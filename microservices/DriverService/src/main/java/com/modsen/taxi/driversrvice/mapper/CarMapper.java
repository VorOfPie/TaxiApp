package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    Car toCar(CreateCarRequest carRequest);

    CarResponse toCarResponse(Car car);

    void updateCarFromRequest(CreateCarRequest carRequest, @MappingTarget Car car);
}
