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

class TimerViewController: UIViewController {

	
	@IBOutlet weak var circularSliderView: UIView!
	@IBOutlet weak var timeinfoLabel: UILabel!
	@IBOutlet weak var setTimerBtn: UIButton!
	@IBOutlet weak var setInfoLable: UILabel!
	
	
	let defaults = UserDefaults.standard
	var currentSliderValue = 0
	var slider: CircularSlider = CircularSlider()
	
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
			slider.endPointValue = CGFloat(Float(UserDefaults.standard.integer(forKey: "timerDurationSeconds")) / 60)
			timeinfoLabel.text = sliderValueToTime()
			let time = getRemainingTime()
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
		backgroundWorker = createTimerDispatchWorkItem()
        // Do any additional setup after loading the view.
    }
	

	
	//Получение осташегося времени работы таймера в виде интервала
	func getRemainingTimeInterval() ->Double{
		let currentDate = Date()
		let setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
		let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
		let remainingTimerDuration = Double(setTimerDate + timerDuration) - currentDate.timeIntervalSince1970
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

	var backgroundWorker = createTimerDispatchWorkItem()
	
	//Экшен нажатия на кнопку установки будильника
	@IBAction func btnSetTimerClick(_ sender: UIButton) {
		
		//Если таймер был остановлен пользователем, пересоздаем его
		if backgroundWorker.isCancelled{
			backgroundWorker = createTimerDispatchWorkItem()
		}
		
		//Если таймер не установлен, устанавливаем его
		if !defaults.bool(forKey: "timerState"){
			setTimerBtn.setImage(UIImage(named: "blueTimer"), for: .normal)
			let seconds = Float(Int(slider.endPointValue)) * 60
			defaults.set(true, forKey: "timerState")
			defaults.set(Int(Date().timeIntervalSince1970), forKey:  "setTimerDate")
			defaults.set(seconds, forKey:  "timerDurationSeconds")
			DispatchQueue.global(qos: .background).async(execute: backgroundWorker)
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
			DispatchQueue.global(qos: .background).async{
				self.backgroundWorker.cancel()
			}
			setInfoLable.text = "Таймер остановлен"
		}
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
			UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "setTimerDate")
		}
	}
	
	//Процесс проверки таймера
	
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}

func createTimerDispatchWorkItem() -> DispatchWorkItem{
	let workItem = DispatchWorkItem{
		while UserDefaults.standard.bool(forKey: "timerState"){
			let currentDate = Date()
			let setTimerDate = UserDefaults.standard.integer(forKey: "setTimerDate")
			let timerDuration = UserDefaults.standard.integer(forKey: "timerDurationSeconds")
			let remainingTimerDuration = Double(setTimerDate + timerDuration) - currentDate.timeIntervalSince1970
			if remainingTimerDuration <= 0{
				UserDefaults.standard.set(false, forKey: "timerState")
				UserDefaults.standard.set(0, forKey: "timerDurationSeconds")
				exit(0)
			}
		}
	}
	return workItem
}
