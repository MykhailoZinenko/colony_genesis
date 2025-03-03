package com.colonygenesis.map;

import com.colonygenesis.resource.ResourceType;

public class ResourceDeposit {
    private final ResourceType resourceType;
    private final double yield;
    private final String name;

    public ResourceDeposit(ResourceType resourceType, double yield, String name) {
        this.resourceType = resourceType;
        this.yield = yield;
        this.name = name;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public double getYield() {
        return yield;
    }

    public String getName() {
        return name;
    }
}