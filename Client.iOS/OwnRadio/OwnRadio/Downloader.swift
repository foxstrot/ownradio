//
//  Downloader.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 12/5/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.
//
//	Download track in cache

import Foundation
import UIKit

class Downloader: NSObject {

	static let sharedInstance = Downloader()
	//	var taskQueue: OperationQueue?
	let baseURL = URL(string: "http://api.ownradio.ru/v5/tracks/")
	let applicationSupportPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
	let tracksPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!.appendingPathComponent("Tracks/")
	let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
	var fillStarted = false
//	let limitMemory =  UInt64(DiskStatus.freeDiskSpaceInBytes / 3)
	var maxMemory = UInt64(1000000000)
	var memoryBuffer = UInt()

	var downloadTasks = [String: DownloadObject]()
	var requestCount = 0
	var deleteCount = 0
	var maxRequest = 9
	var completionHandler:(() -> Void)?
	let coreInstance = CoreDataManager.instance

	lazy var urlSession: URLSession = {
		let config = URLSessionConfiguration.background(withIdentifier: "DownloadSession")
		config.isDiscretionary = true
		config.sessionSendsLaunchEvents = true
		return URLSession(configuration: config, delegate: self, delegateQueue: nil)
	}()

	func load(isSelfFlag: Bool, complition: @escaping (() -> Void)) {
		print("call load")

		let memoryAvailable = UInt64(DiskStatus.folderSize(folderPath: tracksUrlString)) + UInt64(DiskStatus.freeDiskSpaceInBytes)
		let percentage = Double((UserDefaults.standard.object(forKey: "maxMemorySize") as? Double)! / 100)
		maxMemory = UInt64(Double(memoryAvailable) * percentage )
//		maxMemory = 100000000
//		if limitMemory < 1000000000 * ((UserDefaults.standard.object(forKey: "maxMemorySize") as? UInt64)! / 10) {
//			maxMemory = limitMemory
//		} else {
//			let memoryAvailable = UInt64(DiskStatus.folderSize(folderPath: tracksUrlString)) + UInt64(DiskStatus.freeDiskSpaceInBytes)
//			let percentage = Double((UserDefaults.standard.object(forKey: "maxMemorySize") as? Double)! / 100)
//			maxMemory = UInt64(Double(memoryAvailable) * percentage)
//		}
//		+ DiskStatus.folderSize(folderPath: tracksUrlString)))
//		* (DiskStatus.freeDiskSpaceInBytes)

		//если треки занимают больше места, чем максимально допустимо -
		//удаляем "лишние" треки - в первую очередь прослушанные, затем, если необходимо - самые старые из загруженных
		while (!isSelfFlag && DiskStatus.folderSize(folderPath: tracksUrlString) >= maxMemory) {
			// получаем трек проиграный большее кол-во раз
			let song = CoreDataManager.instance.getOldTrack(onlyListen: true) as! [SongObject]
			// получаем путь файла
			guard song != nil && song[0].trackID != nil else {
				self.createPostNotificationSysInfo(message: "Не найден трек для удаления")
				return
			}
			self.createPostNotificationSysInfo(message: "Память заполнена. Удаляем трек \(self.deleteCount)")
			let songObjectEncoded = UserDefaults.standard.data(forKey: "interruptedSongObject")
			let currentSongObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
			if (song[0].trackID.isEqual(currentSongObject.trackID)) != true {
				deleteOldTrack(song: song[0])
			} else {
				DispatchQueue.main.async {
					CoreDataManager.instance.setLogRecord(eventDescription: "Удаляемый трек сейчас играет, uuid = \(String(describing: song[0].trackID.description))", isError: true, errorMessage: "")
					CoreDataManager.instance.saveContext()
					if song.count > 1{
						self.deleteOldTrack(song: song[1])
					}
				}
			}

		}

		//проверка подключения к интернету
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			self.requestCount = 0
			return
		}

