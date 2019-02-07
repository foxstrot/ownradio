//
//  AlertClockViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 27.12.2018.
//  Copyright © 2018 Netvox Lab. All rights reserved.
//

import UIKit

@available(iOS 10.0, *)
class AlertClockViewController: UIViewController {

	@IBOutlet weak var timePicker: UIDatePicker!
    
	@IBOutlet weak var mondayBTN: UIButton!
	@IBOutlet weak var saturdayBTN: UIButton!
	@IBOutlet weak var sundayBTN: UIButton!
	@IBOutlet weak var tuesdayBTN: UIButton!
	@IBOutlet weak var wednesdayBTN: UIButton!
	@IBOutlet weak var thursdayBTN: UIButton!
	@IBOutlet weak var fridayBTN: UIButton!
	@IBOutlet weak var currentPlaySongLbl: UILabel!
    @IBOutlet weak var setBudButton: UIButton!
    @IBOutlet weak var infoLabel: UILabel!
	
	
	let userDefaults = UserDefaults.standard
	var daysSelected = [Bool]()
	var selectedTime = Date()
	var player: AudioPlayerManager!
	var mainController: RadioViewController!
	var budSchedule = [Date]()
	var timer: DispatchSourceTimer?
	
	var remoteAudioControls: RemoteAudioControls?
	
	let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
	let budTracksUrlString = FileManager.applicationSupportDir().appending("/AlarmTracks/")
	
	override func viewDidLoad() {
        super.viewDidLoad()
		let dateformatter = DateFormatter()
		dateformatter.timeStyle = DateFormatter.Style.short
		timePicker.date = (userDefaults.object(forKey: "alarmClockTime") as? Date ?? Date())!
		
		if self.userDefaults.data(forKey: "PlayingSongObject") != nil{
			let songObjectEncoded = self.userDefaults.data(forKey: "PlayingSongObject")
			let songObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
			currentPlaySongLbl.text = songObject.name
		}
		else{
			currentPlaySongLbl.text = ""
		}
		

		daysSelected = userDefaults.array(forKey: "selectedDaysArray") as? [Bool] ?? [Bool]([false, false, false, false, false, false, false])
		if daysSelected.count > 0{
			for (index, value) in daysSelected.enumerated(){
				switch index{
				case 1:
					mondayBTN.isSelected = value
				case 2:
					tuesdayBTN.isSelected = value
				case 3:
					wednesdayBTN.isSelected = value
				case 4:
					thursdayBTN.isSelected = value
				case 5:
					fridayBTN.isSelected = value
				case 6:
					saturdayBTN.isSelected = value
				case 0:
					sundayBTN.isSelected = value
				default:
					break
				}
			}
		}
		if userDefaults.bool(forKey: "budState"){
			setBudButton.setImage(UIImage(named: "budBlue"), for: .normal)
            budSchedule = userDefaults.object(forKey: "budSchedule") as? [Date] ?? [Date]()
            if budSchedule.count != 0 || userDefaults.bool(forKey: "budState"){
				getMinDatediff(needDisplay: true)
            }
            else{
                infoLabel.text = ""
            }
		}
		else{
			setBudButton.setImage(UIImage(named: "budGray"), for: .normal)
			infoLabel.text = ""
		}
		
		// Do any additional setup after loading the view.
    }
	

	
	override func viewWillDisappear(_ animated: Bool) {
		userDefaults.set(timePicker.date, forKey: "alarmClockTime")
		userDefaults.set(daysSelected, forKey: "selectedDaysArray")
		if let rootController = UIApplication.shared.keyWindow?.rootViewController {
			let navigationController = rootController as! UINavigationController
			
			if userDefaults.bool(forKey: "budState"){
				if let radioViewContr = navigationController.topViewController  as? RadioViewController {
					radioViewContr.alertClock = timer
				}
			}
		}
	}
	
	override func remoteControlReceived(with event: UIEvent?) {
		guard let remoteControls = remoteAudioControls else {
			print("Remote controls not set")
			return
		}
		remoteControls.remoteControlReceived(with: event)
	}
	
