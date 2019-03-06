//
//  AppDelegate.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 11/22/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.
//

import UIKit
import Fabric
import Crashlytics
import CoreBluetooth
import UserNotifications


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
	var bgTask: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
	var window: UIWindow?
	//Задаём ориентацию экрана по умолчанию
	var orientationLock = UIInterfaceOrientationMask.portrait

	//с этой функции начинается загрузка приложения
	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
		// Override point for customization after application launch.
		let userDefaults = UserDefaults.standard
		//для получения отчетов об ошибках на фабрик
		Fabric.with([Crashlytics.self, Answers.self])
		userDefaults.set(false, forKey: "budState")
		//если устройству не назначен deviceId - генерируем новый
//		if userDefaults.object(forKey: "UUIDDevice") == nil {
//			let UUID = NSUUID().uuidString.lowercased() //"17096171-1C39-4290-AE50-907D7E62F36A" //
//			userDefaults.set(UUID, forKey: "UUIDDevice")
//			userDefaults.synchronize()
//		}
		//Регаем уведомления на будильник
		
		UNUserNotificationCenter.current().requestAuthorization(options: [.alert], completionHandler: {(granted, error) in
			if granted{
				userDefaults.set(true, forKey: "budPushGranted")
				DispatchQueue.main.async {
					application.registerForRemoteNotifications()
				}
			}
			else{
				userDefaults.set(false, forKey: "budPushGranted")
			}
		})

		//Проверяем в первый ли раз было запущено приложение(Перенесено в StartupViewController)

		//Регистрируем настройки по умолчанию (не меняя имеющиеся значения, если они уже есть)
		userDefaults.register(defaults: ["maxMemorySize": 10])
		userDefaults.register(defaults: ["isOnlyWiFi": false])
		userDefaults.register(defaults: ["PlayingSongInfo": ""])
		try? userDefaults.register(defaults: ["PlayingSongObject": PropertyListEncoder().encode(SongObject())])
		try? userDefaults.register(defaults: ["interruptedSongObject": PropertyListEncoder().encode(SongObject())])
		userDefaults.register(defaults: ["trackPlayingNow": false])
		userDefaults.register(defaults: ["playingInterrupted": false])
		userDefaults.register(defaults: ["wasMalfunction": false])
		userDefaults.register(defaults: ["listenRunning": false])
		userDefaults.register(defaults: ["isSubscribed": false])
		userDefaults.register(defaults: ["runCaching": false])
		userDefaults.register(defaults: ["runCaching" : false])
		userDefaults.register(defaults: ["bellOnce" : false])
		// создаем папку Tracks если ее нет
		var applicationSupportPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
		let tracksPath = applicationSupportPath.appendingPathComponent("Tracks")
		do {
			try FileManager.default.createDirectory(at: tracksPath, withIntermediateDirectories: true, attributes: nil)
		} catch let error as NSError {
			NSLog("Unable to create directory \(error.debugDescription)")
		}
		//создаем папку AlarmTracks
		applicationSupportPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
		let applicationTracksPath = applicationSupportPath.appendingPathComponent("AlarmTracks")
		do {
			try FileManager.default.createDirectory(at: applicationTracksPath, withIntermediateDirectories: true, attributes: nil)
		} catch let error as NSError {
			NSLog("Unable to create directory \(error.debugDescription)")
		}
		applicationSupportPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
		let currentTrackPath = applicationSupportPath.appendingPathComponent("CurrentPlayTrack")
		do {
			try FileManager.default.createDirectory(at: currentTrackPath, withIntermediateDirectories: true, attributes: nil)
		} catch let error as NSError {
			NSLog("Unable to create directory \(error.debugDescription)")
		}
		//проверяем была ли совершена миграция
		if userDefaults.object(forKey: "MigrationWasDoneV2") == nil {
			DispatchQueue.global().async {
				do {
					// получаем содержимое папки Documents
					if let tracksContents = try? FileManager.default.contentsOfDirectory(atPath: FileManager.docDir()) {

						self.removeFilesFromDirectory(tracksContents: tracksContents)

					}
					if let tracksContents = try? FileManager.default.contentsOfDirectory(atPath: FileManager.docDir().appending("/Tracks")) {
						self.removeFilesFromDirectory(tracksContents: tracksContents)
					}
					//удаляем треки из базы
					CoreDataManager.instance.deleteAllTracks()
					// устанавливаем флаг о прохождении миграции
					userDefaults.set(true, forKey: "MigrationWasDoneV2")
					userDefaults.synchronize()
				}
			}
		}
		userDefaults.set(false, forKey: "runCaching")
		userDefaults.set(true, forKey: "writeLog") // Флаг записи лога
		userDefaults.synchronize()
		return true
	}


	var bgSessionCompleteHandler:(() -> Void)?

	func application(_ application: UIApplication, handleEventsForBackgroundURLSession identifier: String, completionHandler: @escaping () -> Void) {
		bgSessionCompleteHandler = completionHandler
	}

	func removeFilesFromDirectory (tracksContents: [String]) {
		//если в папке больше 4 файлов (3 файла Sqlite и папка Tracks) то пытаемся удалить треки
		if tracksContents.count > 1 {
			for track in tracksContents {
				// проверка для удаления только треков
				if !track.contains("sqlite") {
					let atPath = FileManager.docDir().appending("/").appending(track)
					do {
						print(atPath)
						try FileManager.default.removeItem(atPath: atPath)

					} catch {
						print("error with move file reason - \(error)")
					}
				}
			}
		}
	}

	//задаёт ориентацию экрана
	func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
		return self.orientationLock
	}
	
	func applicationWillResignActive(_ application: UIApplication) {
		// Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
		// Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
	}

	func applicationDidEnterBackground(_ application: UIApplication) {
		// Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
		// If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
//		UserDefaults.standard.set(false, forKey: "timerState")
//		UserDefaults.standard.set(0, forKey: "timerDurationSeconds")
		UNUserNotificationCenter.current().requestAuthorization(options: [.alert], completionHandler: {(granted, error) in
			if granted{
				UserDefaults.standard.set(true, forKey: "budPushGranted")
				DispatchQueue.main.async {
					application.registerForRemoteNotifications()
				}
			}
			else{
				UserDefaults.standard.set(false, forKey: "budPushGranted")
			}
		})

		
		if UserDefaults.standard.bool(forKey: "budState"){
			self.bgTask = application.beginBackgroundTask(withName: "AlertClockTask", expirationHandler: nil)
		}
	}

	func applicationWillEnterForeground(_ application: UIApplication) {
		// Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
		if self.bgTask != UIBackgroundTaskInvalid{
			application.endBackgroundTask(bgTask)
		}
		UNUserNotificationCenter.current().requestAuthorization(options: [.alert], completionHandler: {(granted, error) in
			if granted{
				UserDefaults.standard.set(true, forKey: "budPushGranted")
				DispatchQueue.main.async {
					application.registerForRemoteNotifications()
				}
			}
			else{
				UserDefaults.standard.set(false, forKey: "budPushGranted")
			}
		})
		UserDefaults.standard.synchronize()
	}

	func applicationDidBecomeActive(_ application: UIApplication) {
		// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
//		if let rootController = UIApplication.shared.keyWindow?.rootViewController {
//			let navigationController = rootController as! UINavigationController
//			//получаем отображаемый в текущий момент контроллер, если это контроллер видео-слайдера - возобновляем воспроизведение видео.
//			if let startViewContr = navigationController.topViewController  as? StartVideoViewController {
//				DispatchQueue.main.asyncAfter(deadline: .now() + 1.0, execute: {
//					startViewContr.playVideoBackgroud()
//				})
//
//			}
//		}
	}
	

	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
		UserDefaults.standard.set(false, forKey: "timerState")
		UserDefaults.standard.set(0, forKey: "timerDurationSeconds")
		//UserDefaults.standard.set(false, forKey: "budState")
		UserDefaults.standard.set(false, forKey: "wasMalfunction")
		UserDefaults.standard.set(false, forKey: "listenRunning")
		UserDefaults.standard.set(false, forKey: "runCaching")
		UserDefaults.standard.synchronize()
		//UserDefaults.standard.set(false, forKey: "playingInterruptedByTimer")
		if UserDefaults.standard.bool(forKey: "writeLog"){
			CoreDataManager.instance.setLogRecord(eventDescription: "Приложение выгружено", isError: false, errorMessage: "")
			CoreDataManager.instance.saveContext()
		}

	}

}
