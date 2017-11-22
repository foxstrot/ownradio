package ownradio.web.rest.v4;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.Device;
import ownradio.domain.Log;
import ownradio.domain.NextTrack;
import ownradio.domain.Track;
import ownradio.repository.DownloadTrackRepository;
import ownradio.repository.TrackRepository;
import ownradio.service.LogService;
import ownradio.service.TrackService;
import ownradio.util.ResourceUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by a.polunina on 14.03.2017.
 */
@Slf4j
//@CrossOrigin
@RestController("TrackControllerV4")
@RequestMapping(value = "/v4/tracks")
public class TrackController {

	private final TrackService trackService;
	private final TrackRepository trackRepository;
	private final DownloadTrackRepository downloadTrackRepository;
	private final LogService logService;

	@Autowired
	public TrackController(TrackService trackService, TrackRepository trackRepository, DownloadTrackRepository downloadTrackRepository, LogService logService) {
		this.trackService = trackService;
		this.trackRepository = trackRepository;
		this.downloadTrackRepository = downloadTrackRepository;
		this.logService = logService;
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
			track.setDevice(device);
			track.setPath("---");
			track.setLocaldevicepathupload(filePath);

			return track;
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> save(TrackDTO trackDTO) {
		Map<String, String> trackResponse = new HashMap<>();
		Log logRec = new Log();
		logRec.setRecname("Upload");
		logRec.setDeviceid(trackDTO.getDeviceId());
		logRec.setLogtext("/v4/tracks; Body: TrackidId=" + trackDTO.getFileGuid() +", fileName=" + trackDTO.getFileName() + ", filePath=" + trackDTO.getFilePath() + ", deviceid=" + trackDTO.getDeviceId() + ", musicFile=" + trackDTO.getMusicFile().getOriginalFilename());
		logService.save(logRec);

		if (trackDTO.getMusicFile().isEmpty()) {
			logRec.setResponse("Http.Status=" + HttpStatus.BAD_REQUEST + "; File is absent");
			logService.save(logRec);
			trackResponse.put("result", "false");
			return new ResponseEntity<>(trackResponse, HttpStatus.BAD_REQUEST);
		}

		try {
			trackService.save(trackDTO.getTrack(), trackDTO.getMusicFile());
			trackService.setTrackInfo(trackDTO.getTrack().getRecid());
			logRec.setResponse("Http.Status=" + HttpStatus.CREATED);
			logService.save(logRec);
			trackResponse.put("result", "true");
			return new ResponseEntity<>(trackResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			logRec.setResponse("Http.Status=" + HttpStatus.INTERNAL_SERVER_ERROR + "; Error=" + e.getMessage());
			logService.save(logRec);
			trackResponse.put("result", "false");
			return new ResponseEntity<>(trackResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getTrack(@PathVariable UUID id) {
		return getResponseEntity(id, null);
	}


	@RequestMapping(value = "/{id}/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<?> getTrackWithDeviceId(@PathVariable UUID id, @PathVariable UUID deviceId) {
		return getResponseEntity(id, deviceId);
	}

	private ResponseEntity getResponseEntity(@PathVariable UUID id, @PathVariable UUID deviceId){
		Log logRec = new Log();
		logRec.setRecname("GetTrackdById");
		logRec.setDeviceid(deviceId);
		logRec.setLogtext("/v4/tracks/" + id + "/" + deviceId);
		logService.save(logRec);
		Track track = trackService.getById(id);

		if (track != null) {
			log.info("Начинаем читать файл в массив байт");
			byte[] bytes = ResourceUtil.read(track.getPath());
			logRec.setResponse("Http.Status=" + HttpStatus.OK + "; trackid=" + id.toString());
			logService.save(logRec);
			log.info("Возвращаем файл и код ответа");
			return new ResponseEntity<>(bytes, getHttpAudioHeaders(), HttpStatus.OK);
		} else {
			logRec.setResponse("Http.Status=" + HttpStatus.NOT_FOUND + "; trackid=" + id.toString());
			logService.save(logRec);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	private HttpHeaders getHttpAudioHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "audio/mpeg");
		log.info("Формируем заголовки (getHttpAudioHeaders)");
		return responseHeaders;
	}

	@RequestMapping(value = "/{deviceId}/next", method = RequestMethod.GET)
	public ResponseEntity<?> getNextTrack(@PathVariable UUID deviceId) {
		Map<String, String> trackResponse = new HashMap<>();
		Log logRec = new Log();
		logRec.setDeviceid(deviceId);
		logRec.setRecname("Next");
		logRec.setLogtext("/v4/tracks/" + deviceId + "/next");
		logService.save(logRec);

		NextTrack nextTrack = null;
		try {
			nextTrack = trackService.getNextTrackIdV2(deviceId);
		}catch (Exception ex){
			log.info("{}", ex.getMessage());
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error:" + ex.getMessage());
			logService.save(logRec);
			trackResponse.put("result","false");
			return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
		}

		if (nextTrack != null) {
			try {
				Track track = trackRepository.findOne(nextTrack.getTrackid());

				File file = new File(track.getPath());
				if(!file.exists()){
					track.setIsexist(0);
					trackRepository.saveAndFlush(track);
					return getNextTrack(deviceId);
				}

				if(track.getIsfilledinfo() == null || track.getIsfilledinfo() != 1)
					trackService.setTrackInfo(track.getRecid());

				if(track.getIscorrect() != null && track.getIscorrect() == 0)
					return getNextTrack(deviceId);
				if(track.getIscensorial() != null && track.getIscensorial() == 0)
					return getNextTrack(deviceId);
				if(track.getLength() != null && track.getLength() <  120)
					return getNextTrack(deviceId);

				trackResponse.put("id", nextTrack.getTrackid().toString());
				trackResponse.put("length", String.valueOf(track.getLength()));
				if(track.getRecname() != null && !track.getRecname().isEmpty() && !track.getRecname().equals("null"))
					trackResponse.put("name", track.getRecname());
				else
					trackResponse.put("name", "Track");
				if(track.getArtist() != null && !track.getArtist().isEmpty() && !track.getArtist().equals("null"))
					trackResponse.put("artist", track.getArtist());
				else
					trackResponse.put("artist", "Artist");
				trackResponse.put("pathupload", track.getLocaldevicepathupload());

				trackResponse.put("timeexecute", nextTrack.getTimeexecute());
				trackResponse.put("result","true");

				log.info("getNextTrack return {} {}", nextTrack.getMethodid(), trackResponse.get("id"));

				logRec.setResponse("HttpStatus=" + HttpStatus.OK +"; trackid=" + trackResponse.get("id"));
				logService.save(logRec);
				return new ResponseEntity<>(trackResponse, HttpStatus.OK);
			}catch (Exception ex){
				log.info("{}", ex.getMessage());
				logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error:" + ex.getMessage());
				logService.save(logRec);
				trackResponse.put("result","false");
				return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
			}
		} else {
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error: Track is not found");
			logService.save(logRec);
			trackResponse.put("result","false");
			return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
		}
	}
}
