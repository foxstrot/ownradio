package ownradio.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Сущность для хранения информации об отданных пользователю треках и статистики прослушиваний по ним
 *
 * Created by a.polunina on 10.05.2017.
 */
@Setter
@Getter
public class TracksRating {
	private DownloadTrack downloadTrack;
	private Rating rating;
}
