package ownradio.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by a.polunina on 22.03.2017.
 */
@Setter
@Getter
public class UsersRating{
	private UUID userid;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
	//@Temporal(TemporalType.TIMESTAMP)
	private String reccreated;

	private String recname;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
//	@Temporal(TemporalType.TIMESTAMP)
	private String recupdated;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
	//@Temporal(TemporalType.TIMESTAMP)
	private String lastactive;

	private BigInteger owntracks;

	private BigInteger downloadtracks;
}
