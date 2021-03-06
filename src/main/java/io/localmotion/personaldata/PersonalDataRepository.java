package io.localmotion.personaldata;

import io.micronaut.spring.tx.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * The PersonalDataRepository store and provides access to Personally Identifiable Information.
 * This information is kept in a separate store in order to allow for it to be deleted when a person
 * requests to do so as mandated by GDPR regulations.
 *
 * This store maintains records with  the following structure:
 * - record id: single purpose is to be able to reference individual records
 * - person identifier: any kind of key that is used to identify a person. This is used to select the records for deletion.
 * - data: json structure containing relevant data for this record
 *
 * Each record belongs to a single event in the system and is referenced only by this event. The event handlers need to
 * be prepared for the fact that this record may be erased.
 *
 * Apart for their deletion when a person asserts their 'right to be forgotten' these records will not be modified or deleted.
 */
@Slf4j
@Singleton
public class PersonalDataRepository {

    @PersistenceContext
    private EntityManager entityManager;
  
    @Transactional
    public void storeRecord(PersonalDataRecord personalDataRecord) {
        entityManager.persist(personalDataRecord);
    }

    @Transactional(readOnly = true)
    public PersonalDataRecord getRecord(long recordId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT r from PersonalDataRecord r " +
                            "WHERE r.recordId = :recordId"
            );
            query.setParameter("recordId", recordId);
            return (PersonalDataRecord) query.getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public int deleteRecordsOfPerson(String personId) {
        Query query = entityManager.createQuery(
                "DELETE from PersonalDataRecord r " +
                        "WHERE r.personId = :personId"
        );
        query.setParameter("personId", personId);
        int deletedCount = query.executeUpdate();
        return deletedCount;
    }

}
