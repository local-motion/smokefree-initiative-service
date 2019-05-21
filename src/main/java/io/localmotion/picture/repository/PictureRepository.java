package io.localmotion.picture.repository;

import io.localmotion.picture.entity.Picture;

import java.util.List;

public interface PictureRepository {

	Picture findPictureByPictureId(String  pictureId);

	Picture storePicture(Picture picture);

	void deletePicture(String pictureId);

}
