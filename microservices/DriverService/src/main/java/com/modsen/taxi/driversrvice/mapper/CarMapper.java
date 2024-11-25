package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "driver", ignore = true)
    Car toCar(CreateCarRequest carRequest);

    CarResponse toCarResponse(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "driver", ignore = true)
    void updateCarFromRequest(CreateCarRequest carRequest, @MappingTarget Car car);
}

