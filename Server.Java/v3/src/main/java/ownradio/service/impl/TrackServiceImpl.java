package ownradio.service.impl;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ownradio.domain.NextTrack;
import ownradio.domain.Track;
import ownradio.domain.UploadersRating;
import ownradio.repository.TrackRepository;
import ownradio.repository.UserRepository;
import ownradio.service.TrackService;
import ownradio.util.DecodeUtil;
import ownradio.util.ResourceUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrackServiceImpl implements TrackService {

	private final TrackRepository trackRepository;
	private final UserRepository userRepository;

	@Autowired
	public TrackServiceImpl(TrackRepository trackRepository, UserRepository userRepository) {
		this.trackRepository = trackRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Track getById(UUID id) {
		return trackRepository.findOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Track> getByDeviceId(UUID id, Pageable pageable) {
		return trackRepository.findAllByDeviceRecidOrderByReccreatedDesc(id, pageable);
	}

	@Override
	@Transactional
	public UUID getNextTrackId(UUID deviceId) {
		return trackRepository.getNextTrackId(deviceId);
	}

	@Override
	@Transactional
	public NextTrack getNextTrackIdV2(UUID deviceId) {
		NextTrack nextTrack = new NextTrack();

		List<Object[]> objects = null;
		try{
			objects = trackRepository.getNextTrackV2(deviceId);
		}catch (Exception ex){
			ex.getLocalizedMessage();
		}
		if(objects != null) {
			nextTrack.setTrackid(UUID.fromString((String) objects.get(0)[0]));
			nextTrack.setMethodid((Integer) objects.get(0)[1]);
			if(objects.get(0)[2] != null) nextTrack.setUseridrecommended(UUID.fromString((String) objects.get(0)[2]));
			if(objects.get(0)[3] != null) nextTrack.setTxtrecommendinfo((String) objects.get(0)[3]);
			if(objects.get(0)[4] != null) nextTrack.setTimeexecute((String) objects.get(0)[4]);
			return nextTrack;
		}else{
			return null;
		}
	}

	@Override
	@Transactional
	public void save(Track track, MultipartFile file) {
		boolean result = trackRepository.registerTrack(track.getRecid(), track.getLocaldevicepathupload(), track.getPath(), track.getDevice().getRecid());
		if (!result) {
			throw new RuntimeException();
		}

		Track storeTrack = trackRepository.findOne(track.getRecid());

		String dirName = storeTrack.getDevice().getUser().getRecid().toString();
		String fileName = storeTrack.getRecid() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
		String filePath = ResourceUtil.save(dirName, fileName, file);

		storeTrack.setPath(filePath);
	}

	@Override
	@Transactional
	public void setTrackInfo(UUID trackid) {
		String artist = null;
		String title = null;
		boolean artistFlag = false;
		boolean titleFlag = false;
		Track track = null;
		Mp3File mp3File = null;

		if (trackid != null) {
			try {
				track = trackRepository.findOne(trackid);
				mp3File = new Mp3File(track.getPath());
				track.setLength((int) mp3File.getLengthInSeconds());//duration track
				track.setSize((int) mp3File.getLength() / 1024);//size in kilobytes

				if(mp3File.getLayer().equals("II"))
					track.setIscorrect(0);

				if (mp3File.hasId3v2Tag()) {
					ID3v2 id3v2Tag2 = mp3File.getId3v2Tag();
					title = DecodeUtil.Decode(id3v2Tag2.getTitle());
					artist = DecodeUtil.Decode(id3v2Tag2.getArtist());
				}
				if(mp3File.hasId3v1Tag()) {
					ID3v1 id3v1Tag1 = mp3File.getId3v1Tag();
					if(title == null || title.equals("null") || title.isEmpty())
					title = DecodeUtil.Decode(id3v1Tag1.getTitle());
					if(artist == null || artist.equals("null") || artist.isEmpty())
					artist = DecodeUtil.Decode(id3v1Tag1.getArtist());
				}

				if (title != null && !title.equals("null") && !title.isEmpty()) {
					track.setRecname(title.replaceAll("\u0000", ""));
					titleFlag = true;
				}
				if (artist != null && !artist.equals("null") && !artist.isEmpty()) {
					track.setArtist(artist.replaceAll("\u0000", ""));
					artistFlag = true;
				}

				if (artistFlag && titleFlag)
					track.setIsfilledinfo(1);
				trackRepository.saveAndFlush(track);
			} catch (Exception ex) {
				if(mp3File == null) {
					track.setIscorrect(0);
					trackRepository.saveAndFlush(track);
				}
			}
		}
	}

	@Transactional
	@Override
	public List<UploadersRating> getUploadersRating(){
		List<UploadersRating> uploadersRatingList = new ArrayList<>();
		List<Object[]> objects = trackRepository.findUploadersRating();
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				UploadersRating uploadersRating = new UploadersRating();
				uploadersRating.setUser(userRepository.findOne(UUID.fromString((String) objects.get(i)[0])));
				uploadersRating.setUploadtracks((BigInteger)objects.get(i)[1]);
				uploadersRating.setLastactive((String)objects.get(i)[2]);
				uploadersRatingList.add(uploadersRating);
			}
	} else {
		return null;
	}
		return uploadersRatingList;
	}
}
