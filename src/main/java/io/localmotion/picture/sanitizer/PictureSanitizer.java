package io.localmotion.picture.sanitizer;

import java.io.File;

public interface PictureSanitizer {
	boolean trySanitize(File file);
}
