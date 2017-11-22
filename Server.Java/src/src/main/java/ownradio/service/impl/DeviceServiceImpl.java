package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ownradio.domain.Device;
import ownradio.repository.DeviceRepository;
import ownradio.service.DeviceService;

import java.util.UUID;

@Service
public class DeviceServiceImpl implements DeviceService {

	private final DeviceRepository deviceRepository;

	@Autowired
	public DeviceServiceImpl(DeviceRepository deviceRepository) {
		this.deviceRepository = deviceRepository;
	}


	@Override
	public void save(Device device) {
		deviceRepository.saveAndFlush(device);
	}

	@Override
	public Device getById(UUID uuid) {
		return deviceRepository.findOne(uuid);
	}
}
