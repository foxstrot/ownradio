package ownradio.service;

import ownradio.domain.DownloadTrack;
import ownradio.domain.TracksHistory;
import ownradio.domain.TracksRating;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с историей скаченных треков
 *
 * @author Alpenov Tanat
 */
public interface DownloadTrackService {
	void save(DownloadTrack downloadTrack);

	List<DownloadTrack> getLastTracksByDevice(UUID deviceId, Integer countTracks);

	List<TracksHistory> getTracksHistoryByDevice(UUID deviceId, Integer countRows);

	List<TracksRating> getTracksRatingByDevice(UUID deviceId, Integer countRows);
}
