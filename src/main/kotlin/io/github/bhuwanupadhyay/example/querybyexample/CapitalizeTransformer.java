package io.github.bhuwanupadhyay.example.querybyexample;

import org.springframework.data.domain.ExampleMatcher.PropertyValueTransformer;

import java.util.Optional;

class CapitalizeTransformer implements PropertyValueTransformer {

	@Override
	public Optional<Object> apply(Optional<Object> o) {
		if (o.isPresent()) {
			String v = (String) o.get();
			return Optional.of(v.toUpperCase());
		} else {
			return Optional.empty();
		}
	}
}
