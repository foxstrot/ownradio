package ownradio.web.rest.v3;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ownradio.domain.Device;
import ownradio.domain.History;
import ownradio.domain.Log;
import ownradio.domain.Track;
import ownradio.service.DeviceService;
import ownradio.service.HistoryService;
import ownradio.service.LogService;
import ownradio.service.TrackService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by a.polunina on 28.11.2016.
 */
@Slf4j
@RestController("HistoryControllerV3")
@RequestMapping("/v3/histories")
public class HistoryController {
	private final HistoryService historyService;
	private final TrackService trackService;
	private final DeviceService deviceService;
	private final LogService logService;

	@Autowired
	public HistoryController(HistoryService historyService, TrackService trackService, DeviceService deviceService, LogService logService) {
		this.historyService = historyService;
		this.trackService = trackService;
		this.deviceService = deviceService;
		this.logService = logService;
	}

	@Data
	private static class HistoryDTO {
		private UUID deviceId;
		private UUID trackId;
		private String lastListen;
		private int isListen; // 1, -1

		public History getHistory() {
			Calendar calendar;
			try {
				calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'H:m:s");
				calendar.setTime( sdf.parse(lastListen));
			}catch (Exception ex){
				calendar = null;
			}

			History history = new History();
			history.setLastListen(calendar);
			history.setIsListen(isListen);

			return history;
		}
	}

	//form-data
	@RequestMapping(value = "/{deviceId}/{trackId}", method = RequestMethod.POST)
	public ResponseEntity save(@PathVariable UUID deviceId, @PathVariable UUID trackId, HistoryDTO historyDTO) {
		return getResponseEntity(deviceId, trackId, historyDTO.getHistory());
	}

	//json
	@RequestMapping(value = "/{deviceId}/{trackId}", method = RequestMethod.POST, headers = "Content-Type=application/json")
	public ResponseEntity save2(@PathVariable UUID deviceId, @PathVariable UUID trackId, @RequestBody History history) {
		return getResponseEntity(deviceId, trackId, history);
	}

	private ResponseEntity getResponseEntity(@PathVariable UUID deviceId, @PathVariable UUID trackId, @RequestBody History history) {
		Log logRec = new Log();
		try {
			logRec.setDeviceid(deviceId);
			logRec.setRecname("History");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//Time format UTC+0
			String currentDateTime = dateFormat.format(new Date(history.getLastListen().getTimeInMillis()));
			logRec.setLogtext("/v3/histories/" + deviceId + "/" + trackId + ". Body: islisten=" + history.getIsListen() + ", lastlisten=" + currentDateTime);
			logService.save(logRec);

			if (deviceService.getById(deviceId) == null || trackService.getById(trackId) == null) {
				logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; deviceid=" + deviceId + " or trackid " + trackId + " not found");
				logService.save(logRec);
				return new ResponseEntity(HttpStatus.OK);
			}

			log.info("deviceid:{} trackid: {}",deviceId.toString(),trackId.toString());
			log.info("{} {} {}",history.getLastListen(), history.getIsListen());
			Track track = trackService.getById(trackId);
			Device device = deviceService.getById(deviceId);

			if (track == null || device == null) {
				throw new RuntimeException("Track or Device is null");
			}

			history.setTrack(track);
			history.setDevice(device);

			historyService.save(history, true);
			log.info("Save history, rating and update ratios");
			logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; Save history, rating and update ratios");
			logService.save(logRec);
			return new ResponseEntity(HttpStatus.OK);
		} catch (Exception e) {
			logRec.setResponse("HttpStatus=" + HttpStatus.INTERNAL_SERVER_ERROR + "; Error:" + e.getMessage());
			logService.save(logRec);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
