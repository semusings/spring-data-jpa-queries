package io.github.bhuwanupadhyay.example.querybyexample;

import org.springframework.data.domain.ExampleMatcher.PropertyValueTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

class LocalDateTimeTransformer implements PropertyValueTransformer {

    @Override
    public Optional<Object> apply(Optional<Object> o) {
        if (o.isPresent()) {
            LocalDateTime dateTime = (LocalDateTime) o.get();
            LocalDate date = dateTime.toLocalDate();
            LocalTime midnight = LocalTime.MIDNIGHT;
            return Optional.of(LocalDateTime.of(date, midnight));
        } else {
            return Optional.empty();
        }
    }
}
