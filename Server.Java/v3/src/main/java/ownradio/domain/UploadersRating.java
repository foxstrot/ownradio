package ownradio.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by a.polunina on 27.06.2017.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadersRating {
	private User user;
//	private UUID user;
//	private String recname;
	private BigInteger uploadtracks;

	@DateTimeFormat(pattern = "dd-MM-yyyy'T'H:m:s")
	private String lastactive;


}
