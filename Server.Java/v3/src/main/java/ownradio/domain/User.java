package ownradio.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Сущность для хранения информации о пользователе
 *
 * @author Alpenov Tanat
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AbstractEntity {
	public User(String name) {
		setRecname(name);
	}
	public Integer experience;
}
