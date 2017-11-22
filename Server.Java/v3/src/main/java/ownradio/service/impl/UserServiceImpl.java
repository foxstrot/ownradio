package ownradio.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ownradio.domain.Device;
import ownradio.domain.User;
import ownradio.domain.UsersRating;
import ownradio.repository.UserRepository;
import ownradio.service.UserService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public User getById(UUID id) {
		return userRepository.findOne(id);
	}

	@Override
	public User save(User user) {
		return userRepository.saveAndFlush(user);
	}

	@Override
	@Transactional
	public List<UsersRating> getUsersRating(Integer countRows) {
		List<UsersRating> usersRating = new ArrayList<UsersRating>();
		List<Object[]> objects = userRepository.getUsersRating(countRows);
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				UsersRating userRating = new UsersRating();
				userRating.setUserid(UUID.fromString((String) objects.get(i)[0]));
				userRating.setReccreated((String) objects.get(i)[1]);
				userRating.setRecname((String) objects.get(i)[2]);
				userRating.setRecupdated((String) objects.get(i)[3]);
				userRating.setOwntracks((BigInteger) objects.get(i)[4]);
				userRating.setDownloadtracks((BigInteger) objects.get(i)[5]);

				usersRating.add(userRating);
			}
		} else {
			return null;
		}
		return usersRating;
	}


	@Override
	public List<UsersRating> getLastUsers(Integer countRows){
		List<UsersRating> lastUsers = new ArrayList<UsersRating>();
		List<Object[]> objects = userRepository.getLastUsers(countRows);
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				UsersRating lastUser = new UsersRating();
				lastUser.setUserid(UUID.fromString((String) objects.get(i)[0]));
				lastUser.setReccreated((String) objects.get(i)[1]);
				lastUser.setLastactive((String) objects.get(i)[2]);
				lastUser.setRecname((String) objects.get(i)[3]);
				lastUser.setRecupdated((String) objects.get(i)[4]);
				lastUser.setOwntracks((BigInteger) objects.get(i)[5]);
				lastUser.setDownloadtracks((BigInteger) objects.get(i)[6]);
				lastUsers.add(lastUser);
			}
		} else {
			return null;
		}
		return lastUsers;
	}
}
