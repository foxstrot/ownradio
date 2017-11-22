package ownradio.domain;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by a.polunina on 25.05.2017.
 */

@Setter
@Getter
public class HistoryDTO {
	private UUID recid;
	private UUID trackid;
	private String lastlisten;
	private int islisten; // 1, -1

	public History getHistory() {
		Calendar calendar;
		try {
			calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'H:m:s");
			calendar.setTime( sdf.parse(lastlisten));
		}catch (Exception ex){
			calendar = null;
		}

		Track track = new Track();
		try {
			track.setRecid(trackid);
		}catch (Exception ex){
			return null;
		}

		History history = new History();
		if(recid != null)
			history.setRecid(recid);
		history.setTrack(track);
		history.setLastListen(calendar);
		history.setIsListen(islisten);

		return history;
	}
}
