//
//  AudioPlayer.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 11/23/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.

//	Audio manager, set audio session and managing with player

import Foundation
import AVFoundation
import UIKit
import MediaPlayer

@available(iOS 10.0, *)
class AudioPlayerManager: NSObject, AVAssetResourceLoaderDelegate, NSURLConnectionDataDelegate {

	var player: AVPlayer = AVPlayer()
	var playerItem: AVPlayerItem!
	var asset: AVURLAsset?
	static let sharedInstance = AudioPlayerManager()

	var isPlaying: Bool = false
	var canPlayFromCache = false

	var playingSong = SongObject()

	var playingSongID: String?
	var titleSong: String!
	var assetUrlStr: String?
	let baseURL = URL(string: "http://api.ownradio.ru/v3/tracks/")

	var playbackProgres: Double!
	var currentPlaybackTime: CMTime!
	var timer = Timer()
	var shouldRemoveObserve: Bool!

	var wasInterreption = false

    let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
	let budTracksUrlString = FileManager.applicationSupportDir().appending("/AlarmTracks/")
	let currentTrackPathString = FileManager.applicationSupportDir().appending("/currentTrackPath/")
	var isSkipped = false

	let coreDataInstance = CoreDataManager.instance

	// MARK: Overrides
	override init() {
		super.init()
		//подписываемся на уведомления плеера
		//трек проигран до конца

		NotificationCenter.default.addObserver(self, selector: #selector(playerItemDidReachEnd(_:)), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: self.player.currentItem)
		//проигрывание было прервано
		NotificationCenter.default.addObserver(self, selector: #selector(crashNetwork(_:)), name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: self.player.currentItem)
		setup()
	}

	deinit {

		self.removeObserver(self, forKeyPath: #keyPath(AudioPlayerManager.playerItem.status))

		NotificationCenter.default.removeObserver(self, name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: self.player.currentItem)
		NotificationCenter.default.removeObserver(self, name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: self.player.currentItem)
		NotificationCenter.default.removeObserver(self, name: Notification.Name.AVAudioSessionInterruption, object: AVAudioSession.sharedInstance())
	}

	func setup() {
		let audioSession = AVAudioSession.sharedInstance()

		try! audioSession.setCategory(AVAudioSessionCategoryPlayback)
		try! audioSession.setMode(AVAudioSessionModeDefault)
		try! audioSession.setActive(true)

		UIApplication.shared.beginReceivingRemoteControlEvents()

		//Для того, чтобы убрать кнопку "назад" на заблокированном экране
		//явно задаем отображаемые кнопки и функции, вызываемые по их нажатию
		let commandCenter = MPRemoteCommandCenter.shared()

		let handler: (String) -> ((MPRemoteCommandEvent) -> (MPRemoteCommandHandlerStatus)) = { (name) in
			return { (event) -> MPRemoteCommandHandlerStatus in
				dump("\(name) \(event.timestamp) \(event.command)")
				return .success
			}
		}

		commandCenter.nextTrackCommand.isEnabled = true
		commandCenter.nextTrackCommand.addTarget(handler: handler("skipSong"))

		commandCenter.playCommand.isEnabled = true
		commandCenter.playCommand.addTarget(handler: handler("resumeSong"))

		commandCenter.pauseCommand.isEnabled = true
		commandCenter.pauseCommand.addTarget(handler: handler("pauseSong"))

		NotificationCenter.default.addObserver(self, selector: #selector(onAudioSessionEvent(_:)), name: Notification.Name.AVAudioSessionInterruption, object: AVAudioSession.sharedInstance())
	}

	// MARK: KVO
	// подключение/отключение гарнитуры
	override func observeValue(forKeyPath keyPath: String?,
	                           of object: Any?,
	                           change: [NSKeyValueChangeKey: Any]?,
	                           context: UnsafeMutableRawPointer?) {

		if keyPath == #keyPath(AudioPlayerManager.playerItem.status) {
			let status: AVPlayerItemStatus

			// Get the status change from the change dictionary
			if let statusNumber = change?[.newKey] as? NSNumber {
				status = AVPlayerItemStatus(rawValue: statusNumber.intValue)!
			} else {
				status = .unknown
			}

			// Switch over the status
			switch status {
			case .readyToPlay:

				if wasInterreption {
					wasInterreption = false
				} else {
					if isPlaying == true {
						self.resumeSong {
							if let rootController = UIApplication.shared.keyWindow?.rootViewController {
								let navigationController = rootController as! UINavigationController

								if let radioViewContr = navigationController.topViewController  as? RadioViewController {
									DispatchQueue.main.asyncAfter(deadline: .now() + 1.0, execute: {
										radioViewContr.progressView.isHidden = false
									})
								}
							}
						}
					}
				}
			case .failed:
				Downloader.sharedInstance.createPostNotificationSysInfo(message: "Player Item was fail")
				if UserDefaults.standard.bool(forKey: "writeLog"){
					CoreDataManager.instance.setLogRecord(eventDescription: "Player item failed", isError: true, errorMessage: playerItem.error.debugDescription)
					CoreDataManager.instance.saveContext()
				}
				print(playerItem.error.debugDescription)
				self.skipSong {
					DispatchQueue.main.async {
						if let rootController = UIApplication.shared.keyWindow?.rootViewController {
							let navigationController = rootController as! UINavigationController

							if let radioViewContr = navigationController.topViewController  as? RadioViewController {
								DispatchQueue.main.asyncAfter(deadline: .now() + 1.0, execute: {
									radioViewContr.updateUI()
								})
							}
						}
					}
				}
				break
			case .unknown:
				break
			}
		}
	}

	// MARK: Notification selectors
	// трек дослушан до конца
	func playerItemDidReachEnd(_ notification: Notification) {

		if notification.object as? AVPlayerItem  == player.currentItem {
            let dateLastTrackPlay = CoreDataManager.instance.getDateForTrackBy(trackId: self.playingSong.trackID)
            let currentDate = NSDate.init(timeIntervalSinceNow: -60.0)
            if dateLastTrackPlay != nil && !isSkipped {
				//isSkipped = false
                //Если трек был доигран менее чем за минуту после начала его воспроизведения - трек битый. Удаляем его и не отправляем по нему историю
                if (dateLastTrackPlay.self?.compare(currentDate as Date) == .orderedDescending) {
                    let path = self.tracksUrlString.appending((self.playingSong.path!))
                    print(path)
                    if FileManager.default.fileExists(atPath: path) {
                        do {
                            ApiService.shared.setTrackIsCorrect(trackId: self.playingSong.trackID, isCorrect: 0)
                            // удаляем обьект по пути
                            try FileManager.default.removeItem(atPath: path)
                            print("Поврежденный файл был удален")
                        } catch {
                            print("Ошибка при удалении файла: файл не существует")
                        }
                    }
                    // удаляем трек с базы
                    CoreDataManager.instance.deleteTrackFor(trackID: self.playingSong.trackID)
                    CoreDataManager.instance.saveContext()

                    print("Поврежденный файл был найден и удален")
                    NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message": "Поврежденный файл был удален"])

					return
				}
			}

			isSkipped = false
			self.playingSong.isListen = 1
			self.addDateToHistoryTable(playingSong: self.playingSong) //bug: иногда программа заходит сюда и воспроизведение останавливается, следующий трек не проигрывается
																	  //Останавливается внутри ассемблера, тупо примораживается процесс
		}
	}

	//обработка прерывания аудиосессии
	func onAudioSessionEvent(_ notification: Notification) {

		guard notification.name == Notification.Name.AVAudioSessionInterruption else {
			return
		}

		guard let userInfo = notification.userInfo as? [String: AnyObject] else { return }
		guard let rawInterruptionType = userInfo[AVAudioSessionInterruptionTypeKey] as? NSNumber else { return }
		//получаем информацию о прерывании
		guard let interruptionType = AVAudioSessionInterruptionType.init(rawValue: rawInterruptionType.uintValue) else {
			return
		}

		switch interruptionType {

		case .ended: //interruption ended
			print("ENDED")
			if let rawInterruptionOption = userInfo[AVAudioSessionInterruptionOptionKey] as? NSNumber {
				let interruptionOption = AVAudioSessionInterruptionOptions(rawValue: rawInterruptionOption.uintValue)
				if interruptionOption == AVAudioSessionInterruptionOptions.shouldResume {
					self.pauseSong {
						if let rootController = UIApplication.shared.keyWindow?.rootViewController {
							let navigationController = rootController as! UINavigationController

							if let radioViewContr = navigationController.topViewController  as? RadioViewController {
								DispatchQueue.main.asyncAfter(deadline: .now() + 1.0, execute: {
									radioViewContr.updateUI()
								})
							}
						}
					}
				}
			}

		case .began: //interruption started
			if self.isPlaying == true {
				print("Began Playing - TRUE")
			} else {
				print("Began Playing - FALSE")
				wasInterreption = true
			}
		}
	}

	func crashNetwork(_ notification: Notification) {
		//		self.playerItem = nil
		print("crashNetwork")
		self.player.pause()
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			return
		}
		self.nextTrack {
		}
	}

	///  confirure album cover and other params for playing song
	func configurePlayingSong(song: SongObject) {

		let albumArt = MPMediaItemArtwork(image: UIImage(named: "iconBig")!)
		var songInfo = [String: Any]()

		songInfo[MPMediaItemPropertyTitle] = song.name
		songInfo[MPMediaItemPropertyAlbumTitle] = "ownRadio"
		songInfo[MPMediaItemPropertyArtist] = song.artistName
		songInfo[MPMediaItemPropertyArtwork] = albumArt
		songInfo[MPMediaItemPropertyPlaybackDuration] = song.trackLength //NSNumber.init(value: song.trackLength)

		MPNowPlayingInfoCenter.default().nowPlayingInfo = songInfo
		UserDefaults.standard.set(song.name, forKey: "PlayingSongInfo")

	}

	// MARK: Cotrol functions
	//возобновление воспроизведения
	func resumeSong(complition: @escaping (() -> Void)) {
		isPlaying = true
		UserDefaults.standard.set(true, forKey: "trackPlayingNow")
		UserDefaults.standard.synchronize()
		if self.playerItem != nil {
			self.player.play()
			complition()
		} else {
			self.nextTrack(complition: complition)
		}
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "Трек продолжен", isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
		}
	}

	//пауза
	func pauseSong(complition: (() -> Void)) {
		isPlaying = false
		self.player.pause()
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "Трек на паузе", isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
		}
		complition()
	}

