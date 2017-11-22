package ownradio.web.rest.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ownradio.domain.Device;
import ownradio.domain.History;
import ownradio.domain.Track;
import ownradio.domain.User;
import ownradio.service.DeviceService;
import ownradio.service.HistoryService;
import ownradio.service.TrackService;
import ownradio.service.UserService;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HistoryController.class)
public class HistoryControllerTest {
	public static final UUID TRACK_UUID = UUID.randomUUID();
	public static final UUID USER_UUID = UUID.randomUUID();
	public static final UUID DEVICE_UUID = UUID.randomUUID();

	@MockBean
	private HistoryService historyService;

	@MockBean
	private UserService userService;

	@MockBean
	private TrackService trackService;

	@MockBean
	private DeviceService deviceService;

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper mapper = new ObjectMapper();

	private User user;
	private Track track;
	private Device device;

	@Before
	public void setUp() throws Exception {
		user = new User();
		track = new Track();
		device = new Device();
	}

	@Test
	public void saveStatusIsOk() throws Exception {
		given(this.userService.getById(USER_UUID)).willReturn(user);
		given(this.trackService.getById(TRACK_UUID)).willReturn(track);
		given(this.deviceService.getById(DEVICE_UUID)).willReturn(device);

		JSONObject obj = new JSONObject();
		obj.put("lastlisten", "2016-11-28T12:34:56");
		obj.put("islisten", "1");
		obj.put("method", "method");

		mockMvc.perform(post("/v2/histories/{deviceid}/{trackid}", DEVICE_UUID, TRACK_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(obj.toString())
//				.param("lastlisten", "2016-11-28 12:34:56")
//				.param("islisten", "1")
//				.param("method", "method")
		)
				.andDo(print())
				.andExpect(
						status().isOk()
				);
	}

	@Test
	public void saveStatusIsInternalServerError() throws Exception {
		given(this.userService.getById(USER_UUID)).willReturn(user);
		given(this.trackService.getById(TRACK_UUID)).willReturn(track);
		given(this.deviceService.getById(DEVICE_UUID)).willReturn(device);

		doThrow(RuntimeException.class).when(this.historyService).save(any(History.class), anyBoolean());

		JSONObject obj = new JSONObject();
		obj.put("lastlisten", "2016-11-28T12:34:56");
		obj.put("islisten", "1");
		obj.put("method", "method");

		mockMvc.perform(post("/v2/histories/{deviceid}/{trackid}", DEVICE_UUID, TRACK_UUID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(obj.toString())
//				.param("lastlisten", "2016-11-28 12:34:56")
//				.param("islisten", "1")
//				.param("method", "method")
		)
				.andDo(print())
				.andExpect(
						status().isInternalServerError()
				);
	}


}