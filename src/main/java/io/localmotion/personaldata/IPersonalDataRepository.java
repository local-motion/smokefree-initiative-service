package io.localmotion.personaldata;

public interface IPersonalDataRepository {
	void storeRecord(PersonalDataRecord personalDataRecord);

	PersonalDataRecord getRecord(long recordId);

	int deleteRecordsOfPerson(String personId);

	PersonalDataRecord getRecordByPersonId(String personId);
}