	//пропуск трека
	func skipSong(complition: @escaping (() -> Void)) {

		if (self.playingSong.trackID != nil) {
			self.playingSong.isListen = -1
			self.addDateToHistoryTable(playingSong: self.playingSong)
			if  self.playingSong.path != nil {
				//let path = FileManager.applicationSupportDir().appending("/").appending("Tracks").appending("/").appending(self.playingSong.path!)
				let path = self.tracksUrlString.appending(self.playingSong.path!)
				print(path)
				if FileManager.default.fileExists(atPath: path) {
					//удаляем пропущенный трек
					do {
						try FileManager.default.removeItem(atPath: path)
						NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message": "Пропущ. трек удален"])
					} catch {
						NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message": "Файл не удален"])
						print("Error with remove file ")
					}
					//удаляем информацию о треке из БД
					CoreDataManager.instance.deleteTrackFor(trackID: self.playingSong.trackID)
					CoreDataManager.instance.saveContext()
				}
			}
		}
		//запускаем следующий трек
		nextTrack(complition: complition)
	}

	// проигрываем трек по URL
	func playAudioWith(trackURL: URL) {
		UserDefaults.standard.set(true, forKey: "listenRunning")
		UserDefaults.standard.synchronize()
		if playerItem != nil {
			self.removeObserver(self, forKeyPath: #keyPath(AudioPlayerManager.playerItem.status))
		}

		createPlayerItemWith(url: trackURL)
  //      playerItem.addObserver(self, forKeyPath: "status", options: NSKeyValueObservingOptions(), context: nil)

		self.addObserver(self,
		                       forKeyPath: #keyPath(AudioPlayerManager.playerItem.status),
		                       options: [.old, .new],
		                       context: nil)
        player = AVPlayer(playerItem: playerItem)
		if currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable {
			CoreDataManager.instance.sentHistory()
		}

	}

	func createPlayerItemWith(url: URL) {
		if playerItem != nil {
			playerItem = nil
		}
		if self.canPlayFromCache {
			playerItem = AVPlayerItem.init(url: URL.init(fileURLWithPath: url.relativePath))
		} else {
			playerItem = AVPlayerItem(url: url)
		}
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "PlayerItem создан", isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
			
		}
	}

