package io.localmotion.eventsourcing.axon;

import org.axonframework.eventhandling.EventMessage;

/**
 * Projection can defer their event handlers to implementors of this interface
 * @param <T> projection class that receives the events
 */
public interface EventHandlerPlugin<T> {

    /**
     * Try to handle the event contained in the event message
     * @param target instance that received the event
     * @param eventMessage the message containing the event
     * @return whether this event has been handled
     */
    boolean handleEventMessage(T target, EventMessage eventMessage);
}
