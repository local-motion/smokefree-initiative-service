package io.localmotion.adminjob.commands.chatmigration_v1_0_18_v1_0_19;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.localmotion.adminjob.commands.deletepersonaldata.DeletePersonalDataInput;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.application.DomainException;
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
 * This command migrates chat messages between the v1.0.18-beta version and the v1.0.19-beta version.
 * In v1.0.19-beta all chat tables/entities will auto-populate from the event synchronisation, only the
 * chat messages needs to be migrated.
 *
 * It is advised to use the following migration scenario:
 * - first create a database backup
 * - run a DRY_RUN
 * - then MIGRATE
 * - finally use DROP_TABLE to remove the v1_0_18 chat messages table
 *
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
            ChatMigrationInput input = new Gson().fromJson(adminJobCommandRecord.getInputParameters(), ChatMigrationInput.class);

            try {
                    // Drop table action
                if (input.getMigrationAction() == ChatMigrationAction.DROPTABLE) {
                    connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement("drop table ChatMessage;");
                    statement.executeUpdate();

                    return new JobResult(
                            JobResultCode.SUCCESS,
                            "Successfully dropped table",
                            new Gson().toJson(new ChatMigrationResult(Collections.emptyList()))
                    );
                }
                else {
                    // Migrate action (for real or dry run)
                    List<String> conversions = new ArrayList<>();
                    int failCount = 0;

                    connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement("select * from ChatMessage;");
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String messageId = resultSet.getString("messageId");
                        String authorName = resultSet.getString("author");
                        String chatboxId = resultSet.getString("chatboxId");
                        Date creationTime = new Date(resultSet.getDate("creationTime").getTime());
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

                        if (input.getMigrationAction() != ChatMigrationAction.DRYRUN)
                            chatboxRepository.storeMessage(chatBox.getId(), author.getId(), text, creationTime.toInstant());

                        conversions.add("m-" + messageId + " cb-" + chatboxId + " " + authorName + " '" + text + "'");
                    }

                    return new JobResult(
                            JobResultCode.SUCCESS,
                            "Successfully migrated " + (conversions.size() - failCount) + "  messages with " + failCount + " failures",
                            new Gson().toJson(new ChatMigrationResult(conversions))
                    );
                }

            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new JobResult(JobResultCode.FAIL, e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new DomainException("INVALID_COMMAND_RECORD", "Command record contains invalid Json");        }
    }

}
