package ownradio.web.rest.v4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ownradio.domain.Log;
import ownradio.repository.DeviceRepository;
import ownradio.service.LogService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by a.polunina on 03.04.2017.
 */
@Slf4j
//@CrossOrigin
@RestController
@RequestMapping("/v4/devices")
public class DeviceController {
	private final DeviceRepository deviceRepository;
	private final LogService logService;
	@Autowired
	public DeviceController(DeviceRepository deviceRepository, LogService logService) {
		this.deviceRepository = deviceRepository;
		this.logService = logService;
	}

	@RequestMapping(value = "/{deviceId}/registerdevice", method = RequestMethod.GET)
		public ResponseEntity<?> registerDevice(@PathVariable UUID deviceId){
		return getResponseEntityRegisterDevice(deviceId, null);
	}

	@RequestMapping(value = "/{deviceId}/{deviceName}/registerdevice", method = RequestMethod.GET)
	public ResponseEntity<?> registerDevice(@PathVariable UUID deviceId, @PathVariable String deviceName){
		return getResponseEntityRegisterDevice(deviceId, deviceName);
	}

	private ResponseEntity<?> getResponseEntityRegisterDevice(UUID deviceId, String deviceName) {
		Map<String, String> deviceResponse = new HashMap<>();
		Log logRec = new Log();
		logRec.setDeviceid(deviceId);
		logRec.setRecname("RegisterDevice");
		logRec.setLogtext("/v4/devices/" + deviceId + "/" + deviceName + "/registerdevice");
		logService.save(logRec);
		if(deviceName == null)
			deviceName = "New unknown device";
		deviceRepository.registerdevice(deviceId, deviceName);
		logRec.setResponse("HttpStatus=" + HttpStatus.OK + "; deviceid=" + deviceId);
		logService.save(logRec);
		deviceResponse.put("result", "true");
		return new ResponseEntity<>(deviceResponse, HttpStatus.OK);
	}
}
