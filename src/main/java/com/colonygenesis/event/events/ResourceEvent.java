package com.colonygenesis.event.events;

import com.colonygenesis.event.GameEvent;
import com.colonygenesis.resource.ResourceType;

import java.util.Map;

public class ResourceEvent extends GameEvent {
    private final ResourceType resourceType;
    private final int amount;
    private final int previousAmount;
    private final Map<ResourceType, Integer> allResources;

    public ResourceEvent(Object source, ResourceType resourceType, int amount, int previousAmount) {
        super(source, EventType.RESOURCE_CHANGED);
        this.resourceType = resourceType;
        this.amount = amount;
        this.previousAmount = previousAmount;
        this.allResources = null;
    }

    public ResourceEvent(Object source, Map<ResourceType, Integer> allResources) {
        super(source, EventType.RESOURCE_CHANGED);
        this.resourceType = null;
        this.amount = 0;
        this.previousAmount = 0;
        this.allResources = allResources;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getAmount() {
        return amount;
    }

    public int getPreviousAmount() {
        return previousAmount;
    }

    public int getDelta() {
        return amount - previousAmount;
    }

    public Map<ResourceType, Integer> getAllResources() {
        return allResources;
    }

    public boolean isBulkUpdate() {
        return allResources != null;
    }
}