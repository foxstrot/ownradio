//
//  ViewController.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 11/22/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.
//
// Creation and update UI

import UIKit
import MediaPlayer
import Alamofire
import CloudKit
import CallKit
import CoreBluetooth



@available(iOS 10.0, *)
class RadioViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, RemoteAudioControls{
	
	// MARK:  Outlets
	
	@IBOutlet weak var infoView: UIView!
	@IBOutlet weak var circleViewConteiner: UIView!
    @IBOutlet weak var progressView: UIProgressView!
	
	@IBOutlet weak var freeSpaceLbl:UILabel!
	@IBOutlet weak var folderSpaceLbl: UILabel!
	
	@IBOutlet weak var trackNameLbl: UILabel!
	@IBOutlet weak var authorNameLbl: UILabel!
	@IBOutlet weak var trackIDLbl: UILabel!
	@IBOutlet weak var deviceIdLbl: UILabel!
	@IBOutlet weak var infoLabel1: UILabel!
	@IBOutlet weak var infoLabel2: UILabel!
	@IBOutlet weak var infoLabel3: UILabel!
	@IBOutlet weak var infoLabel4: UILabel!
	@IBOutlet weak var infoLabel5: UILabel!
	@IBOutlet weak var infoLabel6: UILabel!
	@IBOutlet weak var infoLabel7: UILabel!
	@IBOutlet weak var infoLabel8: UILabel!
	@IBOutlet weak var infoLabel9: UILabel!
	@IBOutlet weak var infoLabel10: UILabel!
	@IBOutlet var versionLabel: UILabel!
	@IBOutlet var numberOfFiles: UILabel!
	@IBOutlet var numberOfFilesInDB: UILabel!
	@IBOutlet var isNowPlaying: UILabel!
	@IBOutlet var tableView: UITableView!
	@IBOutlet weak var intrrpt_info: UILabel!
	
	@IBOutlet weak var playPauseBtn: UIButton!
	@IBOutlet weak var nextButton: UIButton!
	
	@IBOutlet weak var timerButton: UIButton!
	@IBOutlet weak var budButton: UIButton!
	
	@IBOutlet var tapRecogniser: UITapGestureRecognizer!
	
	@IBOutlet weak var leftPlayBtnConstraint: NSLayoutConstraint!
	
	// MARK: Variables
	let defaultSession = URLSession(configuration: URLSessionConfiguration.default)
	var dataTask: URLSessionDataTask?
	var player: AudioPlayerManager!
	
	var isPlaying: Bool!
	var visibleInfoView: Bool!
    var isStartListening: Bool = false
	var activeCall:Bool = false
	var btHeadsetDetached: Bool = false
//	var callObserver: CXCallObserver!
	
	var timeObserverToken:AnyObject? = nil
	
	//let progressView = CircularView(frame: CGRect.zero)
	
	let playBtnConstraintConstant = CGFloat(15.0)
	let pauseBtnConstraintConstant = CGFloat(10.0)
	
	let coreInstance = CoreDataManager.instance
	
	var cachingView = CachingView.instanceFromNib()
	var playedTracks: NSArray = CoreDataManager.instance.getGroupedTracks()
	var reachability = NetworkReachabilityManager(host: "http://api.ownradio.ru/v5")
	
	let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
	let currentTrackPathUrl = FileManager.applicationSupportDir().appending("/currentTrackPath/")
	let budTracksUrlString = FileManager.applicationSupportDir().appending("/AlarmTracks/")
	
	var alertClock: DispatchSourceTimer?
	var timer: DispatchSourceTimer?
	var bluetoothWaitTimer: DispatchSourceTimer?
	
	var interrupedByMalfunction = false
	//Воспроизведение прервано пользователем
	var interruptedManually = false
	
