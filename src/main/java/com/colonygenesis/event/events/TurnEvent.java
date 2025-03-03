package com.colonygenesis.event.events;

import com.colonygenesis.core.TurnPhase;
import com.colonygenesis.event.GameEvent;

public class TurnEvent extends GameEvent {
    private final int turnNumber;
    private final TurnPhase phase;
    private final int previousTurn;
    private final TurnPhase previousPhase;

    public TurnEvent(Object source, EventType type, int turnNumber, TurnPhase phase, int previousTurn, TurnPhase previousPhase) {
        super(source, type);
        this.turnNumber = turnNumber;
        this.phase = phase;
        this.previousTurn = previousTurn;
        this.previousPhase = previousPhase;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public TurnPhase getPhase() {
        return phase;
    }

    public int getPreviousTurn() {
        return previousTurn;
    }

    public TurnPhase getPreviousPhase() {
        return previousPhase;
    }

    // Factory methods for common turn events
    public static TurnEvent turnAdvanced(Object source, int newTurn, int previousTurn) {
        return new TurnEvent(source, EventType.TURN_ADVANCED, newTurn, TurnPhase.PLANNING, previousTurn, TurnPhase.END_TURN);
    }

    public static TurnEvent phaseChanged(Object source, int turn, TurnPhase newPhase, TurnPhase previousPhase) {
        return new TurnEvent(source, EventType.PHASE_CHANGED, turn, newPhase, turn, previousPhase);
    }
}