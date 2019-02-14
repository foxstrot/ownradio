//
//  LogTableViewCell.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 11.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit

class LogTableViewCell: UITableViewCell {


    @IBOutlet weak var dateLbl: UILabel!
    @IBOutlet weak var descriptionLbl: UILabel!
    @IBOutlet weak var threadLbl: UILabel!
    @IBOutlet weak var networkLbl: UILabel!
    
	
    
	
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
