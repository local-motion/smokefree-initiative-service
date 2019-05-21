package io.localmotion.picture.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table
public class Picture {

	@Id
	private String pictureId;

	private String userId;

	private PictureType type;

	@Lob
	private byte[] pictureBytes;

	private final Date createdTime = new Date();

	private Date DeletedTime;

	private boolean inUse;

}
