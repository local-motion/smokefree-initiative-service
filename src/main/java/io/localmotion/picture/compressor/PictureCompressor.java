package io.localmotion.picture.compressor;


import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Singleton
public class PictureCompressor {

    public static final float QUALITY = 0.2f;

    public  void compress(BufferedImage bufferedUploadedImage, File compressedFile, String formatName ) throws IOException {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);

        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");

        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(compressedFile));
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        // compress to a given quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(QUALITY);

        // appends a complete image stream containing a single image and
        // associated stream and image metadata and thumbnails to the output
        writer.write(null, new IIOImage(bufferedUploadedImage, null, null), param);

        ios.flush();
        ios.close();
        writer.dispose();

    }

}
