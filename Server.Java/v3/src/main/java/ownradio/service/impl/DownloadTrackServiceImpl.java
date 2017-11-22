package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ownradio.domain.DownloadTrack;
import ownradio.domain.TracksHistory;
import ownradio.domain.TracksRating;
import ownradio.repository.DownloadTrackRepository;
import ownradio.repository.HistoryRepository;
import ownradio.repository.RatingRepository;
import ownradio.service.DownloadTrackService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DownloadTrackServiceImpl implements DownloadTrackService {

	private final DownloadTrackRepository downloadTrackRepository;
	private final HistoryRepository historyRepository;
	private final RatingRepository ratingRepository;

	@Autowired
	public DownloadTrackServiceImpl(DownloadTrackRepository downloadTrackRepository, HistoryRepository historyRepository, RatingRepository ratingRepository) {
		this.downloadTrackRepository = downloadTrackRepository;
		this.historyRepository = historyRepository;
		this.ratingRepository = ratingRepository;
	}

	@Override
	public void save(DownloadTrack downloadTrack) {
		downloadTrackRepository.saveAndFlush(downloadTrack);
	}

	@Override
	@Transactional
	public List<DownloadTrack> getLastTracksByDevice(UUID deviceId, Integer countTracks) {
		return downloadTrackRepository.getLastTracksByDevice(deviceId, countTracks);
	}

	@Override
	@Transactional
	public List<TracksHistory> getTracksHistoryByDevice(UUID deviceId, Integer countRows){
		List<TracksHistory> tracksHistories = new ArrayList<TracksHistory>();
		List<Object[]> objects = downloadTrackRepository.getTracksHistoryByDevice(deviceId, countRows);
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				TracksHistory tracksHistory = new TracksHistory();
				tracksHistory.setDownloadTrack(downloadTrackRepository.findOne(UUID.fromString((String) objects.get(i)[0])));
				if((String) objects.get(i)[1] != null) tracksHistory.setHistory(historyRepository.findOne(UUID.fromString((String) objects.get(i)[1])));

				tracksHistories.add(tracksHistory);
			}
		} else {
			return null;
		}
		return tracksHistories;
	}

	@Override
	@Transactional
	public List<TracksRating> getTracksRatingByDevice(UUID deviceId, Integer countRows){
		List<TracksRating> tracksRatings = new ArrayList<TracksRating>();
		List<Object[]> objects = downloadTrackRepository.getTracksHistoryWithRatingByDevice(deviceId, countRows);
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				TracksRating tracksRating = new TracksRating();
				tracksRating.setDownloadTrack(downloadTrackRepository.findOne(UUID.fromString((String) objects.get(i)[0])));
				if((String) objects.get(i)[1] != null) tracksRating.setRating(ratingRepository.findOne(UUID.fromString((String) objects.get(i)[1])));

				tracksRatings.add(tracksRating);
			}
		} else {
			return null;
		}
		return tracksRatings;
	}
}
