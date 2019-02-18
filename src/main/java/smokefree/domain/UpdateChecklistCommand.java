package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;

/**
 * The checklist is used to keep track of various event / activities within an initiative update.
 * This eliminates the need to have separate commands (and events and other handlers) for each item.
 * The actor is explicitly included (as opposed to just keeping it in the metadata) because some items
 * are tracked per actor as opposed to per initiative.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChecklistCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    @NotBlank
    String actor;                       // The user that checked / unchecked the item
    @NotBlank
    String checklistItem;               // Identifies the item on the checklist that is ticked on not
    boolean checked;                    // The new state of the item
}
