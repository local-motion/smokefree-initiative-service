package io.localmotion.adminjob.commands.chatverification;

import com.google.gson.Gson;
import io.localmotion.adminjob.commands.cognitoimportfile.CognitoImportFileResult;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.model.ChatBox;
import io.localmotion.chatbox.model.Participation;
import io.localmotion.chatbox.model.ParticipationId;
import io.localmotion.chatbox.model.User;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.storage.file.FileAccessor;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.context.annotation.Value;
import org.hibernate.Hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ChatVerificationCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "ChatVerification";

    @Inject
    private ProfileProjection profileProjection;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private ChatboxRepository chatboxRepository;


    @Override
    public String getIdentifier() {
        return COMMAND_IDENTIFIER;
    }

    @Override
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord, SecurityContext securityContext) {

        List<String> deviations = new ArrayList<>();

        Set<Integer> remainingChatBoxIds = new HashSet<>(chatboxRepository.getAllChatBoxes().stream().map(chatBox -> chatBox.getId()).collect(Collectors.toList()));
        Set<Integer> remainingChatBoxUserIds = new HashSet<>(chatboxRepository.getAllUsers().stream().map(user -> user.getId()).collect(Collectors.toList()));

        for (Initiative i : initiativeProjection.getAllInitiatives()) {
            ChatBox chatBox = chatboxRepository.getChatBoxWithExternalId(i.getId());

            if (chatBox == null)
                deviations.add("No chatbox present for initiative " + i);
            else {
                Set<Participation> participations = new HashSet<>(chatBox.getParticipations());
                Set<ParticipationId> remainingParticipationIds = new HashSet<>(participations.stream()
                        .map(participation -> participation.getParticipationId()).collect(Collectors.toList()));
                for (Initiative.Member m : i.getMembers()) {
                    Optional<Participation> participation = participations.stream().filter(p -> p.getUser().getExternalId().equals(m.getUserId())).findFirst();
                    if (!participation.isPresent())
                        deviations.add("No participation present for member " + m + " of initiative " + getPrettyInitiativeName(i));
                    else {
                        if (!participation.get().getUser().getName().equals(m.getUserName()))
                            deviations.add("Member " + m + " of initiative " + getPrettyInitiativeName(i) + " has a different name in chatbox: " + participation.get().getUser());
                        remainingParticipationIds.remove(participation.get().getParticipationId());
                        remainingChatBoxUserIds.remove(participation.get().getUser().getId());
                    }

                }
                for (ParticipationId p : remainingParticipationIds) {
                    Participation participation = chatboxRepository.getParticipation(p);
                    deviations.add("Participation registered for user " + participation.getUser() + " in chatbox " + chatBox + " while the user is not a member of the corresponding initiative");
                    remainingChatBoxUserIds.remove(participation.getUser().getId());
                }
            }
            remainingChatBoxIds.remove(chatBox.getId());
        }

        remainingChatBoxIds.stream().forEach(chatboxId -> deviations.add("ChatBox " + chatboxRepository.getChatBox(chatboxId) + " does not belong to an active initiative"));
        remainingChatBoxUserIds.stream().forEach(userId -> {
//            Hibernate.initialize(user.getParticipations());
            User user = chatboxRepository.getUser(userId);

            deviations.add(
                            user.getParticipations().isEmpty() ?
//                            chatboxRepository.getParticipationsForUser(user).isEmpty() ?
                                    "User " + user + " does not belong to a chatbox"
                                    :
                                    "User " + user + " only participates in chatboxes that do not belong to an active initiative"
            );
                }
        );


        ChatVerificationResult result = new ChatVerificationResult(deviations);

        return new JobResult(JobResultCode.SUCCESS, deviations.isEmpty() ? "No deviations" : deviations.size() + " deviations found", new Gson().toJson(result));
    }

    private String getPrettyInitiativeName(Initiative initiative) {
        return initiative.getId() + "-" + initiative.getName();
    }
}
