package io.localmotion.picture.controller;

import io.localmotion.picture.entity.Picture;
import io.localmotion.picture.entity.PictureType;
import io.localmotion.picture.repository.PictureRepository;
import io.localmotion.picture.repository.PictureRepositoryImplementation;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.*;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest(propertySources = "application-test.yml")
class PictureControllerTest {

	@Inject
	PictureRepository pictureRepository;

	@Inject
	@Client("/")
	RxHttpClient client;

	private String accessToken;

	@BeforeEach
	void setUp() {
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("test", "password");
		HttpRequest request = HttpRequest.POST("/login", creds);
		HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
		this.accessToken = rsp.body().getAccessToken();
	}

	@Test
	void should_notUploadPicture_when_fileHasNotAllowedExtension() {
		//Given
		String pictureId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.txt";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(pictureId)
				.userId(userId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
		String pictureId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.txt.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(pictureId)
				.userId(userId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
		String pictureId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/hello.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(pictureId)
				.userId(userId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
		String pictureId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/logo-hartstichting-horizontal.png";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(pictureId)
				.userId(userId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
		String userId = UUID.randomUUID().toString();
		PictureType pictureType = PictureType.USER_PROFILE;
		final String filenname = "image/logo.jpg";
		byte[] pictureBytes = this.getBytesFromFile(filenname);
		Picture picture = Picture.builder()
				.pictureId(imageId)
				.userId(userId)
				.type(pictureType)
				.pictureBytes(pictureBytes)
				.build();
		Picture expectedPicture = picture;
		when(pictureRepository.storePicture(picture)).then(invocation -> picture);

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
	void should_getPicture_when_forValidImageId() {
		//Given
		String imageId = UUID.randomUUID().toString();
		String userId = "test";
		byte[] pictureBytes = this.getBytesFromFile("image/logo-hartstichting-horizontal.png");
		when(pictureRepository.findPictureByPictureId(imageId)).then(invocation -> {
			return Picture.builder()
					.pictureId(imageId)
					.type(PictureType.USER_PROFILE)
					.pictureBytes(pictureBytes)
					.build();
		});
		byte[] expectedPicture = pictureBytes;

		//When
		final byte[] actualPicture = client.toBlocking().retrieve(HttpRequest.GET("/api/pictures/" + imageId).bearerAuth(this.accessToken), byte[].class);

		//Then
		assertArrayEquals(expectedPicture, actualPicture);
		verify(pictureRepository).findPictureByPictureId(imageId);
	}

	@Primary
	@MockBean(PictureRepositoryImplementation.class)
	public PictureRepository pictureRepository() {
		return mock(PictureRepository.class);
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
}
