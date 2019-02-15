//
//  TimerViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 28.12.2018.
//  Copyright © 2018 Netvox Lab. All rights reserved.
//

import UIKit
import Foundation
import HGCircularSlider
import MediaPlayer

@available(iOS 10.0, *)
class TimerViewController: UIViewController {

	
	@IBOutlet weak var circularSliderView: UIView!
	@IBOutlet weak var timeinfoLabel: UILabel!
	@IBOutlet weak var setTimerBtn: UIButton!
	@IBOutlet weak var setInfoLable: UILabel!
	
	var player: AudioPlayerManager!
	
	let defaults = UserDefaults.standard
	var currentSliderValue = 0
	var slider: CircularSlider = CircularSlider()
	var timer: DispatchSourceTimer?
	
	var remoteAudioControls: RemoteAudioControls?
	
	
    override func viewDidLoad() {
        super.viewDidLoad()
		//Создаем слайдер
		createCircularSlider()
		//устанавливаем значение слайдера
		timeinfoLabel.text = sliderValueToTime()
		//Проверяем установлен ли таймер, если установлен отображаем его статус и меняем иконку кнопки
		setInfoLable.text = ""
		if defaults.bool(forKey: "timerState"){
			setTimerBtn.setImage(UIImage(named: "blueTimer"), for: .normal)
			
			let updateTimerDate = UserDefaults.standard.integer(forKey: "updateTimerDate")
			let time: String //getRemainingTime()
			
			if updateTimerDate > UserDefaults.standard.integer(forKey: "setTimerDate"){
				time = getRemainingTime(interval: updateTimerDate)
				slider.endPointValue = CGFloat(Float(getRemainingTimeInterval(interval: updateTimerDate)) / 60)
			}
			else{
				time = getRemainingTime()
				slider.endPointValue = CGFloat(Float(getRemainingTimeInterval()) / 60)
			}
			timeinfoLabel.text = sliderValueToTime()
			let splittedTime = time.split(separator: ":")
			if splittedTime.count == 2{
				setInfoLable.text = "Таймер установлен\nприложение закроется через " + splittedTime[0] + " ч, " + splittedTime[1] + " мин"
			}
			else{
				setInfoLable.text = "Таймер установлен\nприложение закроется через " + splittedTime[0] + " мин"
			}

		}else{
			setTimerBtn.setImage(UIImage(named: "grayTimer"), for: .normal)
		}
		
        // Do any additional setup after loading the view.
    }
	
	override func viewDidDisappear(_ animated: Bool) {
		super.viewDidDisappear(true)
		if let rootController = UIApplication.shared.keyWindow?.rootViewController {
			let navigationController = rootController as! UINavigationController
			
			if let radioViewContr = navigationController.topViewController  as? RadioViewController {
				radioViewContr.timer = self.timer
			}
		}
		defaults.synchronize()
	}
	
	override func remoteControlReceived(with event: UIEvent?) {
		guard let remoteControls = remoteAudioControls else {
			print("Remote controls not set")
			return
		}
		remoteControls.remoteControlReceived(with: event)
	}
	
//	func remoteControlRegister(){
//		UIApplication.shared.beginReceivingRemoteControlEvents()
//		let commandCenter = MPRemoteCommandCenter.shared()
//
//		let handler: (String) -> ((MPRemoteCommandEvent) -> (MPRemoteCommandHandlerStatus)) = { (name) in
//				return {(event) -> MPRemoteCommandHandlerStatus in dump("\(name) \(event.timestamp) \(event.command)")
//					return .success
//				}
//			}
//		commandCenter.nextTrackCommand.isEnabled = true
//		commandCenter.nextTrackCommand.addTarget(handler: handler("skipSong"))
//		
//		commandCenter.playCommand.isEnabled = true
//		commandCenter.playCommand.addTarget(handler: handler("resumeSong"))
//
//		commandCenter.pauseCommand.isEnabled = true
//		commandCenter.pauseCommand.addTarget(handler: handler("pauseSong"))
//
//		NotificationCenter.default.addObserver(self, selector: #selector(AudioPlayerManager.sharedInstance.onAudioSessionEvent(_:)), name: Notification.Name.AVAudioSessionInterruption, object: AVAudioSession.sharedInstance())
//	}
	
