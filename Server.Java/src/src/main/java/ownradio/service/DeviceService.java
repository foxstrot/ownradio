package ownradio.service;

import ownradio.domain.Device;

import java.util.UUID;

/**
 * Интерфейс сервиса, для работы с девайсами
 *
 * @author Alpenov Tanat
 */
public interface DeviceService {
	void save(Device device);

	Device getById(UUID uuid);
}
