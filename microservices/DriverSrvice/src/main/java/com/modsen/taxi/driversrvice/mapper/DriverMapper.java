package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(source = "car", target = "car")
    Driver toDriver(DriverRequest driverRequest);

    @Mapping(source = "car", target = "car")
    DriverResponse toDriverResponse(Driver driver);

    void updateDriverFromRequest(DriverRequest driverRequest, @MappingTarget Driver driver);
}