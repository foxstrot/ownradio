package ownradio.web.rest.v3;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.*;
import ownradio.repository.DownloadTrackRepository;
import ownradio.repository.TrackRepository;
import ownradio.service.LogService;
import ownradio.service.TrackService;
import ownradio.util.ResourceUtil;

import java.io.File;
import java.util.*;

/**
 * Created by a.polunina on 28.11.2016.
 */
@Slf4j
@RestController("TrackControllerV3")
@RequestMapping(value = "/v3/tracks")
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
	public ResponseEntity save(TrackDTO trackDTO) {
		Log logRec = new Log();
		logRec.setRecname("Upload");
		logRec.setDeviceid(trackDTO.getDeviceId());
		logRec.setLogtext("/v3/tracks; Body: TrackidId=" + trackDTO.getFileGuid() +", fileName=" + trackDTO.getFileName() + ", filePath=" + trackDTO.getFilePath() + ", deviceid=" + trackDTO.getDeviceId() + ", musicFile=" + trackDTO.getMusicFile().getOriginalFilename());
		logService.save(logRec);
		if (trackDTO.getMusicFile().isEmpty()) {
			logRec.setResponse("Http.Status=" + HttpStatus.BAD_REQUEST + "; File is absent");
			logService.save(logRec);
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		try {
			trackService.save(trackDTO.getTrack(), trackDTO.getMusicFile());
			trackService.setTrackInfo(trackDTO.getTrack().getRecid());
			logRec.setResponse("Http.Status=" + HttpStatus.CREATED);
			logService.save(logRec);
			return new ResponseEntity(HttpStatus.CREATED);
		} catch (Exception e) {
			logRec.setResponse("Http.Status=" + HttpStatus.INTERNAL_SERVER_ERROR + "; Error=" + e.getMessage());
			logService.save(logRec);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getTrack(@PathVariable UUID id) {
		Log logRec = new Log();
		logRec.setRecname("GetTrackdById");
		logRec.setLogtext("/v3/tracks/" + id);
		logService.save(logRec);
		Track track = trackService.getById(id);

		if (track != null) {
			byte[] bytes = ResourceUtil.read(track.getPath());
			logRec.setResponse("Http.Status=" + HttpStatus.OK + "; trackid=" + id.toString());
			logService.save(logRec);
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
		return responseHeaders;
	}

	@RequestMapping(value = "/{deviceId}/next", method = RequestMethod.GET)
	public ResponseEntity<?> getNextTrack(@PathVariable UUID deviceId) {
		Log logRec = new Log();
		logRec.setDeviceid(deviceId);
		logRec.setRecname("Next");
		logRec.setLogtext("/v3/tracks/" + deviceId + "/next");
		logService.save(logRec);

		NextTrack nextTrack = null;
		try {
			nextTrack = trackService.getNextTrackIdV2(deviceId);
		}catch (Exception ex){
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error:" + ex.getMessage());
			logService.save(logRec);
		}

		Map<String, String> trackInfo = new HashMap<>();
		if (nextTrack != null) {
			try {
				Track track = trackRepository.findOne(nextTrack.getTrackid());
				//Сохраняем информацию об отданном треке
//				Device device = new Device();
//				device.setRecid(deviceid);
//				DownloadTrack downloadTrack = new DownloadTrack();
//				downloadTrack.setTrack(track);
//				downloadTrack.setDevice(device);
//				downloadTrack.setMethodid(nextTrack.getMethodid());
//				downloadTrack.setUserrecommendid(nextTrack.getUseridrecommended());
//				downloadTrack.setTxtrecommendinfo(nextTrack.getTxtrecommendinfo());
//				downloadTrackRepository.saveAndFlush(downloadTrack);

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

				trackInfo.put("id", nextTrack.getTrackid().toString());
				trackInfo.put("length", String.valueOf(track.getLength()));
				if(track.getRecname() != null && !track.getRecname().isEmpty() && !track.getRecname().equals("null"))
					trackInfo.put("name", track.getRecname());
				else
					trackInfo.put("name", "Track");
				if(track.getArtist() != null && !track.getArtist().isEmpty() && !track.getArtist().equals("null"))
					trackInfo.put("artist", track.getArtist());
				else
					trackInfo.put("artist", "Artist");
				trackInfo.put("methodid", nextTrack.getMethodid().toString());

				log.info("getNextTrack return {} {}", nextTrack.getMethodid().toString(), trackInfo.get("id"));
				logRec.setResponse("HttpStatus=" + HttpStatus.OK +"; trackid=" + trackInfo.get("id"));
				logService.save(logRec);
				return new ResponseEntity<>(trackInfo, HttpStatus.OK);
			}catch (Exception ex){
				log.info("{}", ex.getMessage());
				logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error:" + ex.getMessage());
				logService.save(logRec);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND +"; Error: Track is not found");
			logService.save(logRec);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}