package io.localmotion.picture.compressor;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class PictureCompressorTest {

    @Inject
    PictureCompressor pictureCompressor;

    @Test
    void compress() throws Exception {
        File inputFile = new File(getClass().getClassLoader().getResource("image/logo.jpg").toURI());
        File compressedImageFile = File.createTempFile("compressed-", "jpg");
        pictureCompressor.compress(ImageIO.read(inputFile), compressedImageFile,"jpg");
        assertTrue( compressedImageFile.length() < inputFile.length());
        assertTrue(compressedImageFile.delete());
    }
}