	let callObserver = CXCallObserver()
	
	
	override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
		if segue.identifier == "alertClockSegue"{
			if let nextViewController = segue.destination as? AlertClockViewController{
				nextViewController.player = self.player
				nextViewController.mainController = self
				nextViewController.remoteAudioControls = self
				if alertClock != nil && UserDefaults.standard.bool(forKey: "budState"){
					nextViewController.timer = alertClock
				}
			}
		}
		else if segue.identifier == "timerSegue"{
			if let nextViewController = segue.destination as? TimerViewController{
				nextViewController.player = self.player
				nextViewController.remoteAudioControls = self
				if self.timer != nil && UserDefaults.standard.bool(forKey: "timerState"){
					nextViewController.timer = timer
					
				}
			}
		}
		else if segue.identifier == "SettingsByButton" || segue.identifier == "SettingsBySwipe"{
			if let nextViewController = segue.destination as? SettingsViewController{
				nextViewController.remoteAudioControls = self
			}
		}
	}
	
	// MARK: Override
	//выполняется при загрузке окна
	override func viewDidLoad() {
		super.viewDidLoad()
		print(Thread.current.description)
		DispatchQueue.global(qos: .background).sync {
			print(Thread.current.description)
		}
		view.isUserInteractionEnabled = true
		//включаем отображение навигационной панели
		self.navigationController?.isNavigationBarHidden = false
		
		//TEST
//		var iCloudStore = NSUbiquitousKeyValueStore()
//		var a = "aaa"
//		a = iCloudStore.string(forKey: "test1") ?? ""
//		iCloudStore.set("TESTtest", forKey: "test1")
//		iCloudStore.synchronize()
		//TEST
		
		//задаем цвет навигационного бара
//		self.navigationController?.navigationBar.barTintColor = UIColor(red: 3.0/255.0, green: 169.0/255.0, blue: 244.0/255.0, alpha: 1.0)
		//цвет кнопки и иконки
		self.navigationController?.navigationBar.tintColor = UIColor.darkGray
//		self.navigationController?.navigationBar.tintColor = UIColor(red: 1, green: 0, blue: 0, alpha: 1.0)
		//цвет заголовка
		self.navigationController?.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName : UIColor.darkGray]
		
