package io.github.bhuwanupadhyay.example.querybyexample;

import org.springframework.data.domain.ExampleMatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

abstract class EntityQuery<R, E> {

    protected final ExampleMatcher matcher = ExampleMatcher
            .matchingAll()
            .withIgnoreNullValues()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
    private final R request;

    public EntityQuery(R request) {
        this.request = request;
        this.enrichMatcher();
    }

    public ExampleMatcher getMatcher() {
        return matcher;
    }

    protected abstract void enrichMatcher();

    public abstract E getExample();

    protected Optional<R> getRequest() {
        return Optional.ofNullable(request);
    }

    protected LocalDateTime queryValueFromDate(String date) {
        return Optional.ofNullable(date)
                .map(DateTimeFormatter.ISO_LOCAL_DATE::parse)
                .map(LocalDate::from)
                .map(v -> LocalDateTime.of(v, LocalTime.MIDNIGHT)).orElse(null);
    }

}
