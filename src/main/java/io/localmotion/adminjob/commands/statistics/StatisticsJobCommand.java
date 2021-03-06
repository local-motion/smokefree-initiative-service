package io.localmotion.adminjob.commands.statistics;

import com.google.gson.Gson;
import io.localmotion.adminjob.domain.AdminCommand;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.adminjob.domain.JobResultCode;
import io.localmotion.eventsourcing.tracker.TrackerProjection;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.user.projection.ProfileProjection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Singleton
public class StatisticsJobCommand implements AdminCommand {

    private static final String COMMAND_IDENTIFIER = "Statistics";

    @Inject
    private ProfileProjection profileProjection;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private TrackerProjection trackerProjection;

    @Inject
    private DataSource dataSource;


    @Override
    public String getIdentifier() {
        return COMMAND_IDENTIFIER;
    }

    @Override
    public JobResult run(AdminJobCommandRecord adminJobCommandRecord, SecurityContext securityContext) {

        if (!trackerProjection.isUpToDate())
            return new JobResult(JobResultCode.FAIL, "Projections are not up-to-date (yet)");

        Connection connection = null;
        try {
            StatisticsResult result = new StatisticsResult(
                    getCountFromTable("cb1_chat_message"),
                    getCountFromTable("DomainEventEntry"),
                    getCountFromTable("SnapshotEventEntry"),
                    getCountFromTable("UserDataRecord"),
                    getCountFromTable("PersonalDataRecord"),
                    profileProjection.getAllProfiles().size(),
                    profileProjection.getAllDeletedProfiles().size(),
                    initiativeProjection.getAllInitiatives().size()
            );
            return new JobResult(JobResultCode.SUCCESS, "Success", new Gson().toJson(result));

        } catch (SQLException e) {
            e.printStackTrace();
            return new JobResult(JobResultCode.FAIL, e.getMessage());
        }

    }

    private int getCountFromTable(String tableName) throws SQLException {

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("select count(*) from " + tableName + ";");
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            return resultSet.getInt(1);

        } finally {
            connection.close();
        }

    }
}
