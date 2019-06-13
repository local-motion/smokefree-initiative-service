package io.localmotion.userdata;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Singleton
public class UserDataRepository {

//    @PersistenceContext
    private EntityManager entityManager;

    public UserDataRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void store(String userId, String userData) {

        // create record


        entityManager.persist(userData);
    }

    @Transactional(readOnly = true)
    public String retrieve(String userId) {
        Query query = entityManager.createQuery(
                "SELECT m from UserDataRecord m " +
                        "WHERE m.userId = :userId "
        );
        query.setParameter("userId", userId);
        UserDataRecord userDataRecord = (UserDataRecord) query.getSingleResult();
        return userDataRecord.getText();
    }

}
