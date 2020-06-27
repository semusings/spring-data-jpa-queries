package io.github.bhuwanupadhyay.example.querybyexample;

import io.github.bhuwanupadhyay.example.OrderEntity;
import io.github.bhuwanupadhyay.example.OrderQueryRequest;

public class OrderExampleQuery extends ExampleQuery<OrderQueryRequest, OrderEntity> {

	public OrderExampleQuery(OrderQueryRequest request) {
		super(request);
	}

	@Override
	protected void enrichMatcher() {
		getMatcher().withTransformer("status", new CapitalizeTransformer());
	}

	@Override
	public OrderEntity getExample() {
		return getRequest().map(req -> {
			OrderEntity entity = new OrderEntity();
			entity.setId(req.getId());
			entity.setItem(req.getItem());
			entity.setQuantity(req.getQuantity());
			entity.setStatus(req.getStatus());
			entity.setCreatedDate(null);
			return entity;
		}).orElse(new OrderEntity());
	}
}