	//Получение осташегося времени работы таймера в виде интервала
	func getRemainingTimeInterval() ->Float{
		let currentDate = Date()
		let setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
		let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
		let remainingTimerDuration = Float(Double(setTimerDate + timerDuration) - currentDate.timeIntervalSince1970)
		return remainingTimerDuration
	}
	
	func getRemainingTimeInterval(interval: Int) ->Float{
		let currentDate = Date()
		let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
		let remainingTimerDuration = Float(Double(interval + timerDuration) - currentDate.timeIntervalSince1970)
		return remainingTimerDuration
	}
	
	//Перевод временного интервала в вид hh:mm
	func secondsToString(seconds: TimeInterval) -> String{
		let formatter = DateComponentsFormatter()
		formatter.allowedUnits = [.hour, .minute]
		formatter.unitsStyle = .positional
		let formattedString = formatter.string(from: seconds)
		return formattedString ?? "0"
	}
	
	func createCircularSlider(){
		
		circularSliderView.backgroundColor = .clear
		
		var frame = circularSliderView.frame
		frame.origin.x = 0
		frame.origin.y = 0
	
		let grayColor = UIColor(red: 0.83, green: 0.83, blue: 0.83, alpha: 1)
		let blueColor = UIColor(red: 0.08, green: 0.60, blue: 0.92, alpha: 1)
		
		slider = CircularSlider(frame: frame)
		slider.maximumValue = 240.0
		slider.trackColor = grayColor
		slider.trackFillColor = blueColor
		slider.diskColor = .clear
		slider.diskFillColor = .clear
		slider.backgroundColor = .clear
		slider.endThumbStrokeColor = .clear
		slider.endThumbTintColor = blueColor
		slider.endThumbStrokeHighlightedColor = blueColor
		slider.thumbRadius = 7
		slider.addTarget(self, action: #selector(sliderValueChangedAction), for: .valueChanged)
		circularSliderView.addSubview(slider)
	}
	//Экшен смены значения ползунка
	@objc func sliderValueChangedAction(sender: CircularSlider!){
		timeinfoLabel.text = sliderValueToTime()
	}

	//var backgroundWorker = createTimerDispatchWorkItem()
	
	//Экшен нажатия на кнопку установки будильника
	@IBAction func btnSetTimerClick(_ sender: UIButton) {
		
		//Если таймер был остановлен пользователем, пересоздаем его
//		if backgroundWorker.isCancelled{
//			backgroundWorker = createTimerDispatchWorkItem()
//		}
		
		//Если таймер не установлен, устанавливаем его
		if !defaults.bool(forKey: "timerState"){
			setTimerBtn.setImage(UIImage(named: "blueTimer"), for: .normal)
			let seconds = Float(Int(slider.endPointValue)) * 60
			defaults.set(true, forKey: "timerState")
			defaults.set(Int(Date().timeIntervalSince1970), forKey:  "setTimerDate")
			defaults.set(Int(Date().timeIntervalSince1970), forKey:  "updateTimerDate")
			defaults.set(seconds, forKey:  "timerDurationSeconds")
			startTimer(timeInterval: TimeInterval(seconds))
			let time = secondsToString(seconds: TimeInterval(seconds))
			let splittedTime = time.split(separator: ":")
			if splittedTime.count == 2{
				setInfoLable.text = "Таймер установлен\nприложение закроется через " + splittedTime[0] + " ч, " + splittedTime[1] + " мин"
			}
			else{
				setInfoLable.text = "Таймер установлен\nприложение закроется через " + splittedTime[0] + " мин"
			}
			
		}
		else{
			setTimerBtn.setImage(UIImage(named: "grayTimer"), for: .normal)
			defaults.set(false, forKey: "timerState")
			self.timer?.cancel()
			self.timer = nil
//			DispatchQueue.global(qos: .background).async{
//				self.backgroundWorker.cancel()
//			}
			setInfoLable.text = "Таймер остановлен"
		}
		defaults.synchronize()
	}
	
	//Получение оставшегося времени работы таймера в виде hh:mm
	func getRemainingTime() -> String{
		let currentDate = Date()
		let setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
		let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
		let remainingTimerDuration = Double(setTimerDate + timerDuration) - currentDate.timeIntervalSince1970
		let formattedString = secondsToString(seconds: remainingTimerDuration)
		return formattedString ?? "0"
	}
	
	func getRemainingTime(interval: Int) -> String{
		let currentDate = Int(Date().timeIntervalSince1970)
		//let setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
		let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
		let remainingTimerDuration = interval + timerDuration - currentDate
		let formattedString = secondsToString(seconds: TimeInterval(remainingTimerDuration))
		return formattedString ?? "0"
	}
	
	//Перевод значения слайдера в формат hh:mm
	func sliderValueToTime() -> String{
		let formatter = DateComponentsFormatter()
		formatter.allowedUnits = [.hour, .minute]
		formatter.unitsStyle = .positional
		let formattedString = formatter.string(from: TimeInterval(Float(slider.endPointValue) * 60))
		
		return formattedString ?? "0"
	}

	@IBAction func oneTapAction(_ sender: Any) {
		//Обновление состояния таймера по нажатию на экран
		if UserDefaults.standard.bool(forKey: "timerState"){
			UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "updateTimerDate")
		}
	}
	private func startTimer(timeInterval: TimeInterval){
		let queue = DispatchQueue(label: "AlertClockTimer", attributes: .concurrent)
		timer?.cancel()
		timer = DispatchSource.makeTimerSource(queue: queue)
		
		//		timer?.scheduleRepeating(deadline: .now() + .seconds(Int(timeInterval)), interval: timeInterval)
		timer?.scheduleOneshot(deadline: .now() + .seconds(Int(timeInterval)))
		timer?.setEventHandler{
			self.timerAction()
		}
		timer?.resume()
		
	}
	
