package ownradio.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import ownradio.annotation.DisplayName;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Базовый класс для всех сущностей
 * Предназначен для хранения технической информации
 *
 * @author Alpenov Tanat
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

	@DisplayName(key = "id")
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(unique = true)
	private UUID recid;

	private String recname;

	private Date reccreated;
	private Date recupdated;

	@PrePersist
	public void beforePersist() {
		setReccreated(new Date());
	}

	@PreUpdate
	public void beforeUpdate() {
		setRecupdated(new Date());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractEntity that = (AbstractEntity) o;

		return recid != null ? recid.equals(that.recid) : that.recid == null;

	}

	@Override
	public int hashCode() {
		return recid != null ? recid.hashCode() : 0;
	}
}
