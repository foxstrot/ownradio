//
//  AlertClockViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 27.12.2018.
//  Copyright © 2018 Netvox Lab. All rights reserved.
//

import UIKit

class AlertClockViewController: UIViewController {

	@IBOutlet weak var timePicker: UIDatePicker!
    
	@IBOutlet weak var mondayBTN: UIButton!
	@IBOutlet weak var saturdayBTN: UIButton!
	@IBOutlet weak var sundayBTN: UIButton!
	@IBOutlet weak var tuesdayBTN: UIButton!
	@IBOutlet weak var wednesdayBTN: UIButton!
	@IBOutlet weak var thursdayBTN: UIButton!
	@IBOutlet weak var fridayBTN: UIButton!
	
	let userDefaults = UserDefaults.standard
	var daysSelected = [Bool]()
    var weekDays = ["ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"]
	var selectedTime = Date()
	
	override func viewDidLoad() {
        super.viewDidLoad()
		let dateformatter = DateFormatter()
		dateformatter.timeStyle = DateFormatter.Style.short
		timePicker.date = (userDefaults.object(forKey: "alarmClockTime") as? Date ?? Date())!
		
		daysSelected = userDefaults.array(forKey: "selectedDaysArray") as? [Bool] ?? [Bool]([false, false, false, false, false, false, false])
		if daysSelected.count > 0{
			for (index, value) in daysSelected.enumerated(){
				switch index{
				case 0:
					mondayBTN.isSelected = value
				case 1:
					tuesdayBTN.isSelected = value
				case 2:
					wednesdayBTN.isSelected = value
				case 3:
					thursdayBTN.isSelected = value
				case 4:
					fridayBTN.isSelected = value
				case 5:
					saturdayBTN.isSelected = value
				case 6:
					sundayBTN.isSelected = value
				default:
					break
				}
				
			}
		}

        // Do any additional setup after loading the view.
    }

	
	override func viewWillDisappear(_ animated: Bool) {
		userDefaults.set(timePicker.date, forKey: "alarmClockTime")
		userDefaults.set(daysSelected, forKey: "selectedDaysArray")
	}
	@IBAction func weekDaySelect(sender: UIButton){
		sender.isSelected = !sender.isSelected
		
		switch sender {
		case mondayBTN:
			if sender.isSelected{
				daysSelected[0] = true;
			}
			else{
				daysSelected[0] = false;
			}
		case tuesdayBTN:
			if sender.isSelected{
				daysSelected[1] = true;
			}
			else{
				daysSelected[1] = false;
			}
		case wednesdayBTN:
			if sender.isSelected{
				daysSelected[2] = true;
			}
			else{
				daysSelected[2] = false;
			}
		case thursdayBTN:
			if sender.isSelected{
				daysSelected[3] = true;
			}
			else{
				daysSelected[3] = false;
			}
		case fridayBTN:
			if sender.isSelected{
				daysSelected[4] = true;
			}
			else{
				daysSelected[4] = false;
			}
		case saturdayBTN:
			if sender.isSelected{
				daysSelected[5] = true;
			}
			else{
				daysSelected[5] = false;
			}
		case sundayBTN:
			if sender.isSelected{
				daysSelected[6] = true;
			}
			else{
				daysSelected[6] = false;
			}
		default:
			break;
		}
		
		
	}

	@IBAction func tapEvent(_ sender: Any) {
		userDefaults.set(timePicker.date, forKey: "alarmClockTime")
	}
	
	@IBAction func isChanged(_ sender: Any) {
		UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "setTimerDate")
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
