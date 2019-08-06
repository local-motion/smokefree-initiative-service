package io.localmotion.adminjob.commands.chatmigration_v1_0_18_v1_0_19;

import com.google.gson.Gson;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.model.ChatBox;
import io.localmotion.chatbox.model.Participation;
import io.localmotion.chatbox.model.User;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This command migrates chat message between the v1.0.18-beta version and the v1.0.19-beta version.
 * In v1.0.19-beta all chat tables/entities will auto-populate from the event synchronisation, only the
 * chat messages needs to be migrated.
 */
@Singleton
public class ChatMigrationCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "ChatMigration1.0.18-1.0.19";

    @Inject
    private ProfileProjection profileProjection;

    @Inject
    private ChatboxRepository chatboxRepository;

    @Inject
    private DataSource dataSource;


    @Override
    public String getIdentifier() {
        return COMMAND_IDENTIFIER;
    }

    @Override
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord, SecurityContext securityContext) {

        Connection connection = null;
        try {
            try {
                List<String> conversions = new ArrayList<>();
                int failCount = 0;

                connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from ChatMessage;");
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String messageId = resultSet.getString("messageId");
                    String authorName = resultSet.getString("author");
                    String chatboxId = resultSet.getString("chatboxId");
                    Date creationTime = resultSet.getDate("creationTime");
                    String text = resultSet.getString("text");

                    Profile profile = profileProjection.getProfileByName(authorName);
                    if (profile == null) {
                        conversions.add("FAIL: no profile present for author " + authorName);
                        failCount++;
                        continue;
                    }

                    String authorId = profileProjection.getProfileByName(authorName).getId();
                    User author = chatboxRepository.getUserWithExternalId(authorId);
                    if (author == null) {
                        conversions.add("FAIL: author not known in chatbox " + authorName);
                        failCount++;
                        continue;
                    }

                    ChatBox chatBox = chatboxRepository.getChatBoxWithExternalId(chatboxId);
                    if (chatBox == null) {
                        conversions.add("FAIL: chatbox unknown for external id " + chatboxId);
                        failCount++;
                        continue;
                    }

                    Participation participation = chatboxRepository.getParticipation(chatBox.getId(), author.getId());
                    if (participation == null) {
                        conversions.add("FAIL: author " + authorName + " does not participate in chatbox with external id " + chatboxId);
                        failCount++;
                        continue;
                    }

                    chatboxRepository.storeMessage(chatBox.getId(), author.getId(), text, creationTime.toInstant());
                    conversions.add("m-" + messageId + " cb-" + chatboxId + " " + authorName + " " + text);
                }

                return new JobResult(
                        JobResultCode.SUCCESS,
                        "Successfully migrated " + (conversions.size() - failCount) + "  messages with " + failCount + " failures",
                        new Gson().toJson(new ChatMigrationResult(conversions))
                );

            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new JobResult(JobResultCode.FAIL, e.getMessage());
        }
    }

}