//        if isStartListening == false {
//            self.authorNameLbl.text = "ownRadio"
//        }
		self.trackNameLbl.text = ""
        self.authorNameLbl.text = ""
        
		self.checkMemoryWarning()
		
		cachingView.frame = self.view.bounds
		
		
		
		//get version of app
		if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
			if (Bundle.main.infoDictionary?["CFBundleVersion"] as? String) != nil {
				self.versionLabel.text =  "v" + version
			}
		}
		//self.circleViewConteiner.addSubview(self.progressView)
		//self.progressView.frame = self.circleViewConteiner.bounds
		//self.circleViewConteiner.autoresizingMask = [.flexibleWidth,.flexibleHeight]
		
		self.player = AudioPlayerManager.sharedInstance
		self.detectedHeadphones()
		
		self.deviceIdLbl.text = UIDevice.current.identifierForVendor?.uuidString.lowercased() //  NSUUID().uuidString.lowercased()
		self.visibleInfoView = false
		
		getCountFilesInCache()
	
		//подписываемся на уведомлени
		reachability?.listener = { [unowned self] status in
			guard self.coreInstance.getCountOfTracks() < 1 else {
					self.updateUI()
				return
			}
//            if status != NetworkReachabilityManager.NetworkReachabilityStatus.notReachable {
//                self.downloadTracks()
//            }
		}
		
		reachability?.startListening()
		//Проверяем как вылетело приложение
		if UserDefaults.standard.bool(forKey: "wasMalfunction") == false{
			self.interrupedByMalfunction = false
			UserDefaults.standard.set(true, forKey: "wasMalfunction")
		}
		else{
			self.interrupedByMalfunction = true
			UserDefaults.standard.set(true, forKey: "wasMalfunction")
		}
        UserDefaults.standard.synchronize()
		checkPlayingInterrupt()
		
		
		callObserver.setDelegate(self, queue: nil)
		coreInstance.setLogRecord(eventDescription: "Приложение запущено", isError: false, errorMessage: "")
		coreInstance.saveContext()
		
	}
	
	func checkPlayingInterrupt(){
		let songObjectEncoded = UserDefaults.standard.data(forKey: "interruptedSongObject")
		let songObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
		
		
		if UserDefaults.standard.bool(forKey: "trackPlayingNow") && songObject.path != nil{
			print("Воспроизведение было прервано, проигрывается прерваный трек")
			
			if UserDefaults.standard.bool(forKey: "playingInterrupted"){
				UserDefaults.standard.set(false, forKey: "playingInterrupted")
				print("Прервано выгрузкой или по таймеру")
			}
			else{
				print("Прервано вылетом")
			}
			
			let songFileName = String(songObject.path!)
			var trackPath: URL
			var isCorrect: Bool = false
			
			let fileManager = FileManager.default
			if fileManager.fileExists(atPath: self.tracksUrlString + songFileName!){
				isCorrect = true
				trackPath = NSURL(fileURLWithPath: self.tracksUrlString + songFileName!) as URL
			}
			else{
				print("Прерваный трек отсутсвует в кеше, копируем его туда")
				isCorrect = CopyManager.copyTrackToCache(trackPath: currentTrackPathUrl + songFileName!, trackName: songFileName!)
				trackPath = NSURL(fileURLWithPath: tracksUrlString + songFileName!) as URL
			}
			
			if !UserDefaults.standard.bool(forKey: "budState"){ //Если будильник не установлен
				try? UserDefaults.standard.set(PropertyListEncoder().encode(songObject), forKey:"PlayingSongObject")
				UserDefaults.standard.synchronize()
				DispatchQueue.global(qos: .utility).async{
					CopyManager.copyCurrentTrackToDir(song: songObject, copyTo: self.budTracksUrlString)
					print("Текущий трек скопирован в директорию будильника")
				}
				
			}
			
			let playFromTime = UserDefaults.standard.double(forKey: "playingDuration")
			if isCorrect{
				self.playTrackByUrl(trackURL: trackPath, song: songObject, seekTo: playFromTime, needUpdateUI: true)
			}
		}
		else if UserDefaults.standard.bool(forKey: "listenRunning") && songObject.path != nil{ //Супер костыль
			
			let songFileName = String(songObject.path!)
			var trackPath: URL
			var isCorrect: Bool
			
			let fileManager = FileManager.default
			if fileManager.fileExists(atPath: self.tracksUrlString + songFileName!){
				trackPath = NSURL(fileURLWithPath: self.tracksUrlString + songFileName!) as URL
				isCorrect = true
			}
			else{
				print("Прерваный трек отсутсвует в кеше, копируем его туда")
				isCorrect = CopyManager.copyTrackToCache(trackPath: currentTrackPathUrl + songFileName!, trackName: songFileName!)
				trackPath = NSURL(fileURLWithPath: tracksUrlString + songFileName!) as URL
			}
			let playFromTime = UserDefaults.standard.double(forKey: "playingDuration")
			if isCorrect{
				self.playTrackByUrl(trackURL: trackPath, song: songObject, seekTo: playFromTime, needUpdateUI: false)
				coreInstance.setLogRecord(eventDescription: "Восстановлено после вылета uuid трека = \(songObject.trackID.description)", isError: false, errorMessage: "")
				coreInstance.saveContext()
			}
			if player.playerItem != nil{
				self.player.pauseSong {
					print("Song paused")
					self.updateUI()
				}
			}
		}
		else{
			UserDefaults.standard.set(false, forKey: "listenRunning")
			UserDefaults.standard.set(false, forKey:"trackPlayingNow")
			UserDefaults.standard.synchronize()
			self.updateUI()
		}
		//обрыв воспроизведения трека
		NotificationCenter.default.addObserver(self, selector: #selector(crashNetwork(_:)), name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: self.player.playerItem)
		//трек доигран до конца
		NotificationCenter.default.addObserver(self, selector: #selector(self.songDidPlay), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: nil)
		//Приложение закрывается
		NotificationCenter.default.addObserver(self, selector: #selector(self.appTerminate), name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
		//обновление системной информации
		NotificationCenter.default.addObserver(self, selector: #selector(updateSysInfo(_:)), name: NSNotification.Name(rawValue:"updateSysInfo"), object: nil)
	}
	
	func playTrackByUrl(trackURL: URL, song: SongObject, seekTo: Float64, needUpdateUI: Bool){
		if !activeCall{
			UserDefaults.standard.set(true, forKey:"trackPlayingNow")
			UserDefaults.standard.synchronize()
//			NotificationCenter.default.addObserver(self, selector: #selector(self.crashNetwork(_:)), name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: self.player.playerItem)
//			//трек доигран до конца
//			NotificationCenter.default.addObserver(self, selector: #selector(self.songDidPlay), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: nil)
//			//Приложение закрывается
//			NotificationCenter.default.addObserver(self, selector: #selector(self.appTerminate), name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
			
			self.player.playOuterTrack(url: trackURL, song: song, seekTo: seekTo)
			self.isStartListening = true
			if needUpdateUI{
				self.updateUI()
			}
		}
	}
	
	func checkMemoryWarning() {
		guard DiskStatus.freeDiskSpaceInBytes < 104857600 && coreInstance.chekCountOfEntitiesFor(entityName: "TrackEntity") < 1 else {
			return
		}
		self.authorNameLbl.text = "Not enough free memory. To work correctly, you need at least 100 mb"
		self.trackNameLbl.text = ""
		self.playPauseBtn.isEnabled = false
		self.nextButton.isEnabled = false
	}
	
	func detectedHeadphones () {
		
		let currentRoute = AVAudioSession.sharedInstance().currentRoute
		if currentRoute.outputs.count != 0 {
			for description in currentRoute.outputs {
				if description.portType == AVAudioSessionPortHeadphones {
					print("headphone plugged in")
				} else {
					print("headphone pulled out")
				}
			}
		} else {
			print("requires connection to device")
		}
		NotificationCenter.default.addObserver(self, selector:  #selector(self.audioRouteChangeListener), name: NSNotification.Name.AVAudioSessionRouteChange, object: nil)
	}
	
	override func viewDidAppear(_ animated: Bool) {
		super.viewDidAppear(animated)
		//Центр уведомлений не предоставляет API, позволяющее проверить был ли уже зарегистрирован наблюдатель, поэтому когда представление снова становится видимым удаляем наблюдателя и добавляем его заново
//		NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil)
//		//обновление системной информации
//		NotificationCenter.default.addObserver(self, selector: #selector(updateSysInfo(_:)), name: NSNotification.Name(rawValue:"updateSysInfo"), object: nil)
//
//		NotificationCenter.default.addObserver(self, selector: #selector(self.crashNetwork(_:)), name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: self.player.playerItem)
//		//трек доигран до конца
//		NotificationCenter.default.addObserver(self, selector: #selector(self.songDidPlay), name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: nil)
//		//Приложение закрывается
//		NotificationCenter.default.addObserver(self, selector: #selector(self.appTerminate), name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
		
		
		self.player = AudioPlayerManager.sharedInstance
		
		if UserDefaults.standard.bool(forKey: "timerState"){
			timerButton.setImage(UIImage(named: "timBlueImage"), for: .normal)
		}
		else{
			timerButton.setImage(UIImage(named: "timGrayImage"), for: .normal)
		}
		if UserDefaults.standard.bool(forKey: "budState"){
			budButton.setImage(UIImage(named: "budBlueImage"), for: .normal)
			updateUI()
		}
		else{
			budButton.setImage(UIImage(named: "budGrayImage"), for: .normal)
		}
		updateUI()
	}
	
	//когда приложение скрыто - отписываемся от уведомлений
	override func viewDidDisappear(_ animated: Bool) {
		super.viewDidDisappear(animated)
//		reachability?.stopListening()
		
//		NotificationCenter.default.removeObserver(self, name:  NSNotification.Name.AVPlayerItemFailedToPlayToEndTime, object: nil)
//		NotificationCenter.default.removeObserver(self, name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: self.player.playerItem)
//		NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil)
		//NotificationCenter.default.removeObserver(self, name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
	}
	
	//управление проигрыванием со шторки / экрана блокировки
	override func remoteControlReceived(with event: UIEvent?) {
		//по событию нажития на кнопку управления медиаплеером
		//проверяем какая именно кнопка была нажата и обрабатываем нажатие
		if event?.type == UIEventType.remoteControl {
			switch event!.subtype {
			case UIEventSubtype.remoteControlPause:
				guard MPNowPlayingInfoCenter.default().nowPlayingInfo != nil else {
					break
				}
				interruptedManually = true
				changePlayBtnState()
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(self.player.player.currentTime())
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 0
				
			case .remoteControlPlay:
				guard MPNowPlayingInfoCenter.default().nowPlayingInfo != nil else {
					break
				}
				changePlayBtnState()
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(self.player.player.currentTime())
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 1
				
			case .remoteControlTogglePlayPause:
				guard MPNowPlayingInfoCenter.default().nowPlayingInfo != nil else {
					break
				}
				changePlayBtnState()
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(self.player.player.currentTime())
				if player.isPlaying == false {
					MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 0
				} else {
					MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 1
				}
				break
				
			case .remoteControlNextTrack:
				player.skipSong(complition: { [unowned self] in
						self.updateUI()
				})
			default:
				break
			}
		}
	}
	
	func downloadTracks() {
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			return
		}
		DispatchQueue.global(qos: .utility).async {
			Downloader.sharedInstance.load(isSelfFlag: false){ [unowned self] in
					self.updateUI()
			}
		}
	}
	
	// MARK: Notification Selectors
	func songDidPlay() {
		self.player.nextTrack { [unowned self] in
				self.updateUI()
		}
//		self.progressView.isHidden = true
	}
	
	
	//функция обновления поля Info системной информации
	func updateSysInfo(_ notification: Notification){
		DispatchQueue.main.async {
			let creatinDate = Date()
			let dateFormatter = DateFormatter()
			dateFormatter.dateFormat = "HH:mm:ss.SS"
			dateFormatter.timeZone = TimeZone.current
			let creationDateString = dateFormatter.string(from: creatinDate)

		
			
			guard let userInfo = notification.userInfo,
				let message = userInfo["message"] as? String else {
					self.infoLabel10.text = self.infoLabel9.text
					self.infoLabel9.text = self.infoLabel8.text
					self.infoLabel8.text = self.infoLabel7.text
					self.infoLabel7.text = self.infoLabel6.text
					self.infoLabel6.text = self.infoLabel5.text
					self.infoLabel5.text = self.infoLabel4.text
					self.infoLabel4.text = self.infoLabel3.text
					self.infoLabel3.text = self.infoLabel2.text
					self.infoLabel2.text = self.infoLabel1.text
					self.infoLabel1.text = creationDateString + "No userInfo found in notification"
					return
			}
			self.infoLabel10.text = self.infoLabel9.text
			self.infoLabel9.text = self.infoLabel8.text
			self.infoLabel8.text = self.infoLabel7.text
			self.infoLabel7.text = self.infoLabel6.text
			self.infoLabel6.text = self.infoLabel5.text
			self.infoLabel5.text = self.infoLabel4.text
			self.infoLabel4.text = self.infoLabel3.text
			self.infoLabel3.text = self.infoLabel2.text
			self.infoLabel2.text = self.infoLabel1.text
			self.infoLabel1.text = creationDateString + " " + message
			print("\(self.infoLabel1.text)")
		}
	}
	
	func crashNetwork(_ notification: Notification) {
		self.playPauseBtn.setImage(UIImage(named: "playImage"), for: UIControlState.normal)
		self.leftPlayBtnConstraint.constant = pauseBtnConstraintConstant
		self.trackIDLbl.text = ""
		self.infoLabel10.text = self.infoLabel9.text
		self.infoLabel9.text = self.infoLabel8.text
		self.infoLabel8.text = self.infoLabel7.text
		self.infoLabel7.text = self.infoLabel6.text
		self.infoLabel6.text = self.infoLabel5.text
		self.infoLabel5.text = self.infoLabel4.text
		self.infoLabel4.text = self.infoLabel3.text
		self.infoLabel3.text = self.infoLabel2.text
		self.infoLabel2.text = self.infoLabel1.text
		self.infoLabel1.text = notification.description
	}
	
	//Функция уведомления о закрытии приложения
	func appTerminate(){
		if UserDefaults.standard.bool(forKey: "trackPlayingNow"){
			UserDefaults.standard.set(true, forKey: "playingInterrupted")
			if self.player.playerItem != nil{
				UserDefaults.standard.set(self.player.playerItem.currentTime().seconds, forKey: "playingDuration")
			}
		}
		UserDefaults.standard.set(false, forKey: "listenRunning")
        UserDefaults.standard.set(false, forKey: "wasMalfunction")
        UserDefaults.standard.synchronize()
		
		coreInstance.setLogRecord(eventDescription: "Приложение выгружено", isError: false, errorMessage: "")
		coreInstance.saveContext()
	}
	

	
	func audioRouteChangeListener(notification:NSNotification) {
		let audioRouteChangeReason = notification.userInfo![AVAudioSessionRouteChangeReasonKey] as! UInt
		//		 AVAudioSessionPortHeadphones
		switch audioRouteChangeReason {
		case AVAudioSessionRouteChangeReason.newDeviceAvailable.rawValue:
			print("headphone plugged in")
			let currentRoute = AVAudioSession.sharedInstance().currentRoute
			for description in currentRoute.outputs {
				
				if description.portType == AVAudioSessionPortHeadphones {
					print(description.portType)
//					print(self.player.isPlaying)
//					if self.player != nil && !self.player.isPlaying && !self.interruptedManually && !self.activeCall{
//						player.resumeSong {
//							self.updateUI()
//						}
//					}
				}
	//				else if (description.portType == AVAudioSessionPortBluetoothLE || description.portType == AVAudioSessionPortBluetoothHFP || description.portType == AVAudioSessionPortBluetoothA2DP){
	//					bluetoothWaitTimer?.cancel()
	//					if !self.activeCall{
	//
	//						if self.player != nil && !self.player.isPlaying && !self.interruptedManually && !self.activeCall{
	//							self.player.isPlaying = true
	//							self.player.player.play()
	//							updateUI()
	//						}
	//					}
	//				}
				else {
					print(description.portType)
				}
			}
		case AVAudioSessionRouteChangeReason.oldDeviceUnavailable.rawValue:
//			btHeadsetDetached = true
			print("headphone pulled out")
			print(self.player.isPlaying)
			print(self.player.isPlaying)
			self.interruptedManually = false
			self.player.pauseSong {
				self.updateUI()
			}
			
//		case AVAudioSessionRouteChangeReason.categoryChange.rawValue:
//
//			for description in AVAudioSession.sharedInstance().currentRoute.outputs {
//
//				switch description.portType {
//				case AVAudioSessionPortBluetoothA2DP:
//					if self.player.isPlaying == false {
//						self.player.pauseSong{
//						}
//					}
//				case AVAudioSessionPortBluetoothLE:
//					if self.player.isPlaying == false {
//						self.player.pauseSong {
//						}
//					}
//				case AVAudioSessionPortBluetoothHFP:
//					if self.player.isPlaying == false {
//						self.player.pauseSong {
//						}
//					}
//				default: break
//				}
//			}
		default:
			break
		}
	}
	
	//меняет состояние проигрывания и кнопку playPause
	func changePlayBtnState() {
		//если трек проигрывается - ставим на паузу
		if player.isPlaying == true {
			player.pauseSong(complition: { [unowned self] in
				UserDefaults.standard.set(false, forKey:"trackPlayingNow")
				UserDefaults.standard.synchronize()
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(self.player.player.currentTime())
				MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 0
					self.updateUI()
				})
		}else {
			//иначе - возобновляем проигрывание если возможно или начинаем проигрывать новый трек
			player.resumeSong(complition: { [unowned self] in
				if self.coreInstance.getCountOfTracks() > 0 {
					//Вылетает, если ничего не проигрывалось и играет трек из будильника и нажата кнопка PLAY
					MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(self.player.player.currentTime())
					MPNowPlayingInfoCenter.default().nowPlayingInfo![MPNowPlayingInfoPropertyPlaybackRate] = 1
					self.updateUI()
				}
			})
		}
	}
	
	//функция отображения количества файлов в кеше
	func getCountFilesInCache () {
		do {
//			let appSupportUrl = URL(string: FileManager.applicationSupportDir().appending("/"))
			let docUrl = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first?.appendingPathComponent("Tracks")
			let directoryContents = try FileManager.default.contentsOfDirectory(at: docUrl!, includingPropertiesForKeys: nil, options: [])
			let mp3Files = directoryContents.filter{ $0.pathExtension == "mp3" }
			self.numberOfFiles.text = String(coreInstance.chekCountOfEntitiesFor(entityName: "TrackEntity"))
		} catch let error as NSError {
			print(error.localizedDescription)
		}
	}
	
	func playingAlarmTrack(){
		self.isStartListening = true
	}
	
	//обновление UI
	func updateUI() {
		DispatchQueue.main.async { [unowned self] in
        
        if self.isStartListening == true {
            self.trackNameLbl.text = self.player.playingSong.name
            self.authorNameLbl.text = self.player.playingSong.artistName
        }
		self.trackIDLbl.text = self.player.playingSong.trackID
		//self.isNowPlaying.text = String(self.player.isPlaying)
		
		if UserDefaults.standard.bool(forKey: "timerState"){
			self.timerButton.setImage(UIImage(named: "timBlueImage"), for: .normal)
		}
		else{
			self.timerButton.setImage(UIImage(named: "timGrayImage"), for: .normal)
		}
			
		if UserDefaults.standard.bool(forKey: "budState"){
			self.budButton.setImage(UIImage(named: "budBlueImage"), for: .normal)
		}
		else{
			self.budButton.setImage(UIImage(named: "budGrayImage"), for: .normal)
		}
			
			
			if self.coreInstance.getCountOfTracks() < 3 && self.coreInstance.getCountOfTracks() != 0 {
//			self.playPauseBtn.isEnabled = false
			self.nextButton.isEnabled = false
			self.cachingView.removeFromSuperview()
			}else if self.coreInstance.getCountOfTracks() < 1 {
			self.playPauseBtn.isEnabled = true
			self.view.addSubview(self.cachingView)
		}else {
			self.playPauseBtn.isEnabled = true
			self.nextButton.isEnabled = true
			self.cachingView.removeFromSuperview()
		}
		
		//обновляение прогресс бара
		

		//		self.timeObserverToken =
		 self.timeObserverToken = self.player.player.addPeriodicTimeObserver(forInterval: CMTimeMakeWithSeconds(1.0, 1) , queue: DispatchQueue.main) { [unowned self] (time) in
            if self.player.isPlaying == true {
                if self.player.playingSong.trackLength != nil{
                self.progressView.setProgress(Float(CGFloat(time.seconds) / CGFloat((self.player.playingSong.trackLength)!)), animated: false)
					UserDefaults.standard.set(time.seconds.description, forKey:"lastTrackPosition")
					UserDefaults.standard.set(self.player.playingSong.trackID as String, forKey:"lastTrack")
					if Int(time.seconds) % 15 == 0{
						UserDefaults.standard.set(self.player.playerItem.currentTime().seconds, forKey: "playingDuration")
						UserDefaults.standard.synchronize()
					}
					
//				self.progressView.progress = (CGFloat(time.seconds) / CGFloat((self.player.playingSong.trackLength)!))
                }
			}
			} as AnyObject?
		}
	
		
		//обновление кнопки playPause
		if self.player.isPlaying == false {
			self.playPauseBtn.setImage(UIImage(named: "playImage"), for: UIControlState.normal)
			//self.leftPlayBtnConstraint.constant = self.playBtnConstraintConstant
		} else {
			self.playPauseBtn.setImage(UIImage(named: "pauseImage"), for: UIControlState.normal)
			//self.leftPlayBtnConstraint.constant = self.pauseBtnConstraintConstant
		}
		
		self.getCountFilesInCache()
		// обновление количевства записей в базе данных
		self.numberOfFilesInDB.text = String(coreInstance.chekCountOfEntitiesFor(entityName: "TrackEntity"))
		// update table 
		self.playedTracks = coreInstance.getGroupedTracks()
		self.tableView.reloadData()
		self.freeSpaceLbl.text = DiskStatus.GBFormatter(Int64(DiskStatus.freeDiskSpaceInBytes)) + " Gb"
		self.folderSpaceLbl.text = DiskStatus.GBFormatter(Int64(DiskStatus.folderSize(folderPath: self.tracksUrlString))) + " Gb"
		if interrupedByMalfunction{
			self.intrrpt_info.text = "true"
		}
		else{
			self.intrrpt_info.text = "false"
		}
	}
	
	
	// MARK: UITableViewDataSource
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return self.playedTracks.count
	}

	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
		
		let dict = playedTracks[indexPath.row] as! [String: Any]
		let countOfPlay = dict["countPlay"] as? Int
		let countOfTracks = dict["count"] as? Int
		if countOfPlay != nil && countOfTracks != nil {
			let str = NSString(format: "Count play: %d - Count tracks: %d", countOfPlay! , countOfTracks! )
			cell.textLabel?.text = str as String
		}
		return cell
	}
	
	func createPostNotificationSysInfo (message:String) {
		NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message":message])
	}
	
	
	@IBAction func oneTapAction(_ sender: Any) {
		//Обновление состояния таймера по нажатию на экран
		if UserDefaults.standard.bool(forKey: "timerState"){
			UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "updateTimerDate")
		}
	}
	
    // MARK: Actions
	@IBAction func tripleTapAction(_ sender: AnyObject) {
		if self.infoView.isHidden == true {
			self.infoView.isHidden = false
			self.visibleInfoView = false
			let items = try! FileManager.default.contentsOfDirectory(atPath: tracksUrlString)
			for item in items{
				print("Track:" + item)
			}
		}else {
			self.infoView.isHidden = true
			self.visibleInfoView = true
		}
	}
	
	@IBAction func nextTrackButtonPressed() {
        isStartListening = true
        
		if player.isPlaying == true {
			self.player.player.pause()
		}
//		self.progressView.isHidden = true
		self.progressView.setProgress(0.0, animated: false)
        
		self.player.skipSong { [unowned self] in
				self.updateUI()
		}
		if self.timeObserverToken != nil {
			self.timeObserverToken = nil
		}
	}
	
	//обработчик нажатий на кнопку play/pause
	@IBAction func playBtnPressed() {
        isStartListening = true
		if player.isPlaying{
			self.interruptedManually = true
		}
		else{
			interruptedManually = false
		}
		
		guard self.player.playerItem != nil else {
			
			self.player.isPlaying = true
			UserDefaults.standard.set(true, forKey:"trackPlayingNow")
			UserDefaults.standard.synchronize()
			
			nextTrackButtonPressed()
			
			return
		}
		self.progressView.isHidden = false
		changePlayBtnState()
	}
		
	@IBAction func refreshPressed() {
		updateUI()
	}
	
	
	@IBAction func skipTrackToEnd(_ sender: UIButton) {
		self.player.fwdTrackToEnd()
	}


}

