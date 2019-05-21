package io.localmotion.picture.sanitizer;

import io.localmotion.picture.compressor.PictureCompressor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Singleton
@Slf4j
public class PictureSanitizerImplementation implements PictureSanitizer {


	@Inject
	PictureCompressor pictureCompressor;

	@Override
	public boolean trySanitize(File file) {
		boolean safeState = false;
		boolean fallbackOnApacheCommonsImaging;
		try {
			String formatName = null;
			if((file != null) && file.exists() && file.canRead() && file.canWrite()) {
				try(ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
					Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(imageInputStream);
					if ( imageReaderIterator.hasNext()) {
						ImageReader imageReader = imageReaderIterator.next();
						formatName = imageReader.getFormatName();
						fallbackOnApacheCommonsImaging = false;
					} else {
						ImageInfo imageInfo = Imaging.getImageInfo(file);
						if (imageInfo != null && imageInfo.getFormat() != null && imageInfo.getFormat().getName() != null) {
							formatName = imageInfo.getFormat().getName();
							fallbackOnApacheCommonsImaging = true;
						} else {
							throw new IOException("Format of the original image is not supported for read operation");
						}
					}
				}

				// Load the image
				BufferedImage originalImage;
				if (fallbackOnApacheCommonsImaging) {
					originalImage = Imaging.getBufferedImage(file);
				} else {
					originalImage = ImageIO.read(file);
				}

				// Check that image has been successfully loaded
				if (originalImage == null) {
					throw new IOException("Cannot load the original image");
				}

				// Get current Width and Height of the image
				int originalWidth = originalImage.getWidth(null);
				int originalHeight = originalImage.getHeight(null);


				Image resizedImage = originalImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);

				// check if the picture is scaled to origin size
				Image initialSizedImage = resizedImage.getScaledInstance(originalWidth, originalHeight, Image.SCALE_SMOOTH);

				// Save image by overwriting the provided source file content
				BufferedImage sanitizedImage = new BufferedImage(resizedImage.getWidth(null), resizedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
				Graphics graphics = sanitizedImage.getGraphics();
				graphics.drawImage(resizedImage, 0, 0, null);
				graphics.dispose();

				pictureCompressor.compress(sanitizedImage, file, formatName);
				// Set state flag
				safeState = true;
			}
		}catch(ImageReadException e) {
			safeState = false;
			log.warn("Error during Image bytes reading", e);
		}catch (IOException e) {
			safeState = false;
			log.warn("Error during Image file processing", e);
		}catch(Exception e) {
			safeState = false;
			log.warn("Error during Image file processing", e);
		}
		return safeState;
	}
}
