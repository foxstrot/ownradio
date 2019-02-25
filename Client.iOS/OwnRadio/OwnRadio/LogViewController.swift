//
//  LogViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 11.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit

class LogViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

	@IBOutlet weak var tableView: UITableView!
	@IBOutlet weak var errorsSwitch: UISwitch!

    var logRecords: [LogObject] = []
	var errorsRecords: [LogObject] = []
	var timer: DispatchSourceTimer!

    let cellReuseIdentifier = "cell"
	var tappedItem: LogObject!
    override func viewDidLoad() {
        super.viewDidLoad()
        logRecords = CoreDataManager.instance.getLogRecords()
        tableView.delegate = self
        tableView.dataSource = self

        // Do any additional setup after loading the view.
    }

	override func viewWillAppear(_ animated: Bool) {
		timer = DispatchSource.makeTimerSource()
		timer.scheduleRepeating(deadline: .now(), interval: .seconds(1))
		timer.setEventHandler(handler: {
			self.logRecords = CoreDataManager.instance.getLogRecords()
			DispatchQueue.main.async {
				self.tableView.reloadData()
			}
		})
		timer.resume()
	}

	override func viewDidDisappear(_ animated: Bool) {
		timer.cancel()
	}

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		if errorsSwitch.isOn {
			errorsRecords = onlyErrors()
			return self.errorsRecords.count
		} else {
			return self.logRecords.count
		}
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: LogTableViewCell = self.tableView.dequeueReusableCell(withIdentifier: cellReuseIdentifier) as! LogTableViewCell
		let dateFormatter = DateFormatter()
		dateFormatter.dateFormat = "yyyy.MM.dd HH:mm:ss"

		if errorsSwitch.isOn {
			cell.descriptionLbl.text = self.errorsRecords[indexPath.row].eventDescription
			cell.dateLbl.text = dateFormatter.string(from: self.errorsRecords[indexPath.row].eventDate)
			if errorsRecords[indexPath.row].isError {
				cell.backgroundColor = UIColor(red: 1.00, green: 0.34, blue: 0.34, alpha: 1.0)
			} else {
				if errorsRecords[indexPath.row].eventDescription == "Приложение запущено"{
					cell.backgroundColor = UIColor(red: 1.00, green: 0.97, blue: 0.0, alpha: 1.0)
				} else {
					cell.backgroundColor = UIColor(red: 0.44, green: 1.00, blue: 0.60, alpha: 1.0)
				}
			}
		} else {
			cell.descriptionLbl.text = self.logRecords[indexPath.row].eventDescription
			cell.dateLbl.text = dateFormatter.string(from: self.logRecords[indexPath.row].eventDate)
			if logRecords[indexPath.row].isError {
				cell.backgroundColor = UIColor(red: 1.00, green: 0.34, blue: 0.34, alpha: 1.0)
			} else {
				if logRecords[indexPath.row].eventDescription == "Приложение запущено"{
					cell.backgroundColor = UIColor(red: 1.00, green: 0.97, blue: 0.0, alpha: 1.0)
				} else {
					cell.backgroundColor = UIColor(red: 0.44, green: 1.00, blue: 0.60, alpha: 1.0)
				}

			}
		}
        return cell
    }
	override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
		if segue.identifier == "logDetails"{
			if let nextViewController = segue.destination as? LogDetailsTableViewController {
				nextViewController.logItem = self.tappedItem
			}
		}
	}

	func onlyErrors() -> [LogObject] {
		var errorsArray: [LogObject] = []
		if logRecords.count > 0 {
			for log in logRecords {
				if log.isError {
					errorsArray.append(log)
				}
			}
			return errorsArray
		} else {
			return errorsArray
		}
	}

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print("Tapped \(indexPath.row)")
		if errorsSwitch.isOn {
			tappedItem = errorsRecords[indexPath.row]
		} else {
			tappedItem = logRecords[indexPath.row]
		}

		self.performSegue(withIdentifier: "logDetails", sender: self)
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    @IBAction func switchClick(_ sender: Any) {
		tableView.reloadData()
    }

    @IBAction func clearLogClick(_ sender: Any) {
        CoreDataManager.instance.deleteLogRecords()
        CoreDataManager.instance.saveContext()
		logRecords = CoreDataManager.instance.getLogRecords()
		tableView.reloadData()

    }
}
