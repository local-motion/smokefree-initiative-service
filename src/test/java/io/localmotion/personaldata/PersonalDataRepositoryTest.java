package io.localmotion.personaldata;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@Slf4j
class PersonalDataRepositoryTest {

	@Inject
	PersonalDataRepository personalDataRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	PlatformTransactionManager platformTransactionManager;

	@BeforeEach
	void clearDataBase() {
		TransactionStatus tx = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

		//Delete all rows from PersonalDataRecord Entity
		CriteriaDelete<PersonalDataRecord> deletePersonalDataRecord = entityManager.getCriteriaBuilder().createCriteriaDelete(PersonalDataRecord.class);
		deletePersonalDataRecord.from(PersonalDataRecord.class);
		entityManager.createQuery(deletePersonalDataRecord).executeUpdate();
		platformTransactionManager.commit(tx);
	}

	@Test
	void getRecordByPersonId() {
		// Given
		//long recordId = 1;
		String data = "{\"name\":\"ta-user-5\",\"emailAddress\":\"anandaili08@gmail.com\"}";
		String personId = UUID.randomUUID().toString();
		PersonalDataRecord record_1 = new PersonalDataRecord(personId,data);
		entityManager.persist(record_1);
		PersonalDataRecord expectedPersonalDataRecord = record_1;

		// When
		PersonalDataRecord actualPersonalDataRecord =  personalDataRepository.getRecordByPersonId(personId);

		// Then
		assertEquals(expectedPersonalDataRecord, actualPersonalDataRecord);
	}
}
