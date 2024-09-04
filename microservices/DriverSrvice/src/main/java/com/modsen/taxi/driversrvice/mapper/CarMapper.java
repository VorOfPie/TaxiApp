package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarResponse toCarResponse(Car car);

    Car toCar(CarRequest carRequest);

    void updateCarFromRequest(CarRequest carRequest, @MappingTarget Car car);
}