	@IBAction func weekDaySelect(sender: UIButton){
		sender.isSelected = !sender.isSelected
		switch sender {
		case mondayBTN:
			if sender.isSelected{
				daysSelected[1] = true;
			}
			else{
				daysSelected[1] = false;
			}
		case tuesdayBTN:
			if sender.isSelected{
				daysSelected[2] = true;
			}
			else{
				daysSelected[2] = false;
			}
		case wednesdayBTN:
			if sender.isSelected{
				daysSelected[3] = true;
			}
			else{
				daysSelected[3] = false;
			}
		case thursdayBTN:
			if sender.isSelected{
				daysSelected[4] = true;
			}
			else{
				daysSelected[4] = false;
			}
		case fridayBTN:
			if sender.isSelected{
				daysSelected[5] = true;
			}
			else{
				daysSelected[5] = false;
			}
		case saturdayBTN:
			if sender.isSelected{
				daysSelected[6] = true;
			}
			else{
				daysSelected[6] = false;
			}
		case sundayBTN:
			if sender.isSelected{
				daysSelected[0] = true;
			}
			else{
				daysSelected[0] = false;
			}
		default:
			break;
		}
	}
	
	func appendDayToSchedule(additionalDays: Int){
		let timePickerValue = timePicker.date
		let calendar = Calendar.current
		let components = calendar.dateComponents([.hour, .minute], from: timePickerValue)
		var currentDateComponents = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: Date())
		currentDateComponents.hour = components.hour
		currentDateComponents.minute = components.minute
		let currentDate = Calendar.current.date(from: currentDateComponents)
		var budDate = Date(timeInterval: Double(additionalDays * 86400), since: currentDate ?? Date())
		if calendar.dateComponents([.second], from: Date(), to: budDate).second! < 0 && additionalDays == 0{
			budDate = Date(timeInterval: Double(7 * 86400), since: currentDate ?? Date())
		}
		budSchedule.append(budDate)
	}

	@IBAction func oneTapAction(_ sender: Any) {
		//Обновление состояния таймера по нажатию на экран
		if UserDefaults.standard.bool(forKey: "timerState"){
			UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "updateTimerDate")
		}
	}
	
    
	@IBAction func setBudButtonClick(_ sender: Any) {
        
        if !userDefaults.bool(forKey: "budState") && self.userDefaults.data(forKey: "PlayingSongObject") != nil{
			budSchedule = [Date]()
//			if player.isPlaying{
//				copyCurrentTrackToAlarmDir()
//			}
			
			setBudButton.setImage(UIImage(named: "budBlue"), for: .normal)
			userDefaults.set(true, forKey: "budState")
			
			let currentDayOfWeek = Calendar.current.component(.weekday, from: Date())
			let selectedDaysCount = daysSelected.filter{$0 == true}.count
			if selectedDaysCount == 0{
				daysSelected[currentDayOfWeek - 1] = true
				switch currentDayOfWeek-1{
				case 1:
					mondayBTN.isSelected = true
				case 2:
					tuesdayBTN.isSelected = true
				case 3:
					wednesdayBTN.isSelected = true
				case 4:
					thursdayBTN.isSelected = true
				case 5:
					fridayBTN.isSelected = true
				case 6:
					saturdayBTN.isSelected = true
				case 0:
					sundayBTN.isSelected = true
				default:
					break
				}
			}
			
			for (index, element) in daysSelected.enumerated() where element == true{
				if index + 1 >= currentDayOfWeek{
					let additionalDays = (index + 1) - currentDayOfWeek
					appendDayToSchedule(additionalDays: additionalDays)
				}
				else{
					let additionalDays = (index + 1) + (7 - currentDayOfWeek)
					appendDayToSchedule(additionalDays: additionalDays)
				}
			}
			userDefaults.set(budSchedule, forKey: "budSchedule")
			let minInterval = getMinDatediff(needDisplay: true)
			if minInterval[1] >= 0{
				startTimer(timeInterval: TimeInterval(minInterval[1]), minIndex: minInterval[0])
			}
			
			userDefaults.set(Int(Date().timeIntervalSince1970), forKey:  "setBudDate")
			
			let songObjectEncoded = self.userDefaults.data(forKey: "PlayingSongObject")
			let songObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
			DispatchQueue.global(qos: .utility).async{
				CopyManager.copyCurrentTrackToDir(song: songObject, copyTo: self.budTracksUrlString)
				print("Текущий трек скопирован в директорию будильника")
			}
		}
		else if self.userDefaults.data(forKey: "PlayingSongObject") == nil{
			infoLabel.text = "Нет прослушиваемого трека"
		}
		else{
			setBudButton.setImage(UIImage(named: "budGray"), for: .normal)
			userDefaults.set(false, forKey: "budState")
			budSchedule = [Date]()
			userDefaults.set(budSchedule, forKey: "budSchedule")
			userDefaults.synchronize()
			stopTimer()
		}
		userDefaults.set(timePicker.date, forKey: "alarmClockTime")
		userDefaults.set(daysSelected, forKey: "selectedDaysArray")
		
	}

	
	private func startTimer(timeInterval: TimeInterval, minIndex: Int){
		let queue = DispatchQueue.main
		timer?.cancel()
		timer = DispatchSource.makeTimerSource(queue: queue)
		
		//timer?.scheduleRepeating(deadline: .now() + .seconds(Int(timeInterval)), interval: timeInterval)
		timer?.scheduleOneshot(deadline: .now() + .seconds(Int(timeInterval)))
		timer?.setEventHandler{
			self.timerAction(currentMinIndex: minIndex)
		}
		timer?.resume()
	}
	
	private func stopTimer(){
		timer?.cancel()
		timer = nil
	}
	
	func timerAction(currentMinIndex: Int) -> Void{
		//Обновление расписания
		let newScheduleDate = Date(timeInterval: 7 * 86400, since: budSchedule[currentMinIndex]) // будильник ставится через неделю
		budSchedule[currentMinIndex] = newScheduleDate
		userDefaults.set(budSchedule, forKey: "budSchedule")
		let minInterval = getMinDatediff(needDisplay: false)
		stopTimer()
		startTimer(timeInterval: TimeInterval(minInterval[1]), minIndex: minInterval[0])
		
		//Проигрывание сохраненного трека если будильник установлен
		if userDefaults.bool(forKey: "budState"){
			let songObjectEncoded = self.userDefaults.data(forKey: "PlayingSongObject")
			let songObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
			let songFileName = songObject.path
//			var songFilePath = self.budTracksUrlString + songFileName!
//			let trackPath = NSURL(fileURLWithPath: songFilePath) as URL
			var trackPath: URL
			var isCorrect: Bool = false
			
			let fileManager = FileManager.default
			if fileManager.fileExists(atPath: self.tracksUrlString + songFileName!){
				trackPath = NSURL(fileURLWithPath: self.tracksUrlString + songFileName!) as URL
				isCorrect = true
			}
			else{
				isCorrect = CopyManager.copyTrackToCache(trackPath: budTracksUrlString + songFileName!, trackName: songFileName!)
				trackPath = NSURL(fileURLWithPath: self.tracksUrlString + songFileName!) as URL
			}
			
			if self.mainController != nil && isCorrect{
				self.mainController.playTrackByUrl(trackURL: trackPath, song: songObject, seekTo: 0)
			}
		}
	}
	
	func getMinDatediff(needDisplay: Bool) -> [Int]{
        var dateDiffs = [Int]()
        for date in budSchedule{
            let dateDiff = Calendar.current.dateComponents([.second], from: Date(), to: date).second
            dateDiffs.append(dateDiff!)
        }
        let minDiffIndex = dateDiffs.index(of: dateDiffs.min()!)
        let bellDate = Calendar.current.dateComponents([.day, .month, .hour, .minute], from: budSchedule[minDiffIndex!])
        let bellDay = String(bellDate.day!)
        let bellMonth = String(bellDate.month!)
        let bellHour = String(bellDate.hour!)
        let bellMinute = String(bellDate.minute!)
		
		if needDisplay{
			infoLabel.text = "Будильник зазвонит " + bellDay + "." + bellMonth + "\n в " + bellHour + ":" + bellMinute
		}
		
		let returnedArr = [minDiffIndex, dateDiffs[minDiffIndex!]]
		return returnedArr as! [Int]
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
	
	//Копирование играющего трека в директорию для будильника
	func copyCurrentTrackToAlarmDir(){
		let songObjectEncoded = self.userDefaults.data(forKey: "PlayingSongObject")
		let songObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
		let songFileName = songObject.path
		if songFileName != ""{
			let pathToTrack = tracksUrlString + songFileName!
			
			var isDir: ObjCBool = true
			if FileManager.default.fileExists(atPath: budTracksUrlString, isDirectory: &isDir){
				if !isDir.boolValue{
					createDirectory(path: budTracksUrlString)
				}
			}
			else{
				createDirectory(path: budTracksUrlString)
			}
			
			let items = try! FileManager.default.contentsOfDirectory(atPath: budTracksUrlString)
			// Удаляем старый трек
			for item in items{
				try! FileManager.default.removeItem(atPath: budTracksUrlString + item)
			}
			// Копируем новый
			try! FileManager.default.copyItem(atPath: pathToTrack, toPath: budTracksUrlString + songFileName!)
				
		}
	}
	
	func createDirectory(path: String){
		try! FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: false, attributes: nil)
	}
}
