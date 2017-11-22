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
 * Сущность для хранения информации о рейтингах треков
 *
 * @author Alpenov Tanat
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ratings")
public class Rating extends AbstractEntity {
	@ManyToOne
	@JoinColumn(name = "userid")
	private User user;

	@ManyToOne
	@JoinColumn(name = "trackid")
	private Track track;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar lastlisten;

	@Column(nullable = false)
	private Integer ratingsum;
}
