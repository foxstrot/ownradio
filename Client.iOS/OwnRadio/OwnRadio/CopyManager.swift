//
//  CopyManager.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 06.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import Foundation

class CopyManager{
	
	static let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
	
	static public func copyCurrentTrackToDir(song: SongObject, copyTo: String){
		let songFileName = song.path
		if songFileName != ""{
			let pathToTrack = tracksUrlString + songFileName!
			var isDir: ObjCBool = true
			if FileManager.default.fileExists(atPath: copyTo, isDirectory: &isDir){
				if !isDir.boolValue{
					createDirectory(path: copyTo)
				}
			}
			else{
				createDirectory(path: copyTo)
			}
			
			let items = try! FileManager.default.contentsOfDirectory(atPath: copyTo)
			// Удаляем старый трек
			for item in items{
				try! FileManager.default.removeItem(atPath: copyTo + item)
			}
			// Копируем новый
			try! FileManager.default.copyItem(atPath: pathToTrack, toPath: copyTo + songFileName!)
			
		}
	}
	//Создает директорию
	static func createDirectory(path: String){
		try! FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: false, attributes: nil)
	}
	//Копирует трек из папки по пути trackPath в папку с кешем, возвращает полный путь к файлу в папке с кешем
	static func copyTrackToCache(trackPath: String, trackName: String) -> URL{
		let pathToTrackInCache = tracksUrlString + trackName
		try! FileManager.default.copyItem(atPath: trackPath, toPath: pathToTrackInCache)
		return NSURL(fileURLWithPath: pathToTrackInCache) as URL
	}
}
