package ownradio.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by a.polunina on 16.05.2017.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logs")
public class Log extends AbstractEntity {
	@Column(columnDefinition="TEXT")
	private String logtext;
	@Type(type="pg-uuid")
	@Column(columnDefinition = "uuid")
	private UUID deviceid;
	@Column(columnDefinition="TEXT")
	private String response;
}
