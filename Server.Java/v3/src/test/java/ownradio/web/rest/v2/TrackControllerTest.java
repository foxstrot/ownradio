package ownradio.web.rest.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ownradio.domain.Device;
import ownradio.domain.Track;
import ownradio.domain.User;
import ownradio.service.DeviceService;
import ownradio.service.TrackService;
import ownradio.util.ResourceUtil;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ownradio.util.ResourceUtil.UPLOAD_DIR;

@RunWith(SpringRunner.class)
@WebMvcTest(TrackController.class)
public class TrackControllerTest {
	public static final UUID TRACK_UUID = UUID.randomUUID();
	public static final UUID USER_UUID = UUID.randomUUID();
	public static final UUID DEVICE_UUID = UUID.randomUUID();
	public static final String FILE = TRACK_UUID + ".mp3";
	public static final String PATH = UPLOAD_DIR + USER_UUID + "/" + FILE;

	@MockBean
	private TrackService trackService;

	@MockBean
	private DeviceService deviceService;

	@Autowired
	protected MockMvc mockMvc;

	private MockMultipartFile correctFile;
	private MockMultipartFile emptyFile;

	private ObjectMapper mapper = new ObjectMapper();

	private User user = new User();
	private Device device = new Device();
	private Track track;

	@Before
	public void setUp() throws Exception {
		user.setRecid(USER_UUID);
		device.setRecid(DEVICE_UUID);
		device.setUser(user);

		track = new Track(PATH, device, "---", 0,  "", 0, null, null, null, 1);

		String requestParam = "musicFile";
		String originalFilename = "test.mp3";
		String contentType = "audio/mpeg";

		correctFile = new MockMultipartFile(requestParam, originalFilename, contentType, "Text".getBytes());
		emptyFile = new MockMultipartFile(requestParam, originalFilename, contentType, "".getBytes());

		ResourceUtil.save(USER_UUID.toString(), FILE, correctFile);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(new File(UPLOAD_DIR));
	}

	@Test
	public void saveStatusIsOk() throws Exception {
		given(this.deviceService.getById(DEVICE_UUID)).willReturn(device);

		JSONObject obj = new JSONObject();
		obj.put("fileGuid", TRACK_UUID.toString());
		obj.put("fileName", correctFile.getOriginalFilename());
		obj.put("filePath", PATH);
		obj.put("deviceid", DEVICE_UUID.toString());

		mockMvc.perform(fileUpload("/v2/tracks")
				.file(correctFile)
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(obj.toString())
				.contentType(MediaType.MULTIPART_FORM_DATA)
//				.param("fileGuid", TRACK_UUID.toString())
//				.param("fileName", correctFile.getOriginalFilename())
//				.param("filePath", PATH)
//				.param("deviceid", DEVICE_UUID.toString())
		)
				.andDo(print())
				.andExpect(
						status().isCreated()
				);
	}

	@Test
	public void saveStatusIsBadRequest() throws Exception {
		mockMvc.perform(fileUpload("/v2/tracks")
				.file(emptyFile)
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.param("fileGuid", TRACK_UUID.toString())
				.param("fileName", correctFile.getOriginalFilename())
				.param("filePath", PATH)
				.param("deviceid", DEVICE_UUID.toString())
		)
				.andDo(print())
				.andExpect(
						status().isBadRequest()
				);
	}

	@Test
	public void getTrackStatusIsOk() throws Exception {
		given(this.trackService.getById(TRACK_UUID)).willReturn(track);

		mockMvc.perform(get("/v2/tracks/{trackid}", TRACK_UUID).accept(MediaType.TEXT_PLAIN))
				.andDo(print())
				.andExpect(
						status().isOk()
				)
				.andExpect(
						header().string("Content-Type", is("audio/mpeg"))
				)
				.andExpect(
						content().string("Text")
				);
	}

	@Test
	public void getTrackStatusIsNotFound() throws Exception {

		given(this.trackService.getById(TRACK_UUID)).willReturn(null);

		mockMvc.perform(get("/v2/tracks/{trackid}", TRACK_UUID).accept(MediaType.TEXT_PLAIN))
				.andDo(print())
				.andExpect(
						status().isNotFound()
				);
	}

	@Test
	public void getNextTrackIdIsOk() throws Exception {
		given(this.trackService.getNextTrackId(DEVICE_UUID)).willReturn(TRACK_UUID);

		mockMvc.perform(get("/v2/tracks/{deviceid}/next", DEVICE_UUID))
				.andDo(print())
				.andExpect(
						status().isOk()
				)
				.andExpect(
						content().string(mapper.writeValueAsString(TRACK_UUID))
				);

	}

	@Test
	public void getNextTrackIdIsNotFound() throws Exception {
		given(this.trackService.getNextTrackId(DEVICE_UUID)).willReturn(null);

		mockMvc.perform(get("/v2/tracks/{deviceid}/next", DEVICE_UUID))
				.andDo(print())
				.andExpect(
						status().isNotFound()
				);

	}

}