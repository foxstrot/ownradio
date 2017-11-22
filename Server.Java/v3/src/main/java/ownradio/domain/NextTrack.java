package ownradio.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.UUID;

/**
 * Created by a.polunina on 29.11.2016.
 */
@Getter
@Setter
public class NextTrack {
	private UUID trackid;
	private Integer methodid;
	private UUID useridrecommended;
	private String txtrecommendinfo;
	private String timeexecute;
}