	func timerAction(){
		if UserDefaults.standard.bool(forKey: "timerState") && !(timer?.isCancelled)!{
			UserDefaults.standard.synchronize()
			var setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
			let updateTimerDate = UserDefaults.standard.integer(forKey: "updateTimerDate")
			
			if updateTimerDate > setTimerDate{ //Если была активность пользователя, обновляем дату срабатывания таймера
				setTimerDate = updateTimerDate
			}
			let timeInterval = Float(Int(slider.endPointValue)) * 60
			if (Int(Date().timeIntervalSince1970) - Int(timeInterval)) >= setTimerDate{ //Если активности пользователя не было, таймер срабатывает, если была, переставляем на тот же интервал
				UserDefaults.standard.synchronize()
				if UserDefaults.standard.bool(forKey: "trackPlayingNow"){
					UserDefaults.standard.set(true, forKey: "playingInterrupted")
					if self.player != nil{
						UserDefaults.standard.set(self.player.playerItem.currentTime().seconds, forKey: "playingDuration")
					}
				}
				UserDefaults.standard.set(false, forKey: "timerState")
				UserDefaults.standard.set(0, forKey: "timerDurationSeconds")
				UserDefaults.standard.set(false, forKey: "wasMalfunction")
				UserDefaults.standard.synchronize()
				exit(0)
			}
			else{
				let datesDifferent = timeInterval - Float(Int(Date().timeIntervalSince1970) - updateTimerDate)
				startTimer(timeInterval: TimeInterval(datesDifferent))
			}
		}
		else{
			timer?.cancel()
		}
		
	}
	
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
