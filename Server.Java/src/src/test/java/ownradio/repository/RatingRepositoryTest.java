package ownradio.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ownradio.domain.Rating;
import ownradio.domain.Track;
import ownradio.domain.User;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by a.polunina on 10.11.2016.
 */
@ActiveProfiles("prod")
@RunWith(SpringRunner.class)
@SpringBootTest
//@DataJpaTest
public class RatingRepositoryTest {
	@Autowired
	private RatingRepository ratingRepository;
	@Test
	public void findByUser() throws Exception {
		User user = new User();
		user.setRecid(UUID.fromString("bfa8137b-c917-4496-8fe7-39202322d257"));
		Rating rating = ratingRepository.findByUser(user);
		System.out.println(rating);
	}

	@Test
	public void findByUserAndTrack() throws Exception {
		User user = new User();
		Track track = new Track();
		user.setRecid(UUID.fromString("bfa8137b-c917-4496-8fe7-39202322d257"));
		track.setRecid(UUID.fromString("bfa8137b-c917-4496-8fe7-39202322d257"));
		Rating rating = ratingRepository.findByUserAndTrack(user, track);
		System.out.println(rating);
	}
}