@available(iOS 10.0, *)
extension RadioViewController: CXCallObserverDelegate{
	func callObserver(_ callObserver: CXCallObserver, callChanged call: CXCall){
		if call.isOutgoing == true && call.hasConnected == false{
			if player.isPlaying{
				self.interruptedManually = false
			}
			print("Звонок начался")
			self.createPostNotificationSysInfo(message: "Call started")
			self.activeCall = true
			if self.player != nil{
				player.pauseSong {
					print("call start, song paused")
					self.updateUI()
				}
			}
			coreInstance.setLogRecord(eventDescription: "Входящий звонок", isError: false, errorMessage: "")
			coreInstance.saveContext()
		}
		
		else if call.isOutgoing == false && call.hasConnected == false && call.hasEnded == false{
			
			
			if player.isPlaying{
				self.interruptedManually = false
			}
			print("Звонок начался")
			self.createPostNotificationSysInfo(message: "Call started")
			self.activeCall = true
			if self.player != nil{
				player.pauseSong {
					print("call start, song paused")
					self.updateUI()
				}
			}
			coreInstance.setLogRecord(eventDescription: "Входящий звонок", isError: false, errorMessage: "")
			coreInstance.saveContext()
		}
		else if call.hasEnded == true{
			self.activeCall = false
			print("Звонок завершен")
			self.createPostNotificationSysInfo(message: "Call end")
			if self.player != nil && !self.interruptedManually{
				player.resumeSong {
					print("call stop, song resumed")
					self.updateUI()
				}
			}
			coreInstance.setLogRecord(eventDescription: "Звонок завершен", isError: false, errorMessage: "")
			coreInstance.saveContext()
		}
	}
}

protocol RemoteAudioControls {
	func remoteControlReceived(with event: UIEvent?)
}

