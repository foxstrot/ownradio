package ownradio.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import ownradio.domain.Device;
import ownradio.domain.History;
import ownradio.domain.Track;
import ownradio.domain.User;

import java.util.Date;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class HistoryRepositoryTest {
	@Autowired
	private HistoryRepository historyRepository;

	@Autowired
	private TestEntityManager entityManager;

	private History history;

	@Before
	public void setUp() throws Exception {
		User user = entityManager.persist(new User());
		Device device = entityManager.persist(new Device(user, "1"));
		Track track = entityManager.persist(new Track("1", device, "1"));

		history = new History(track, new Date(), 0, "post", device);
		entityManager.persist(history);
	}

	@Test
	public void createdAt() throws Exception {
		assertThat(history.getReccreated(), not(nullValue()));
		assertThat(history.getReccreated().toString(), is(new Date().toString()));
	}

	@Test
	public void updatedAt() throws Exception {
		assertThat(history.getReccreated(), not(nullValue()));
		assertThat(history.getReccreated().toString(), is(new Date().toString()));
		assertThat(history.getRecupdated(), is(nullValue()));

		History storeHistory = historyRepository.findOne(history.getRecid());
		storeHistory.setIsListen(1);
		historyRepository.saveAndFlush(storeHistory);

		assertThat(storeHistory.getReccreated(), not(nullValue()));
		assertThat(storeHistory.getReccreated().toString(), is(history.getReccreated().toString()));

		assertThat(storeHistory.getRecupdated(), not(nullValue()));
		assertThat(storeHistory.getRecupdated().toString(), is(new Date().toString()));
	}


}