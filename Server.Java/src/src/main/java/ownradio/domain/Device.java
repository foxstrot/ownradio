package ownradio.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Сущность для хранения информации о девайсе
 *
 * @author Alpenov Tanat
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class Device extends AbstractEntity {
	@ManyToOne
	@JoinColumn(name = "userid")
	private User user;

//	private String name;
	public Device (User user, String name){
		setRecname(name);
		setUser(user);
	}
}
