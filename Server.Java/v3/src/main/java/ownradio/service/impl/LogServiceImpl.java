package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ownradio.domain.Log;
import ownradio.repository.LogRepository;
import ownradio.service.LogService;

import java.util.List;
import java.util.UUID;

/**
 * Created by a.polunina on 16.05.2017.
 */
@Service
public class LogServiceImpl implements LogService {
	@Autowired
	private LogRepository logRepository;

	@Override
	public Log save(Log log){ return logRepository.saveAndFlush(log);}

	@Override
	public List<Log> getByDeviceId(UUID deviceId) {
		return logRepository.findAllByDeviceidOrderByReccreatedDesc(deviceId);
	}
}
