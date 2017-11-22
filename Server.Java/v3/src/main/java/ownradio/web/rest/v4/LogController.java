package ownradio.web.rest.v4;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ownradio.util.ResourceUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by a.polunina on 16.05.2017.
 */
@Slf4j
//@CrossOrigin
@RestController
@RequestMapping("/v4/logs")

public class LogController {

	public LogController(){

	}

	@Data
	private static class LogDTO{
		private UUID deviceId;
		private MultipartFile logFile;

	}

	@RequestMapping(value = "/{deviceId}", method = RequestMethod.POST)
	public ResponseEntity<?> save(@PathVariable UUID deviceId, MultipartFile logFile) {
		if (logFile == null || logFile.isEmpty() || deviceId == null) {
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		Map<String, String> logResponse = new HashMap<>();
		try {
			String dirName = "LogFiles/" + deviceId.toString();
			String fileName = deviceId.toString() + "." + logFile.getOriginalFilename();

			String filePath = ResourceUtil.save(dirName, fileName, logFile);
			logResponse.put("result", "true");
			return new ResponseEntity<>(logResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			logResponse.put("result", "false");
			return new ResponseEntity<>(logResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
