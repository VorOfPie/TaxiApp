package com.modsen.taxi.driversrvice.mapper;

import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverResponse toDriverResponse(Driver driver);

    Driver toDriver(DriverRequest driverRequest);

    void updateDriverFromRequest(DriverRequest driverRequest, @MappingTarget Driver driver);
}