	// selection way to playing (Online or Cache)
	func setWayForPlay(complition: @escaping (() -> Void)) {
		//если есть кешированные треки - играем из кеша
		if self.checkCountFileInCache() {
			self.playFromCache(complition: complition)
		} else {
			//TODO: перенесено. Проверить.
			self.playingSong = SongObject()
			self.playingSong.trackID = nil
			self.player.pause()
			isPlaying = false
			self.playerItem = nil
			configurePlayingSong(song: playingSong)
		}
//		//проверка подключения к интернету
//		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
//			return
//		}
		if !UserDefaults.standard.bool(forKey: "runCaching") {
			DispatchQueue.global(qos: .background).async {
				Downloader.sharedInstance.load(isSelfFlag: false, complition: complition)
			}
		}
	}

	//проверяем есть ли кешированные треки
	func checkCountFileInCache() -> Bool {
		self.canPlayFromCache = false
		if CoreDataManager.instance.getCountOfTracks() > 0 {
			self.canPlayFromCache = true
		}
		return self.canPlayFromCache
	}

	/*	// проигрываем трек онлайн
	func playOnline(complition: (() -> Void)?) {
	//проверка подключения к интернету
	guard  currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable  else {
	return
	}
	CoreDataManager.instance.sentHistory()
	//получаем информацию о следующем треке
	ApiService.shared.getTrackIDFromServer {  (dictionary) in
	self.playingSong = SongObject()
	self.playingSong.initWithDict(dict: dictionary)
	//формируем URL трека для проигрывания
	let trackURL = self.baseURL?.appendingPathComponent(self.playingSong.trackID)
	guard let url = trackURL else {
	return
	}
	self.playAudioWith(trackURL: url)
	self.playingSongID = self.playingSong.trackID
	self.titleSong = self.playingSong.name
	self.configurePlayingSong(song: self.playingSong)
	if complition != nil {
	complition!()
	}
	}
	}
	*/

