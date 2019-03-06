//
//  LogObject.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 11.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import Foundation

class LogObject: NSObject {

	public var eventDate: Date = Date()
	public var hasInternet: Bool = false
	public var eventThread: String = ""
	public var eventDescription: String = ""
	public var isError: Bool = false
	public var errorMessage: String = ""

//	override init() {
//
//	}
}
