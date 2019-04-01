package io.localmotion.initiative.event;

import lombok.*;

/**
 * The checklist is used to keep track of various events / activities within an initiative.
 * This eliminates the need to have separate events (and commands and other handlers) for each item.
 * The actor is explicitly included (as opposed to just keeping it in the metadata) because some items
 * are tracked per actor as opposed to per initiative.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class ChecklistUpdateEvent {
    String initiativeId;
    String actor;                       // The user that checked / unchecked the item
    String checklistItem;               // Identifies the item on the checklist that is ticked or not
    boolean checked;                    // The new state of the item
}
