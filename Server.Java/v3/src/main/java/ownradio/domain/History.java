package ownradio.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Сущность для хранения информации о прослушанных треках
 *
 * @author Alpenov Tanat
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "histories")
public class History extends AbstractEntity {

	@ManyToOne
	@JoinColumn(name = "trackid")
	private Track track;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastlisten", nullable = false)
	private Calendar lastListen;

	@Column(name = "islisten", nullable = false)
	private int isListen; // 1, -1

	@ManyToOne
	@JoinColumn(name = "deviceid")
	private Device device;

	private Integer countsend;

	private String comment;
}