	// проигрываем трек из кеша
	func playFromCache(complition: (() -> Void)?) {

		//		if currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable{
		//			CoreDataManager.instance.sentHistory()
		//		}
		//получаем из БД трек для проигрывания
		self.playingSong = CoreDataManager.instance.getTrackToPlaing()
		guard playingSong.trackID != nil else {
			if let rootController = UIApplication.shared.keyWindow?.rootViewController {
				let navigationController = rootController as! UINavigationController

				if let radioViewContr = navigationController.topViewController  as? RadioViewController {
					DispatchQueue.main.asyncAfter(deadline: .now() + 1.0, execute: {
						radioViewContr.updateUI()
					})
				}
			}
			return
		}
		CoreDataManager.instance.setCountOfPlayForTrackBy(trackId: self.playingSong.trackID)
		CoreDataManager.instance.setDateForTrackBy(trackId: self.playingSong.trackID)
		CoreDataManager.instance.saveContext()

		NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message": "Старт \(self.playingSong.trackID!)"])
		print("Проигрываем \(self.playingSong.trackID)")
		let str = FileManager.applicationSupportDir().addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)
		let docUrl = NSURL(string: str!)?.appendingPathComponent("Tracks")
		let resUrl = docUrl?.absoluteURL.appendingPathComponent(playingSong.path!)
		var playingTrackUrlString = resUrl?.absoluteURL.absoluteString.replacingOccurrences(of: "%2F", with: "/")
		playingTrackUrlString = playingTrackUrlString?.replacingOccurrences(of: "%20", with: " ")

		//сохранение пути и объекта трека в userDefaults для использования с будильником
		//		UserDefaults.standard.set(playingSong.path, forKey:"PlayingSongPath")
		if !UserDefaults.standard.bool(forKey: "budState") { //Если будильник не установлен
			try? UserDefaults.standard.set(PropertyListEncoder().encode(self.playingSong), forKey: "PlayingSongObject")
			//			DispatchQueue.global(qos: .utility).async{
			//				CopyManager.copyCurrentTrackToDir(song: self.playingSong, copyTo: self.budTracksUrlString)
			//				print("Текущий трек скопирован в директорию будильника")
			//			}
		}

