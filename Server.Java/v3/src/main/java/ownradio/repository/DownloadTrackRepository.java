package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ownradio.domain.Device;
import ownradio.domain.DownloadTrack;
import ownradio.domain.Track;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения истории скаченных треков
 *
 * @author Alpenov Tanat
 */
public interface DownloadTrackRepository extends JpaRepository<DownloadTrack, UUID> {

	DownloadTrack findFirstByDeviceAndTrackOrderByReccreatedAsc(Device device, Track track);

	@Query(value = "select * from getlasttracks(?1, ?2)", nativeQuery = true)
	List<DownloadTrack> getLastTracksByDevice(UUID deviceid, Integer countTracks);

	@Query(value = "select * from getTracksHistoryByDevice(?1, ?2)", nativeQuery = true)
	List<Object[]> getTracksHistoryByDevice(UUID deviceId, Integer countRows);

	@Query(value = "select * from getTracksRatingByDevice(?1, ?2)", nativeQuery = true)
	List<Object[]> getTracksHistoryWithRatingByDevice(UUID deviceId, Integer countRows);

}
