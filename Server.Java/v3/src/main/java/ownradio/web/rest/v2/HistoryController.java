package ownradio.web.rest.v2;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.Device;
import ownradio.domain.History;
import ownradio.domain.Track;
import ownradio.domain.User;
import ownradio.service.DeviceService;
import ownradio.service.HistoryService;
import ownradio.service.TrackService;
import ownradio.service.UserService;

import javax.persistence.Column;
import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.UUID;

/**
 * WEB API для работы с историей прослушанных треков
 *
 * @author Alpenov Tanat
 */
@Slf4j
@RestController
@RequestMapping("/v2/histories")
public class HistoryController {
	private final HistoryService historyService;
	private final TrackService trackService;
	private final DeviceService deviceService;

	@Autowired
	public HistoryController(HistoryService historyService, TrackService trackService, DeviceService deviceService) {
		this.historyService = historyService;
		this.trackService = trackService;
		this.deviceService = deviceService;
	}

	@RequestMapping(value = "/{deviceId}/{trackId}", method = RequestMethod.POST)
	public ResponseEntity save(@PathVariable UUID deviceId, @PathVariable UUID trackId, @RequestBody History history) {
		try {
			log.info("{} {}",deviceId.toString(),trackId.toString());
			log.info("{} {}",history.getLastListen(), history.getIsListen());
			Track track = trackService.getById(trackId);
			Device device = deviceService.getById(deviceId);

			if (track == null || device == null) {
				throw new RuntimeException("Track or Device is null");
			}

			history.setTrack(track);
			history.setDevice(device);

			historyService.save(history, true);

			return new ResponseEntity(history, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity(history, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
