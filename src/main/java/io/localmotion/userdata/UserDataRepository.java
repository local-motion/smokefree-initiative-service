package io.localmotion.userdata;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.hql.internal.ast.QuerySyntaxException;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;

@Slf4j
@Singleton
public class UserDataRepository {

//    @PersistenceContext
    private EntityManager entityManager;

    public UserDataRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void store(String userId, String userData) {
//        entityManager.persist(new UserDataRecord(userId, new Date(), userData));
        retrieveRecord(userId).setText(userData);
    }

    @Transactional(readOnly = true)
    public String retrieve(String userId) {
        return retrieveRecord(userId).getText();
//        try {
//            Query query = entityManager.createQuery(
//                    "SELECT m from UserDataRecord m " +
//                            "WHERE m.userId = :userId "
//            );
//            query.setParameter("userId", userId);
//            UserDataRecord userDataRecord = (UserDataRecord) query.getSingleResult();
//            return userDataRecord.getText();
//        } catch (NoResultException e) {
//            return "{}";
//        } catch (IllegalArgumentException e) {
//            if (e.getCause() instanceof  QuerySyntaxException && e.getCause().getMessage().startsWith("UserDataRecord is not mapped")) {
//                log.warn("No user data table present, serving empty user data");
//                entityManager.persist(new UserDataRecord(userId, new Date(), "{}"));
//                return "{}";
//            }
//            else
//                throw e;
//        }
    }

@Transactional(readOnly = true)
    private UserDataRecord retrieveRecord(String userId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT m from UserDataRecord m " +
                            "WHERE m.userId = :userId "
            );
            query.setParameter("userId", userId);
            return (UserDataRecord) query.getSingleResult();
        } catch (NoResultException e) {
            UserDataRecord userDataRecord = new UserDataRecord(userId, new Date(), "{}");
            entityManager.persist(userDataRecord);
            return userDataRecord;
        } catch (IllegalArgumentException e) {
            if (e.getCause() instanceof  QuerySyntaxException && e.getCause().getMessage().startsWith("UserDataRecord is not mapped")) {
                log.warn("No user data table present, serving empty user data");
                UserDataRecord userDataRecord = new UserDataRecord(userId, new Date(), "{}");
                entityManager.persist(userDataRecord);
                return userDataRecord;
            }
            else
                throw e;
        }
    }

}
