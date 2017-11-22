package ownradio.domain;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * Сущность для хранения информации об отданных пользователю треках и истории по ним
 *
 * Created by a.polunina on 07.04.2017.
 */
@Setter
@Getter
public class TracksHistory {
	private DownloadTrack downloadTrack;
	private History history;

}
