package ownradio.service;

import ownradio.domain.DownloadTrack;

/**
 * Интерфейс сервиса, для работы с историей скаченных треков
 *
 * @author Alpenov Tanat
 */
public interface DownloadTrackService {
	void save(DownloadTrack downloadTrack);
}
