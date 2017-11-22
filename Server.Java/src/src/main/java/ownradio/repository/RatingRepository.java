package ownradio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ownradio.domain.Rating;
import ownradio.domain.Track;
import ownradio.domain.User;

import java.util.Date;
import java.util.UUID;

/**
 * Интерфейс репозитория, для хранения рейтингов прослушанных треков
 *
 * @author Alpenov Tanat
 */
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Rating findByUser(User user);

    Rating findByUserAndTrack(User user, Track track);
}