		// Сохранение объекта для последующего возобновления проигрывания при отключении приложения
		do {
			UserDefaults.standard.set(try PropertyListEncoder().encode(self.playingSong), forKey: "interruptedSongObject")
		} catch {
			print("Объект не сохранен в ud")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при сохранении текущего объекта трека в UserDefaults", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}

		do {
			if UserDefaults.standard.bool(forKey: "writeLog"){
				DispatchQueue.global(qos: .utility).async {
					CopyManager.copyCurrentTrackToDir(song: self.playingSong, copyTo: self.currentTrackPathString)
					print("Текущий трек скопирован во временное хранилище")
				}
				
			}
		} catch {
			print("Трек не скопирован во временное хранилище")

		}

		guard let url = resUrl else {
			return
		}
		self.player.pause()
		self.playAudioWith(trackURL: url as URL)
		self.playingSongID = self.playingSong.trackID
		self.configurePlayingSong(song: self.playingSong)
		self.player.play()
		if complition != nil {
			complition!()
		}
		UserDefaults.standard.synchronize()
	}

	func nextTrack(complition: @escaping (() -> Void)) {
		self.setWayForPlay(complition: complition)
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			return
		}
	}

	//сохраняем историю прослушивания
	func addDateToHistoryTable(playingSong: SongObject) {

		let creatinDate = Date()
		let dateFormetter = DateFormatter()
		dateFormetter.dateFormat = "yyyy-MM-dd'T'H:m:s"
		let creationDateString = dateFormetter.string(from: creatinDate)
		let historyEntity = HistoryEntity()

		historyEntity.recId = NSUUID().uuidString
        historyEntity.trackId = playingSong.trackID
		historyEntity.isListen = playingSong.isListen!
		historyEntity.recCreated = creationDateString
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "История прослушивания сохранена", isError: false, errorMessage: "")
		}
		CoreDataManager.instance.saveContext()

	}

	func fwdTrackToEnd() {
		isSkipped = true
		player.seek(to: (player.currentItem?.duration)!-CMTimeMake(3, 1))
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "Трек перемотан в конец", isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
		}
	}

//	func copyCurrentTrackToDir(song: SongObject, copyTo: String){
//		let songFileName = song.path
//		if songFileName != ""{
//			let pathToTrack = tracksUrlString + songFileName!
//			var isDir: ObjCBool = true
//			if FileManager.default.fileExists(atPath: copyTo, isDirectory: &isDir){
//				if !isDir.boolValue{
//					createDirectory(path: copyTo)
//				}
//			}
//			else{
//				createDirectory(path: copyTo)
//			}
//
//			let items = try! FileManager.default.contentsOfDirectory(atPath: copyTo)
//			// Удаляем старый трек
//			for item in items{
//				try! FileManager.default.removeItem(atPath: copyTo + item)
//			}
//			// Копируем новый
//			try! FileManager.default.copyItem(atPath: pathToTrack, toPath: copyTo + songFileName!)
//
//		}
//	}
//
//	func createDirectory(path: String){
//		try! FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: false, attributes: nil)
//	}

	func playOuterTrack(url: URL, song: SongObject, seekTo: Float64) {
		self.pauseSong {}
//		self.player = AVPlayer()

		playingSong = song
		//self.player.createPlayerItemWith(url: NSURL(fileURLWithPath: tracksUrlString + "/" + String(songObject.path!)) as URL)
		playAudioWith(trackURL: url)

		playerItem.seek(to: CMTimeMakeWithSeconds(seekTo, 1000000000))

		do {
			try UserDefaults.standard.set(PropertyListEncoder().encode(self.playingSong), forKey: "interruptedSongObject")
		} catch {
			print("Текущий трек не сохранен в ud")
		}

		UserDefaults.standard.synchronize()
		DispatchQueue.global(qos: .utility).async {
			CopyManager.copyCurrentTrackToDir(song: self.playingSong, copyTo: self.currentTrackPathString)
			print("Текущий трек скопирован во временное хранилище")
		}

		self.playingSongID = song.trackID

		//CoreDataManager.instance.u(trackId: song.trackID)
		//		self.player.isPlaying = true
		configurePlayingSong(song: song)
		self.resumeSong {}
	}
}