		//делаем 10 попыток скачивания треков, если место свободное место закончилось, но есть прослушанные треки - удаляем их и загружаем новые, иначе перестаем пытаться скачать
		if DiskStatus.folderSize(folderPath: tracksUrlString) < maxMemory {
			self.deleteCount = 0
			//получаем trackId следующего трека и информацию о нем
			self.completionHandler = complition
			ApiService.shared.getTrackIDFromServer(requestCount: self.requestCount) { [unowned self] (dict) in
				guard dict["id"] != nil else {
					return
				}
				let trackURL = self.baseURL?.appendingPathComponent(dict["id"] as! String).appendingPathComponent((UIDevice.current.identifierForVendor?.uuidString.lowercased())!)
				if let audioUrl = trackURL {
					//задаем директорию для сохранения трека
					let destinationUrl = self.tracksPath.appendingPathComponent(dict["id"] as! String)
					//если этот трек не еще не загружен - загружаем трек
					//						let mp3Path = destinationUrl.appendingPathExtension("mp3")
					guard FileManager.default.fileExists(atPath: destinationUrl.path ) == false else {
						self.createPostNotificationSysInfo(message: "Трек уже загружен - пропустим")
						return
					}
					//добавляем трек в очередь загрузки
					let downloadRequest = self.createDownloadTask(audioUrl: audioUrl, destinationUrl: destinationUrl, dict: dict, trackId: dict["id"] as! String)
					downloadRequest.resume()
					//						}
				}
			}

		} else {
			// если память заполнена удаляем трек
			if self.deleteCount < 9 {
				if self.completionHandler != nil {
					self.completionHandler!()
				}
				self.deleteCount += 1

				// получаем трек проиграный большее кол-во раз
				let song = CoreDataManager.instance.getOldTrack(onlyListen: false) as! [SongObject]
				// получаем путь файла
				guard song != nil && song[0].trackID != nil else {
					self.createPostNotificationSysInfo(message: "Память заполнена, нет прослуш треков")
					self.requestCount = 0
					return
				}
				self.createPostNotificationSysInfo(message: "Память заполнена. Удаляем трек \(self.deleteCount)")

				let songObjectEncoded = UserDefaults.standard.data(forKey: "interruptedSongObject")
				let currentSongObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
				if (song[0].trackID.isEqual(currentSongObject.trackID)) != true {
					deleteOldTrack(song: song[0])
				} else {
//					DispatchQueue.main.async {
//						CoreDataManager.instance.setLogRecord(eventDescription: "Удаляемый трек сейчас играет, uuid = \(String(describing: song?.trackID.description))", isError: false, errorMessage: "")
//						CoreDataManager.instance.saveContext()
//					}
					if song.count > 1{
						deleteOldTrack(song: song[1])
					}
				}

//				self.load (isSelfFlag: true){
//
//				}

			} else {
				self.deleteCount = 0
			}

		}
	}

	func createDownloadTask(audioUrl: URL, destinationUrl: URL, dict: [String: AnyObject], trackId: String) -> URLSessionDownloadTask {
		print("call createDownloadTask")
		let downloadItem = DownloadObject(audioPath: audioUrl, destinationPath: destinationUrl, infoDict: dict)
		DispatchQueue.main.sync {
			downloadTasks[trackId] = (downloadItem)
		}

		return urlSession.downloadTask(with: audioUrl)
//		print(audioUrl)
//		return URLSession.shared.downloadTask(with: audioUrl, completionHandler: { (location, response, error) -> Void in
//			guard error == nil else {
//
//				self.createPostNotificationSysInfo(message: error.debugDescription)
//				CoreDataManager.instance.setLogRecord(eventDescription: "Download failed, url: \(audioUrl.absoluteString)", isError: true, errorMessage: error.debugDescription)
//				CoreDataManager.instance.saveContext()
//				return
//			}
//			guard let newLocation = location, error == nil else {return }
//
//			if let httpResponse = response as? HTTPURLResponse {
//				if httpResponse.statusCode == 200 {
//					do {
//						let file = NSData(contentsOf: newLocation)
//						let mp3Path = destinationUrl.appendingPathExtension("mp3")
//						guard FileManager.default.fileExists(atPath: mp3Path.path ) == false else {
//							self.createPostNotificationSysInfo(message: "MP3 file exist")
//							CoreDataManager.instance.setLogRecord(eventDescription: "MP3 file exist", isError: true, errorMessage: mp3Path.path.description)
//							CoreDataManager.instance.saveContext()
//							return
//						}
//
//						//сохраняем трек
//						//задаем конечных путь хранения файла (добавляем расширение)
//						let endPath = destinationUrl.appendingPathExtension("mp3")
//						try file?.write(to: endPath, options:.noFileProtection)
//
//						//Проверяем, полностью ли скачан трек
//						if let contentLength = Int(httpResponse.allHeaderFields["Content-Length"] as! String) {
//							if file!.length != contentLength || file!.length == 0 {
//								if FileManager.default.fileExists(atPath: mp3Path.path) {
//									do{
//										// удаляем обьект по пути
//										try FileManager.default.removeItem(atPath: mp3Path.path)
//										self.createPostNotificationSysInfo(message: "Файл с длиной = \(file!.length), ContentLength = \(contentLength) удален")
//									}
//									catch {
//										CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при удалении недокачанного трека", isError: true, errorMessage: error.localizedDescription)
//										CoreDataManager.instance.saveContext()
//										print("Ошибка при удалении недокачанного трека")
//									}
//								}
//								return
//							}
//						}
//						//сохраняем информацию о файле в базу данных
//
//						guard FileManager.default.fileExists(atPath: mp3Path.absoluteString ) == false else {
//							self.createPostNotificationSysInfo(message: "MP3 file exist")
//							return
//						}
//
//						let trackEntity = TrackEntity()
//
//						trackEntity.path = String(describing: endPath.lastPathComponent)
//						trackEntity.countPlay = 0
//						trackEntity.artistName = dict["artist"] as? String
//						trackEntity.trackName = dict["name"] as? String
//						trackEntity.trackLength = NSString(string: dict["length"] as! String).doubleValue
//						trackEntity.recId = dict["id"] as! String?
//						trackEntity.playingDate = NSDate.init(timeIntervalSinceNow: -315360000.0042889)
//
//						CoreDataManager.instance.saveContext()
//
//
//						self.createPostNotificationSysInfo(message: "Трек (\(self.requestCount+1)) загружен \(trackEntity.recId ?? "")")
//						if self.requestCount < self.maxRequest {
//							if self.completionHandler != nil {
//								self.completionHandler!()
//							}
//							self.requestCount += 1
//							self.load(isSelfFlag: true, complition: self.completionHandler!)
//
//						} else {
//							if self.completionHandler != nil {
//								self.completionHandler!()
//							}
//							self.requestCount = 0
//							self.maxRequest = 9
//						}
//
//						//				complition()
//						CoreDataManager.instance.setLogRecord(eventDescription: "Информация о треке сохранена в БД", isError: false, errorMessage: "")
//						CoreDataManager.instance.saveContext()
//						print("File moved to documents folder")
//
//					} catch {
//						CoreDataManager.instance.setLogRecord(eventDescription: "Трек не скачан " + error.localizedDescription, isError: true, errorMessage: error.localizedDescription)
//						CoreDataManager.instance.saveContext()
//						print(error.localizedDescription)
//					}
//				}
//			}
//		})
	}

	func createPostNotificationSysInfo (message: String) {
		NotificationCenter.default.post(name: NSNotification.Name(rawValue: "updateSysInfo"), object: nil, userInfo: ["message": message])
	}

	// удаление трека если память заполнена
	func deleteOldTrack (song: SongObject?) {
		print("call deleteOldTrack")
//		DispatchQueue.main.async {
//			CoreDataManager.instance.setLogRecord(eventDescription: "Удаление старого трека", isError: false, errorMessage: "")
//			CoreDataManager.instance.saveContext()
//		}

		let path = self.tracksUrlString.appending((song?.path)!)
		self.createPostNotificationSysInfo(message: "Удаляем \(song!.trackID.description)")
		print("Удаляем \(song!.trackID.description)")
//		DispatchQueue.main.async {
//			CoreDataManager.instance.setLogRecord(eventDescription: "Удаляем старый трек \(song!.trackID.description)", isError: false, errorMessage: "")
//			CoreDataManager.instance.saveContext()
//		}
		let songObjectEncoded = UserDefaults.standard.data(forKey: "interruptedSongObject")
		let currentSongObject = try! PropertyListDecoder().decode(SongObject.self, from: songObjectEncoded!)
		if (song?.trackID.isEqual(currentSongObject.trackID)) == true {
			print("Не удаляем сейчас играющий трек")
//			DispatchQueue.main.async {
//				CoreDataManager.instance.setLogRecord(eventDescription: "Старый удаляемый трек играет сейчас", isError: false, errorMessage: "")
//				CoreDataManager.instance.saveContext()
//			}
		} else {
			if FileManager.default.fileExists(atPath: path) {
				do {
					// удаляем обьект по пути
					try FileManager.default.removeItem(atPath: path)
					self.createPostNotificationSysInfo(message: "Файл успешно удален")
					print("Файл успешно удален")
//					DispatchQueue.main.async {
//						CoreDataManager.instance.setLogRecord(eventDescription: "Старый трек удален", isError: false, errorMessage: "")
//						CoreDataManager.instance.saveContext()
//					}
				} catch {
					self.createPostNotificationSysInfo(message: "Ошибка удаления трека: \(error)")
					print("Ошибка удаления трека: \(error)")
					if UserDefaults.standard.bool(forKey: "writeLog"){
						CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка удаления старого трека: " + error.localizedDescription, isError: true, errorMessage: error.localizedDescription)
						CoreDataManager.instance.saveContext()
					}
				}
			} else {
				self.createPostNotificationSysInfo(message: "Трек уже удалён с устройства")
				print("Трек уже удалён с устройства")
				CoreDataManager.instance.setLogRecord(eventDescription: "Старый трек уже удален", isError: false, errorMessage: "")
				CoreDataManager.instance.saveContext()
			}
			// удаляем трек с базы
			//			CoreDataManager.instance.managedObjectContext.performAndWait {
			CoreDataManager.instance.deleteTrackFor(trackID: (song?.trackID)!)
			CoreDataManager.instance.setLogRecord(eventDescription: "Трек удален из БД, id: " + (song?.trackID.description)!, isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
			//			}

			//		}
		}
	}

	func fillCache () {
//		let limitMemory =  UInt64(DiskStatus.freeDiskSpaceInBytes / 2)
//		let maxMemory = 1000000000 * (UserDefaults.standard.object(forKey: "maxMemorySize") as? UInt64)!
		let memoryAvailable = UInt64(DiskStatus.folderSize(folderPath: tracksUrlString)) + UInt64(DiskStatus.freeDiskSpaceInBytes)
		let percentage = Double((UserDefaults.standard.object(forKey: "maxMemorySize") as? Double)! / 100)
		maxMemory = UInt64(Double(memoryAvailable) * percentage)
//		maxMemory = 100000000
		let folderSize = DiskStatus.folderSize(folderPath: tracksUrlString)
		DispatchQueue.main.async {
			CoreDataManager.instance.setLogRecord(eventDescription: "Заполнение кеша, объем: " + DiskStatus.GBFormatter(Int64(folderSize)).description + ", свободное место: " + self.maxMemory.description + ", разрешенный процент: " + percentage.description, isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
		}
		UserDefaults.standard.set(true, forKey: "runCaching")
		if DiskStatus.folderSize(folderPath: tracksUrlString) <= maxMemory {
			self.load (isSelfFlag: false) {
				//sleep(2)
				self.fillCache()
			}
		} else {
			if UserDefaults.standard.bool(forKey: "writeLog"){
				DispatchQueue.main.async {
					CoreDataManager.instance.setLogRecord(eventDescription: "Недостаточно памяти для заполнения кеша", isError: true, errorMessage: "")
					CoreDataManager.instance.saveContext()
				}
			}
		}
	}

}
extension Downloader: URLSessionDownloadDelegate {
	func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
		guard downloadTask.error == nil else {
			self.createPostNotificationSysInfo(message: downloadTask.error.debugDescription)
			if UserDefaults.standard.bool(forKey: "writeLog"){
				DispatchQueue.main.async {
					CoreDataManager.instance.setLogRecord(eventDescription: "Download failed", isError: true, errorMessage: downloadTask.error.debugDescription)
					CoreDataManager.instance.saveContext()
				}
			}
			return
		}

//		print(location.absoluteString)
		if let httpResponse = downloadTask.response as? HTTPURLResponse {
			if httpResponse.statusCode == 200 {
				do {
					let filename = (httpResponse.allHeaderFields["Etag"] as! String)
					let trackId = filename.replacingOccurrences(of: ".mp3", with: "")
					if UUID(uuidString: trackId) != nil {
						let downloadItem = self.downloadTasks[trackId]
						if downloadItem != nil {
							let file = NSData(contentsOf: location)
							let mp3Path = downloadItem!.destinationPath.appendingPathExtension("mp3")
							guard FileManager.default.fileExists(atPath: mp3Path.path ) == false else {
								self.createPostNotificationSysInfo(message: "MP3 file exist")
								DispatchQueue.main.async {
									CoreDataManager.instance.setLogRecord(eventDescription: "MP3 file exist", isError: true, errorMessage: mp3Path.path.description)
									CoreDataManager.instance.saveContext()
								}
								return
							}
							try file?.write(to: mp3Path, options: .noFileProtection)

							if let contentLength = Int(httpResponse.allHeaderFields["Content-Length"] as! String) {
								if file!.length != contentLength || file!.length == 0 {
									if FileManager.default.fileExists(atPath: mp3Path.path) {
										do {
											// удаляем обьект по пути
											try FileManager.default.removeItem(atPath: mp3Path.path)
											self.createPostNotificationSysInfo(message: "Файл с длиной = \(file!.length), ContentLength = \(contentLength) удален")
										} catch {
											DispatchQueue.main.async {
												CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при удалении недокачанного трека", isError: true, errorMessage: error.localizedDescription)
												CoreDataManager.instance.saveContext()
											}
											print("Ошибка при удалении недокачанного трека")
										}
									}
									return
								}
							}
							guard FileManager.default.fileExists(atPath: mp3Path.absoluteString ) == false else {
								self.createPostNotificationSysInfo(message: "MP3 file exist")
								return
							}
							DispatchQueue.main.async {
								let trackEntity = TrackEntity()

								trackEntity.path = String(describing: mp3Path.lastPathComponent)
								trackEntity.countPlay = 0
								trackEntity.artistName = downloadItem!.infoDict["artist"] as? String
								trackEntity.trackName = downloadItem!.infoDict["name"] as? String
								trackEntity.trackLength = NSString(string: downloadItem!.infoDict["length"] as! String).doubleValue
								trackEntity.recId = downloadItem!.infoDict["id"] as! String?
								trackEntity.playingDate = NSDate.init(timeIntervalSinceNow: -315360000.0042889)
								CoreDataManager.instance.saveContext()
								self.createPostNotificationSysInfo(message: "Трек (\(self.requestCount+1)) загружен \(trackEntity.recId ?? "")")
								self.downloadTasks.removeValue(forKey: trackId)
							}
							if self.requestCount < self.maxRequest {
								if self.completionHandler != nil {
									self.completionHandler!()
								}
								if !UserDefaults.standard.bool(forKey: "runCaching") {
									self.load(isSelfFlag: false) {
										DispatchQueue.main.sync {
											self.requestCount += 1
										}
										DispatchQueue.main.async {
											CoreDataManager.instance.setLogRecord(eventDescription: "Трек доигран до конца, догрузка", isError: false, errorMessage: "")
											CoreDataManager.instance.saveContext()
										}
									}
								}
							} else {
								if self.completionHandler != nil {
									self.completionHandler!()
								}
								DispatchQueue.main.sync {
									self.requestCount = 0
									self.maxRequest = 9
								}

							}

							//				complition()
							DispatchQueue.main.async {
								CoreDataManager.instance.setLogRecord(eventDescription: "Информация о треке сохранена в БД", isError: false, errorMessage: "")
								CoreDataManager.instance.saveContext()
							}
							print("File moved to documents folder")
							//self.downloadTasks.removeValue(forKey: trackId)
						} else {
							DispatchQueue.main.async {
								CoreDataManager.instance.setLogRecord(eventDescription: "Трек не найден в словаре полученых треков", isError: true, errorMessage: "trackid: \(trackId)")
								CoreDataManager.instance.saveContext()
							}
						}
					} else {
						DispatchQueue.main.async {
							CoreDataManager.instance.setLogRecord(eventDescription: "Некорректный id трека", isError: true, errorMessage: "trackid: \(filename.replacingOccurrences(of: ".mp3", with: ""))")
							CoreDataManager.instance.saveContext()
						}
					}
				} catch {
					DispatchQueue.main.async {
						CoreDataManager.instance.setLogRecord(eventDescription: "Трек не скачан " + error.localizedDescription, isError: true, errorMessage: error.localizedDescription)
						CoreDataManager.instance.saveContext()
					}
					print(error.localizedDescription)
				}
			} else {
				DispatchQueue.main.async {
					CoreDataManager.instance.setLogRecord(eventDescription: "Трек не скачан, код ошибки: " + httpResponse.statusCode.description, isError: true, errorMessage: httpResponse.debugDescription + "\n########\n" + downloadTask.error.debugDescription)
					CoreDataManager.instance.saveContext()
				}
			}
		}
	}
}
