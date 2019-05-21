package io.localmotion.picture.controller;

import io.localmotion.picture.entity.Picture;
import io.localmotion.picture.entity.PictureType;
import io.localmotion.picture.repository.PictureRepository;
import io.localmotion.picture.sanitizer.PictureSanitizer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ValidationException;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller("${micronaut.context.path:}/pictures")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class PictureController {

	private static final List<String> ALLOWED_IMAGE_FORMATS = Arrays.asList(new String[] { "jpeg","jpg","tif", "tiff", "pcx", "pcc", "dcx", "bmp","gif","png","wbmp","xbm","xpm" });
	public static final int FILE_EXTENSION = 1;


	@Inject
	PictureRepository pictureRepository;

	@Inject
	PictureSanitizer pictureSanitizer;


	@Post
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public HttpResponse<String> uploadPicture(Authentication authentication, @Size(max = 1024 * 5) CompletedFileUpload profilePicture,
											  @QueryValue("type") PictureType pictureType) {

		File tmpFile = null;
		Path tmpPath = null;
		String pictureId = UUID.randomUUID().toString();
		String userId = authentication.getName();
		try {

			tmpFile = File.createTempFile("uploaded-", null);
			tmpPath = tmpFile.toPath();

			// check common validations and copy uploaded file to temporary file
			isFileSafe(profilePicture, tmpPath);

			// Initialize a sanitizer for the target file type and perform validation
			boolean isSanitized;
			isSanitized = pictureSanitizer.trySanitize(tmpFile);

			// Take decision based on safe status detected
			if (!isSanitized) {
				log.warn("Detection of a unsafe file upload or cannot sanitize uploaded document");
				// Return error
				return HttpResponse.badRequest("Cannot process file");
			} else {

				pictureRepository.storePicture(Picture.builder()
						.pictureId(pictureId).userId(userId)
						.type(pictureType)
						.pictureBytes(Files.readAllBytes(tmpPath))
						.build());

			}
		}catch(ValidationException e) {
			log.error("Validation Error: {}", e.getMessage(), e);
			return HttpResponse.badRequest( e.getMessage());
		}
		catch(Exception e) {
			log.error("Error during detection of file upload safe status !", e);
			return HttpResponse.serverError(e.getMessage());
		} finally {
			// Remove temporary file
			safelyRemoveFile(tmpPath);
		}
		return HttpResponse.ok(pictureId);

	}

	// Not clear about What data is available at front-end, this implementation can be changed accordingly
	@Get("/{imageId}")
	@Produces("image/*")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public HttpResponse<byte[]> getPicture(String imageId) {
		byte[] image = null;
		Picture picture = null;
		try {
			if(imageId != null) {
				picture = pictureRepository.findPictureByPictureId(imageId);

			}
			return  picture != null ? HttpResponse.ok(picture.getPictureBytes()).contentType("image/jpeg") : HttpResponse.notFound();
		}catch(NoResultException e) {
			return HttpResponse.notFound();
		}

	}
	/*
		Validations
	 */

	private boolean isFileSafe(CompletedFileUpload profilePicture, Path tmpPath) throws IOException {
		boolean isSafe = false;

		// check Request header Content-Type
		if(!profilePicture.getContentType().isPresent()) {
			throw new ValidationException("error: Unknown file type specified !");

		}

		// check file name and it's extension
		String[] fileNameParts = profilePicture.getFilename().split("\\.");
		if(fileNameParts.length > 2) {
			throw new ValidationException("error: More than one extensions found");
		}

		String extension = fileNameParts[FILE_EXTENSION].trim();

		if(!(ALLOWED_IMAGE_FORMATS.contains(extension.toLowerCase()))) {
			throw new ValidationException("error: format is not allowed");
		}

		// check uploaded file stream
		if((profilePicture == null) || profilePicture.getInputStream() == null) {
			throw new ValidationException("error: Unknown file content specified !");
		}

		long copiedBytesCount = Files.copy(profilePicture.getInputStream(), tmpPath, StandardCopyOption.REPLACE_EXISTING);
		if (copiedBytesCount != profilePicture.getSize()) {
			throw new IOException(String.format("Error during stream copy to temporary disk (copied: %s / expected: %s !", copiedBytesCount, profilePicture.getSize()));
		}

		return isSafe;
	}

	/*
		Utilty functions
	 */

	// It removes the temporary file created for Image processing
	private static void safelyRemoveFile(Path path) {
		try {
			if (path != null) {
				// Remove temporary file
				if (!Files.deleteIfExists(path)) {
					// If remove fail then overwrite content to sanitize it
					Files.write(path, "-".getBytes("utf8"), StandardOpenOption.CREATE);
				}
			}
		} catch (Exception e) {
			log.warn("Cannot safely remove file !", e);
		}
	}
}
