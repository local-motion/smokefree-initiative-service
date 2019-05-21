package io.localmotion.picture.repository;

import io.localmotion.picture.entity.Picture;
import io.localmotion.picture.entity.PictureType;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class PictureRepositoryImplementationTest {

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	PlatformTransactionManager platformTransactionManager;

	@Inject
    PictureRepositoryImplementation pictureRepositoryImplementation;

	@BeforeEach
	void refreshDatabase() {
		TransactionStatus tx = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

		//Clear table before each test case starts
		CriteriaDelete<Picture> deletePicture =  entityManager.getCriteriaBuilder().createCriteriaDelete(Picture.class);
		deletePicture.from(Picture.class);
		entityManager.createQuery(deletePicture).executeUpdate();
		platformTransactionManager.commit(tx);
	}

	@Test
	void should_findPicture_when_forValidPictureId() {

		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		byte[] pictureBytes = this.readBytesFromFile("image/logo-hartstichting-horizontal.png");
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		entityManager.persist(picture);
		byte[] expectedPictureBytes = pictureBytes;

		// When
		byte[] actualImageBytes = pictureRepositoryImplementation.findPictureByPictureId(imageId).getPictureBytes();

		// Then
		assertArrayEquals(expectedPictureBytes, actualImageBytes);
	}

	@Test
	void should_insertPictureRecord_when_forValidPictureEntity() {

		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		byte[] pictureBytes = this.readBytesFromFile("image/logo-hartstichting-horizontal.png");
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		// When
		pictureRepositoryImplementation.storePicture(picture);

		// Then
		assertArrayEquals(expectedPicture.getPictureBytes(), entityManager.find(Picture.class, imageId).getPictureBytes());
	}

	@Test
	void should_deletePicture_forValidImageId() {

		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		byte[] pictureBytes = this.readBytesFromFile("image/logo-hartstichting-horizontal.png");
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		entityManager.persist(picture);
		byte[] expectedPictureBytes = pictureBytes;

		// When
		pictureRepositoryImplementation.deletePicture(imageId);

		// Then
		assertNull(entityManager.find(Picture.class, imageId));
	}

	public  byte[] readBytesFromFile(String path) {
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(path).toURI()));
		}catch(URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
}
