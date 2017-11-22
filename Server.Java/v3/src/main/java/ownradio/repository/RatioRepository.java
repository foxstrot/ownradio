package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ownradio.domain.Ratio;

import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения коэффициентов схожести интересов
 *
 * Created by a.polunina on 16.02.2017.
 */
public interface RatioRepository extends JpaRepository<Ratio, UUID> {
	@Query(value = "select updateratios(?1)", nativeQuery = true)
	boolean updateRatios(UUID deviceId);

}