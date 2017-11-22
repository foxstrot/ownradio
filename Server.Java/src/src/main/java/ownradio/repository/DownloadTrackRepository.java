package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ownradio.domain.DownloadTrack;

import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения истории скаченных треков
 *
 * @author Alpenov Tanat
 */
public interface DownloadTrackRepository extends JpaRepository<DownloadTrack, UUID> {
}
