package ownradio.web.rest.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.sun.deploy.net.HttpResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
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
import sun.net.www.http.HttpClient;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
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

	public static class ResponsMusic {
		private String title;
		private String artist;

		@JsonCreator
		public ResponsMusic(@JsonProperty("title") String title, @JsonProperty("artist") String artist) {
			this.title = title;
			this.artist = artist;
		}

		public ResponsMusic() {

		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String value) {
			title = value;
		}

		public String getArtist() {
			return artist;
		}

		public void setArtist(String value) {
			artist = value;
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> save(TrackDTO trackDTO) {
		Map<String, String> trackResponse = new HashMap<>();
		Log logRec = new Log();
		logRec.setRecname("Upload");
		logRec.setDeviceid(trackDTO.getDeviceId());
		logRec.setLogtext("/v4/tracks; Body: TrackidId=" + trackDTO.getFileGuid() + ", fileName=" + trackDTO.getFileName() + ", filePath=" + trackDTO.getFilePath() + ", deviceid=" + trackDTO.getDeviceId() + ", musicFile=" + trackDTO.getMusicFile().getOriginalFilename());
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

	private ResponseEntity getResponseEntity(@PathVariable UUID id, @PathVariable UUID deviceId) {
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


	private ResponsMusic getMusicInfo(File file) throws IOException {
		String attachmentName = "file";
		String attachmentFileName = file.getName();
		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		HttpURLConnection httpUrlConnection = null;
		URL url = new URL("http://localhost:12520/api/audio");
		httpUrlConnection = (HttpURLConnection) url.openConnection();
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setDoOutput(true);

		httpUrlConnection.setRequestMethod("POST");
		httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
		httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
		httpUrlConnection.setRequestProperty(
				"Content-Type", "multipart/form-data;boundary=" + boundary);

		DataOutputStream request = new DataOutputStream(
				httpUrlConnection.getOutputStream());

		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"" +
				attachmentName + "\";filename=\"" +
				attachmentFileName + "\"" + crlf);
		request.writeBytes(crlf);
		Path path = file.toPath();
		request.write(Files.readAllBytes(path));

		request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary +
				twoHyphens + crlf);

		request.flush();
		request.close();


		InputStream responseStream = new
				BufferedInputStream(httpUrlConnection.getInputStream());

		BufferedReader responseStreamReader =
				new BufferedReader(new InputStreamReader(responseStream));

		String reponse = responseStreamReader.readLine();

		ObjectMapper mapper = new ObjectMapper();
		ResponsMusic music = mapper.readValue(reponse, ResponsMusic.class);

		responseStream.close();
		httpUrlConnection.disconnect();

		return music;
	}

	@RequestMapping(value = "/{deviceId}/next", method = RequestMethod.GET)
	public ResponseEntity<?> getNextTrack(@PathVariable UUID deviceId) throws IOException {



		getMusicInfo(new File("D:\\test.mp3"));

		Map<String, String> trackResponse = new HashMap<>();
		Log logRec = new Log();
		logRec.setDeviceid(deviceId);
		logRec.setRecname("Next");
		logRec.setLogtext("/v4/tracks/" + deviceId + "/next");
		logService.save(logRec);

		NextTrack nextTrack = null;
		try {
			nextTrack = trackService.getNextTrackIdV2(deviceId);
		} catch (Exception ex) {
			log.info("{}", ex.getMessage());
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND + "; Error:" + ex.getMessage());
			logService.save(logRec);
			trackResponse.put("result", "false");
			return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
		}

			if (nextTrack != null) {
			try {
				Track track = trackRepository.findOne(nextTrack.getTrackid());

				File file = new File(track.getPath());
				if (!file.exists()) {
					track.setIsexist(0);
					trackRepository.saveAndFlush(track);
					return getNextTrack(deviceId);
				}

				if (track.getIsfilledinfo() == null || track.getIsfilledinfo() != 1)
					trackService.setTrackInfo(track.getRecid());

				if (track.getIscorrect() != null && track.getIscorrect() == 0)
					return getNextTrack(deviceId);
				if (track.getIscensorial() != null && track.getIscensorial() == 0)
					return getNextTrack(deviceId);
				if (track.getLength() != null && track.getLength() < 120)
					return getNextTrack(deviceId);

				trackResponse.put("id", nextTrack.getTrackid().toString());
				trackResponse.put("length", String.valueOf(track.getLength()));

				ResponsMusic music = null;
				if (track.getRecname() != null && !track.getRecname().isEmpty() && !track.getRecname().equals("null"))
					trackResponse.put("name", track.getRecname());
				else {
					{
						music = getMusicInfo(file);
						if (music != null && music.title != null)
							trackResponse.put("name", music.getTitle());
						else
							trackResponse.put("name", "Track");
					}

				}
				if (track.getArtist() != null && !track.getArtist().isEmpty() && !track.getArtist().equals("null"))
					trackResponse.put("artist", track.getArtist());
				else {
					if (music != null && music.artist != null)
						trackResponse.put("artist", music.getArtist());
					else
						trackResponse.put("artist", "Artist");
				}
				trackResponse.put("pathupload", track.getLocaldevicepathupload());

				trackResponse.put("timeexecute", nextTrack.getTimeexecute());
				trackResponse.put("result", "true");

				log.info("getNextTrack return {} {}", nextTrack.getMethodid(), trackResponse.get("id"));

				logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; trackid=" + trackResponse.get("id"));
				logService.save(logRec);
				return new ResponseEntity<>(trackResponse, HttpStatus.OK);
			} catch (Exception ex) {
				log.info("{}", ex.getMessage());
				logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND + "; Error:" + ex.getMessage());
				logService.save(logRec);
				trackResponse.put("result", "false");
				return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
			}
		} else {
			logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND + "; Error: Track is not found");
			logService.save(logRec);
			trackResponse.put("result", "false");
			return new ResponseEntity<>(trackResponse, HttpStatus.NOT_FOUND);
		}
	}
}
