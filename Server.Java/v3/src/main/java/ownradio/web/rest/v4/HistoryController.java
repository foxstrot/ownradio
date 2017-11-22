package ownradio.web.rest.v4;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ownradio.domain.*;
import ownradio.service.DeviceService;
import ownradio.service.HistoryService;
import ownradio.service.LogService;
import ownradio.service.TrackService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by a.polunina on 14.03.2017.
 */
@Slf4j
//@CrossOrigin
@RestController("HistoryControllerV4")
@RequestMapping("/v4/histories")
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

	private ResponseEntity<?> getResponseEntity(@PathVariable UUID deviceId, @PathVariable UUID trackId, @RequestBody History history) {
		Log logRec = new Log();
		Map<String, String> historyResponse = new HashMap<>();
		try {
			logRec.setDeviceid(deviceId);
			logRec.setRecname("History");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//Time format UTC+0
			String currentDateTime = dateFormat.format(new Date(history.getLastListen().getTimeInMillis()));
			logRec.setLogtext("/v4/histories/" + deviceId + "/" + trackId + ". Body: History recid=" + history.getRecid() + ", islisten=" + history.getIsListen() + ", lastlisten=" + currentDateTime);
			logService.save(logRec);

			if(deviceService.getById(deviceId) == null || trackService.getById(trackId) == null) {
				logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND + "; deviceid=" + deviceId + " or trackid " + trackId + " not found");
				logService.save(logRec);
				historyResponse.put("result","false");
				return new ResponseEntity<>(historyResponse, HttpStatus.NOT_FOUND);
			}

			log.info("deviceid:{} trackid: {}",deviceId.toString(),trackId.toString());
			log.info("{} {} {} {}",history.getRecid(), history.getLastListen(), history.getIsListen());

			Track track = trackService.getById(trackId);
			Device device = deviceService.getById(deviceId);

			if (track == null || device == null) {
				throw new RuntimeException("Track or Device is null");
			}

//			if(historyService.getById(history.getRecid()) == null) {
//				History newHistory = history;
//				newHistory.setRecid(newHistory.getRecid());
			if(history.getRecid() != null){
				History historyTemp = historyService.getById(history.getRecid());
				if(historyTemp != null){
					historyTemp.setCountsend(((historyTemp.getCountsend()==null?0:historyTemp.getCountsend())) + 1);
//					historyTemp.setComment(historyTemp.getComment() + (new Date()).toString() + "; ");
					historyService.save(historyTemp, false);
					logRec.setResponse("HttpStatus=" + HttpStatus.ALREADY_REPORTED + "; recid=" + historyTemp.getRecid());
					logService.save(logRec);
					historyResponse.put("result","true");
					return new ResponseEntity<>(historyResponse, HttpStatus.ALREADY_REPORTED);
				} else {
					historyTemp = new History();
					historyTemp.setRecid(history.getRecid());
					historyTemp.setTrack(track);
					historyTemp.setDevice(device);
					historyTemp.setCountsend(1);
					historyTemp.setIsListen(history.getIsListen());
					historyTemp.setLastListen(history.getLastListen());
					historyService.save(historyTemp, true);
				}
			} else {
				history.setTrack(track);
				history.setDevice(device);
				history.setCountsend(1);
				historyService.save(history, true);
			}
				log.info("Save history, rating and update ratios");
			logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; Save history, rating and update ratios");
			logService.save(logRec);
			//}
			historyResponse.put("result","true");
			return new ResponseEntity<>(historyResponse, HttpStatus.OK);
		} catch (Exception e) {
			logRec.setResponse("HttpStatus=" + HttpStatus.INTERNAL_SERVER_ERROR + "; Error:" + e.getMessage());
			logService.save(logRec);
			historyResponse.put("result","false");
			return new ResponseEntity<>(historyResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/{deviceId}", method = RequestMethod.POST, headers = "Content-Type=application/json")
	public ResponseEntity<?> saveListHistory(@PathVariable UUID deviceId, @RequestBody HistoryArray historyDTOs) {
		Map<String, String> historyResponse = new HashMap<>();
		try{
			Log logRec = new Log();
			logRec.setDeviceid(deviceId);
			logRec.setRecname("HistoryArray");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));//Time format UTC+0
			logRec.setLogtext("/v4/histories/" + deviceId + "/array.");
			logService.save(logRec);

			Device device = deviceService.getById(deviceId);

			if(device == null || historyDTOs == null) {
				logRec.setResponse("HttpStatus=" + HttpStatus.NOT_FOUND + "; deviceid=" + deviceId  + " not found OR history is null");
				logService.save(logRec);
				historyResponse.put("result","false");
				return new ResponseEntity<>(historyResponse,HttpStatus.NOT_FOUND);
			}

//				List<History> historyList = new ArrayList<>();
//
//			for(HistoryDTO historyDTO: historyDTOs.getHistoryList()) {
//				historyList.add(historyDTO.getHistory());
//			}
//
//
//
//			historyService.saveHistory(historyList);
//			return new ResponseEntity(HttpStatus.OK);
			for(HistoryDTO historyDTO: historyDTOs.getHistoryList()) {
				History history = historyDTO.getHistory();

				if(history != null) {


					if (history.getRecid() != null) {
						History historyTemp = historyService.getById(history.getRecid());
						if (historyTemp != null) {
							historyTemp.setCountsend(((historyTemp.getCountsend() == null ? 0 : historyTemp.getCountsend())) + 1);
							historyTemp.setDevice(device);
							historyService.save(historyTemp, false);
							logRec.setResponse("HttpStatus=" + HttpStatus.ALREADY_REPORTED + "; recid=" + historyTemp.getRecid());
							logService.save(logRec);
						} else {
							historyTemp = history;
							historyTemp.setDevice(device);
							historyTemp.setRecid(history.getRecid());
							historyTemp.setCountsend(1);
							historyService.save(historyTemp, true);
						}
					} else {
						history.setCountsend(1);
						history.setDevice(device);
						historyService.save(history, true);
					}
					log.info("Save history, rating and update ratios");
					logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; Save history, rating and update ratios");
					logService.save(logRec);
				}
			}
			historyResponse.put("result","true");
			return new ResponseEntity<>(historyResponse, HttpStatus.OK);
		}catch (Exception ex){
			historyResponse.put("result","false");
			return new ResponseEntity<>(historyResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}