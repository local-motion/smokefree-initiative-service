package io.localmotion.picture.repository;

import io.localmotion.picture.entity.Picture;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Singleton
@Transactional
public class PictureRepositoryImplementation implements PictureRepository {

	@PersistenceContext
	EntityManager entityManager;

	public PictureRepositoryImplementation(EntityManager entityManager) {
			this.entityManager = entityManager;
	}

	@Override
	@Transactional(readOnly = true)
	public Picture findPictureByPictureId(String pictureId) {
		return entityManager.find(Picture.class, pictureId);
	}

	@Override
	public Picture storePicture(Picture picture) {
		entityManager.persist(picture);
		return picture;
	}


	@Override
	public void deletePicture(String pictureId) {
		Picture picture = entityManager.find(Picture.class, pictureId);
		if(picture != null) {
			entityManager.remove(picture);
		}

	}

}
