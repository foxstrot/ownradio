package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ownradio.domain.History;

import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения истории прослушанных треков
 *
 * @author Alpenov Tanat
 */
public interface HistoryRepository extends JpaRepository<History, UUID> {
}
