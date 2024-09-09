package com.modsen.taxi.tripservice.dto.error;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record AppErrorCustom(

        int status,

        String message,

        LocalDateTime timestamp,

        Map<String, String> errors
) {
}
