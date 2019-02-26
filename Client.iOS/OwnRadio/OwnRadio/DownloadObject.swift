//
//  DownloadObject.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 19.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import Foundation

class DownloadObject: NSObject {
	var infoDict: [String: AnyObject]
	var audioPath: URL
	var destinationPath: URL

	init(audioPath: URL, destinationPath: URL, infoDict: [String: AnyObject]) {
		self.audioPath = audioPath
		self.destinationPath = destinationPath
		self.infoDict = infoDict
		print("Download object intitalized \(audioPath.absoluteString)")
	}
	deinit {
		print("Download object deintitalized \(audioPath.absoluteString)")
	}
}
