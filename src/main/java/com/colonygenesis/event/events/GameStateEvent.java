package com.colonygenesis.event.events;

import com.colonygenesis.event.GameEvent;

public class GameStateEvent extends GameEvent {
    private final GameStateType stateType;
    private final Object data;

    public GameStateEvent(Object source, GameStateType stateType, Object data) {
        super(source, EventType.GAME_STATE_CHANGED);
        this.stateType = stateType;
        this.data = data;
    }

    public GameStateType getStateType() {
        return stateType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public enum GameStateType {
        GAME_INITIALIZED,
        GAME_STARTED,
        GAME_LOADED,
        GAME_SAVED,
        GAME_PAUSED,
        GAME_RESUMED,
        GAME_ENDED,
        VICTORY_ACHIEVED,
        CRISIS_STARTED,
        CRISIS_RESOLVED
    }
}