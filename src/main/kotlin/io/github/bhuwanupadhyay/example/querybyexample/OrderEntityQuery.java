package io.github.bhuwanupadhyay.example.querybyexample;

import io.github.bhuwanupadhyay.example.OrderEntity;
import io.github.bhuwanupadhyay.example.OrderQueryRequest;

public class OrderEntityQuery extends EntityQuery<OrderQueryRequest, OrderEntity> {

    public OrderEntityQuery(OrderQueryRequest request) {
        super(request);
    }

    @Override
    protected void enrichMatcher() {
        matcher.withTransformer("createdDate", new LocalDateTimeTransformer());
    }

    @Override
    public OrderEntity getExample() {
        return getRequest().map(req -> {
            OrderEntity entity = new OrderEntity();
            entity.setId(req.getId());
            entity.setItem(req.getItem());
            entity.setQuantity(req.getQuantity());
            entity.setCreatedDate(queryValueFromDate(req.getCreatedDate()));
            return entity;
        }).orElse(new OrderEntity());
    }
}
