//
//  LogDetailsTableViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 11.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit

class LogDetailsTableViewController: UITableViewController {

    public var logItem: LogObject!
    @IBOutlet weak var dateLbl: UILabel!
    @IBOutlet weak var descriptionLbl: UILabel!
    @IBOutlet weak var hasinternetLbl: UILabel!
    @IBOutlet weak var threadLbl: UILabel!
	@IBOutlet weak var errorMessageLbl: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()
		if logItem != nil {
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy.MM.dd HH:mm:ss"
            dateLbl.text = dateFormatter.string(from: self.logItem.eventDate)
            descriptionLbl.text = logItem.eventDescription.description
            hasinternetLbl.text = logItem.hasInternet.description
            var thread = matches(for: "number = \\d+, name = \\w+", in: logItem.eventThread.description)
            if thread.count == 1 {
                threadLbl.text = thread[0]
            } else {
                thread = matches(for: "number = \\d+", in: logItem.eventThread.description)
				if thread.count == 1 {
					threadLbl.text = thread[0]
				} else {
					threadLbl.text = ""
				}
            }
			errorMessageLbl.sizeToFit()
			errorMessageLbl.text = logItem.errorMessage.description
		}

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
		if logItem != nil && logItem.errorMessage != ""{
			return 5
		} else if logItem != nil && logItem.errorMessage == ""{
			return 4
		} else {return 0}
    }

    func matches(for regex: String, in text: String) -> [String] {
        do {
            let regex = try NSRegularExpression(pattern: regex)
            let nsString = text as NSString
            let results = regex.matches(in: text, range: NSRange(location: 0, length: nsString.length))
            return results.map { nsString.substring(with: $0.range)}
        } catch let error {
            print("invalid regex: \(error.localizedDescription)")
            return []
        }
    }

    /*
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)

        // Configure the cell...

        return cell
    }
    */

    /*
    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
