package ownradio.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import ownradio.domain.Device;
import ownradio.domain.Track;
import ownradio.domain.User;
import ownradio.repository.TrackRepository;
import ownradio.service.impl.TrackServiceImpl;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static ownradio.util.ResourceUtil.UPLOAD_DIR;

@RunWith(SpringRunner.class)
public class TrackServiceTest {
	@Mock
	private TrackRepository trackRepository;

	protected TrackService trackService;

	private UUID userId = UUID.randomUUID();
	private UUID trackId = UUID.randomUUID();
	private UUID deviceId = UUID.randomUUID();
	private Track expected;

	@Before
	public void setUp() throws Exception {
		trackService = new TrackServiceImpl(trackRepository);
		expected = new Track();
		expected.setRecid(trackId);

		User user = new User();
		user.setRecid(userId);

		Device device = new Device(user, "123");
		device.setRecid(deviceId);
		expected.setDevice(device);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(new File(UPLOAD_DIR));
	}

	@Test
	public void getNextTrackId() throws Exception {
		given(this.trackRepository.getNextTrackId(trackId)).willReturn(trackId);

		UUID actual = trackService.getNextTrackId(trackId);

		assertThat(actual, equalTo(expected.getRecid()));
	}

	@Test
	public void save() throws Exception {
		MockMultipartFile correctFile = new MockMultipartFile("file", "test.mp3", "text/plain", "Text".getBytes());

		given(this.trackRepository.registerTrack(expected.getRecid(), expected.getLocaldevicepathupload(), expected.getPath(), expected.getDevice().getRecid())).willReturn(true);
		given(this.trackRepository.findOne(expected.getRecid())).willReturn(expected);
		trackService.save(expected, correctFile);

		assertThat(new File(expected.getPath()).exists(), is(true));
	}
}