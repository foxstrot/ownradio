package ownradio.web.rest.v2;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.Device;
import ownradio.domain.Track;
import ownradio.service.TrackService;
import ownradio.util.ResourceUtil;

import java.util.UUID;

/**
 * WEB API для работы с треками
 *
 * @author Alpenov Tanat
 */
@Slf4j
@RestController
@RequestMapping(value = "/v2/tracks")
public class TrackController {

	private final TrackService trackService;

	@Autowired
	public TrackController(TrackService trackService) {
		this.trackService = trackService;
	}

	@Data
	private static class TrackDTO {
		private UUID fileGuid;
		private String fileName;
		private String filePath;
		private UUID deviceId;
		private MultipartFile musicFile;

		public Track getTrack() {
			Device device = new Device();
			device.setRecid(deviceId);

			Track track = new Track();
			track.setRecid(fileGuid);
			track.setRecname(fileName);
			track.setDevice(device);
			track.setPath("---");
			track.setLocaldevicepathupload(filePath);

			return track;
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity save(TrackDTO trackDTO) {
		if (trackDTO.getMusicFile().isEmpty()) {
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		try {
			trackService.save(trackDTO.getTrack(), trackDTO.getMusicFile());

			return new ResponseEntity(HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getTrack(@PathVariable UUID id) {
		Track track = trackService.getById(id);

		if (track != null) {
			byte[] bytes = ResourceUtil.read(track.getPath());
			return new ResponseEntity<>(bytes, getHttpAudioHeaders(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/{deviceId}/next", method = RequestMethod.GET)
	public ResponseEntity<?> getNextTrackId(@PathVariable UUID deviceId) {
		UUID trackId = trackService.getNextTrackId(deviceId);

		if (trackId != null) {
			return new ResponseEntity<>(trackId, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private HttpHeaders getHttpAudioHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "audio/mpeg");
		return responseHeaders;
	}
}
