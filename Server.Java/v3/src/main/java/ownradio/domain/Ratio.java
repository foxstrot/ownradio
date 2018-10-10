package ownradio.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * Сущность для хранения коэффициентов схожести интересов
 *
 * Created by a.polunina on 16.02.2017.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ratios")
public class Ratio extends AbstractEntity {
//	@ManyToOne
	@JoinColumn(name = "userid")
	@Type(type="pg-uuid")
	@Column(nullable = false, columnDefinition = "uuid")
	private User userid1;

//	@ManyToOne
	@JoinColumn(name = "userid")
	@Type(type="pg-uuid")
	@Column(nullable = false, columnDefinition = "uuid")

	private User userid2;

	private Integer ratio;
}
