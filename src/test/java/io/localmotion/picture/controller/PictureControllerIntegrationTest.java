package io.localmotion.picture.controller;

import io.localmotion.picture.entity.Picture;
import io.localmotion.picture.entity.PictureType;
import io.localmotion.picture.repository.PictureRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/*
 End to End testing for profile picture upload
 */
@MicronautTest
class PictureControllerIntegrationTest {

	private String accessToken;

	@Inject
	@Client("/")
	RxHttpClient client;

	@Inject
	PictureRepository pictureRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	PlatformTransactionManager platformTransactionManager;

	@BeforeEach
	void setUp() {

		// Delete all rows
		TransactionStatus tx = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

		//Clear table before each test case starts
		CriteriaDelete<Picture> deletePicture =  entityManager.getCriteriaBuilder().createCriteriaDelete(Picture.class);
		deletePicture.from(Picture.class);
		entityManager.createQuery(deletePicture).executeUpdate();
		platformTransactionManager.commit(tx);
		//pictureRepository.deleteAll();
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("test", "password");
		HttpRequest request = HttpRequest.POST("/login", creds);
		HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
		this.accessToken = rsp.body().getAccessToken();

	}

	@Test
	void should_notUploadPicture_when_fileHasNotAllowedExtension() {
		//Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.txt";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		//When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();

		final HttpResponse<String> actualResponse;
		HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> {
			client.toBlocking()
					.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
							.contentType(MediaType.MULTIPART_FORM_DATA)
							.bearerAuth(this.accessToken), String.class);
		});


		//Then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		assertTrue(exception.getResponse().getBody().get().equals("error: format is not allowed"));
	}

	@Test
	void should_notUploadPicture_when_fileHasMoreThanOneExtensions() {

		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.txt.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		// When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();

		final HttpResponse<String> actualResponse;
		HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> {
			client.toBlocking()
					.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
							.contentType(MediaType.MULTIPART_FORM_DATA)
							.bearerAuth(this.accessToken), String.class);
		});

		//Then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		assertTrue(exception.getResponse().getBody().get().equals("error: More than one extensions found"));
	}

	@Test
	void should_notUploadPicture_when_textFileExtensionModifiedToPNG() {
		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		// When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();

		final HttpResponse<String> actualResponse;
		HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> {
			client.toBlocking()
					.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
							.contentType(MediaType.MULTIPART_FORM_DATA)
							.bearerAuth(this.accessToken), String.class);
		});

		//Then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		assertTrue(exception.getResponse().getBody().get().equals("Cannot process file"));
	}

	@Disabled
	@Test
	void should_notUploadPicture_when_fileNameDoesNotExist() {
		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = "test";
		//String id = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		// When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();

		final HttpResponse<String> actualResponse;
		HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> {
			client.toBlocking()
					.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
							.contentType(MediaType.MULTIPART_FORM_DATA)
							.bearerAuth(this.accessToken), String.class);
		});

		//Then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		assertTrue(exception.getResponse().getBody().get().equals("error: format is not allowed"));
	}

	@Test
	void should_uploadPicture_when_forValidPngFile() {
		//Given
		String imageId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/logo-hartstichting-horizontal.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		//When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();
		HttpResponse<String> actualResponse =  client.toBlocking()
				.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.bearerAuth(this.accessToken), String.class);

		//Then
		assertEquals(HttpStatus.OK, actualResponse.getStatus());
		assertTrue(actualResponse.getBody().isPresent());
	}

	@Test
	void should_uploadPicture_when_forValidJpegFile() {
		//Given
		String imageId = UUID.randomUUID().toString();
		String userId = "test";
		//String id = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/logo.jpg";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;

		//When
		MultipartBody multiPartBody = MultipartBody.builder()
				.addPart("profilePicture",filenname, picture.getPictureBytes())
				.build();
		HttpResponse<String> actualResponse =  client.toBlocking()
				.exchange(HttpRequest.POST("/api/pictures?type=" + picture.getType() , multiPartBody)
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.bearerAuth(this.accessToken), String.class);

		//Then
		//verify(pictureRepository).uploadPicture(picture);
		assertEquals(HttpStatus.OK, actualResponse.getStatus());
		assertTrue(actualResponse.getBody().isPresent());
	}



	@Test
	void should_getPicture_when_forValidImageId() {

		// Given
		String imageId = UUID.randomUUID().toString();
		String userId = "test";

		PictureType pictureType = PictureType.USER_PROFILE;
		String filename = "image/logo-hartstichting-horizontal.png";
		byte[] pictureBytes = this.getBytesFromFile(filename);
		Picture uploadPicture = Picture.builder()
				.pictureId(imageId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		insertPicture(uploadPicture);
		//pictureRepository.uploadPicture(uploadPicture);
		//entityManager.persist(uploadPicture);

		// When
		HttpResponse<byte[]> actualPicture = client.toBlocking()
				.exchange(HttpRequest.GET("/api/pictures/"+ imageId).bearerAuth(this.accessToken), byte[].class);
		// Then
		assertEquals(HttpStatus.OK, actualPicture.getStatus());
		assertArrayEquals(uploadPicture.getPictureBytes(), actualPicture.getBody().get());
	}

	public  byte[] getBytesFromFile(String path) {
		byte[] result = null;
		try {
			this.getClass().getClassLoader().getResource(path);
			result = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(path).toURI()));
		}catch(URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void insertPicture(Picture picture) {
		EntityTransaction tx = entityManager.getTransaction();
		entityManager.persist(picture);
		tx.commit();
	}

}
