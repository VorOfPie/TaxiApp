package com.modsen.taxi.ratingservice.mapper;

import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.RatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    Rating toRating(RatingRequest ratingRequest);

    RatingResponse toRatingResponse(Rating rating);

    void updateRatingFromRequest(RatingRequest ratingRequest, @MappingTarget Rating rating);
}
