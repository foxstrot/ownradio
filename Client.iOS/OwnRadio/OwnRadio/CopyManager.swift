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
			do{
				let items = try FileManager.default.contentsOfDirectory(atPath: copyTo)
				// Удаляем старый трек
				for item in items{
					if FileManager.default.fileExists(atPath: copyTo + item){
						try FileManager.default.removeItem(atPath: copyTo + item)
					}
				}
				// Копируем новый
				if FileManager.default.fileExists(atPath: pathToTrack){
					if !FileManager.default.fileExists(atPath: copyTo + songFileName!){
						try FileManager.default.copyItem(atPath: pathToTrack, toPath: copyTo + songFileName!)
					}
					else{
						print("Файл существует в целевой директории")
					}
				}
				else{
					print("Файл не существует в исходной директории")
				}
			}
			catch{
				print("Трек не скопирован")
			}
			
		}
	}
	//Создает директорию
	static func createDirectory(path: String){
		try! FileManager.default.createDirectory(atPath: path, withIntermediateDirectories: false, attributes: nil)
	}
	//Копирует трек из папки по пути trackPath в папку с кешем, возвращает полный путь к файлу в папке с кешем
	static func copyTrackToCache(trackPath: String, trackName: String) -> Bool{
		let pathToTrackInCache = tracksUrlString + trackName
		if FileManager.default.fileExists(atPath: pathToTrackInCache) && !FileManager.default.fileExists(atPath: trackPath){
			try! FileManager.default.copyItem(atPath: trackPath, toPath: pathToTrackInCache)
			return true
		}
		else{
			return false
		}
	}
}
