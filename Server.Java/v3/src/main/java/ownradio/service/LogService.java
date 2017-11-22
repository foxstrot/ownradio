package ownradio.service;

import ownradio.domain.Log;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с логгированием
 *
 * Created by a.polunina on 16.05.2017.
 */
public interface LogService {
	Log save(Log log);

	List<Log> getByDeviceId(UUID deviceId);
}
