package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ownradio.domain.Device;

import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения девайсов
 *
 * @author Alpenov Tanat
 */
public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
