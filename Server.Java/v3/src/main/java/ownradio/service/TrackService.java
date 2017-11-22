package ownradio.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.NextTrack;
import ownradio.domain.Track;
import ownradio.domain.UploadersRating;
import ownradio.domain.User;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с треками
 *
 * @author Alpenov Tanat
 */
public interface TrackService {

	Track getById(UUID id);

	List<Track> getByDeviceId(UUID id, Pageable pageable);

	UUID getNextTrackId(UUID deviceId);

	NextTrack getNextTrackIdV2(UUID deviceId);

	void save(Track track, MultipartFile file);

	void setTrackInfo(UUID trackid);

	List<UploadersRating> getUploadersRating();
}
