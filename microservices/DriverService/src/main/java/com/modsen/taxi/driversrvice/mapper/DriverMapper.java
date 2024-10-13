package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "cars", source = "cars")
    Driver toDriver(DriverRequest driverRequest);

    DriverResponse toDriverResponse(Driver driver);

    default List<Car> carRequestsToCars(List<CarRequest> carRequests) {
        return carRequests.stream()
                .map(carRequest -> Car.builder()
                        .id(carRequest.id())
                        .brand(carRequest.brand())
                        .color(carRequest.color())
                        .licensePlate(carRequest.licensePlate())
                        .build())
                .collect(Collectors.toList());
    }

    @Mapping(target = "cars", ignore = true)
    Driver updateDriverFromRequest(DriverRequest driverRequest, @MappingTarget Driver driver);
}
