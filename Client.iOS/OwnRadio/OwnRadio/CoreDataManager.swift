//
//  CoreDataManager.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 12/1/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.
//
//	Data Manager, creation and managing with data

import CoreData
import Foundation

class CoreDataManager: NSObject {

	// Singleton
	static let instance = CoreDataManager()

	private override init() {}

	// Entity for Name
	func entityForName(entityName: String) -> NSEntityDescription {
		return NSEntityDescription.entity(forEntityName: entityName, in: self.managedObjectContext)!
	}

	func getAllEntitiesFor(entityName: String) -> [Any] {
		let request: NSFetchRequest<NSFetchRequestResult> = NSFetchRequest(entityName: entityName)
		var fetchRequest = [Any]()
		do {
			fetchRequest = try self.managedObjectContext.fetch(request)
		} catch {
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении всех сущностей по имени", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
			fatalError("Failed to fetch : \(error)")
		}
		return fetchRequest
	}

	// MARK: - Core Data stack
	lazy var applicationDocumentsDirectory: NSURL = {
		let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
		return urls[urls.count-1] as NSURL
	}()

	lazy var managedObjectModel: NSManagedObjectModel = {
		let modelURL = Bundle.main.url(forResource: "DataModel", withExtension: "momd")!
		return NSManagedObjectModel(contentsOf: modelURL)!
	}()

	lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator = {
		let coordinator = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
		let url = self.applicationDocumentsDirectory.appendingPathComponent("SingleViewCoreData.sqlite")
		var failureReason = "There was an error creating or loading the application's saved data."
		DispatchQueue.global().async {
		do {
			let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
			try coordinator.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: url, options: options)
		} catch {
			var dict = [String: AnyObject]()
			dict[NSLocalizedDescriptionKey] = "Failed to initialize the application's saved data" as AnyObject?
			dict[NSLocalizedFailureReasonErrorKey] = failureReason as AnyObject?
			dict[NSUnderlyingErrorKey] = error as NSError
			let wrappedError = NSError(domain: "YOUR_ERROR_DOMAIN", code: 9999, userInfo: dict)
			NSLog("Unresolved error \(wrappedError), \(wrappedError.userInfo)")
			CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при создании persistentStoreCoordinator", isError: true, errorMessage: error.localizedDescription)
			CoreDataManager.instance.saveContext()
			abort()
		}
		}
		return coordinator
	}()

	lazy var managedObjectContext: NSManagedObjectContext = {
		let coordinator = self.persistentStoreCoordinator
		var managedObjectContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
		managedObjectContext.persistentStoreCoordinator = coordinator
		managedObjectContext.retainsRegisteredObjects = true
		return managedObjectContext
	}()

	private lazy var privateManagedObjectContext: NSManagedObjectContext = {
		let moc = NSManagedObjectContext(concurrencyType: .privateQueueConcurrencyType)
		moc.parent = self.managedObjectContext
		return moc
	}()

	// End of data stack
	// MARK: Support Functions
	// возвращает количество записей в таблице
	func chekCountOfEntitiesFor(entityName: String) -> Int {
		let request: NSFetchRequest<NSFetchRequestResult> = NSFetchRequest(entityName: entityName)
		var count = 0
		do {
			count = try self.managedObjectContext.count(for: request)
		} catch {
			print("Error with get count of entities")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении записей в локальной БД", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}

		return count
	}

	// удаляет историю прослушивания трека с заданным trackId
	func deleteHistoryFor(trackID: String) {
		let fetchRequest: NSFetchRequest<HistoryEntity> = HistoryEntity.fetchRequest()
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				self.managedObjectContext.delete(object)
			}
		}
	}
	// удаляет из базы трек с заданным trackId
	func deleteTrackFor(trackID: String) {
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		fetchRequest.predicate = NSPredicate(format: "recId = %@", trackID)
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				self.managedObjectContext.delete(object)
			}
		}
	}

	// удаление всех сущностей в таблице Track, для миграции из папки Documents
	func deleteAllTracks() {
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				self.managedObjectContext.delete(object)
			}
		}
	}

	// задает текущую дату для трека с заданным trackId
	func setDateForTrackBy(trackId: String) {
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		// устанавливаем предикат для запроса
		fetchRequest.predicate = NSPredicate(format: "recId = %@", trackId)
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				object.playingDate = NSDate()
			}
		}
	}

	// увеличивает число проигрываний
	func setCountOfPlayForTrackBy(trackId: String) {
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		// устанавливаем предикат для запроса
		fetchRequest.predicate = NSPredicate(format: "recId = %@", trackId)
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				object.countPlay += 1
			}
		}
	}

	func sentHistory () {
		// если нет неотправленной истории прослушивания - выходим из функции
		guard CoreDataManager.instance.chekCountOfEntitiesFor(entityName: "HistoryEntity") > 0 else {
			return
		}
		//create a fetch request, telling it about the entity
		let fetchRequest: NSFetchRequest<HistoryEntity> = HistoryEntity.fetchRequest()

		do {
			// выполняем запрос и отправляем историю о каждом треке
			let searchResults = try self.managedObjectContext.fetch(fetchRequest)
			var buff: String = ""
			for track in searchResults {
				if track.trackId == buff {
					print("TRACKS IDs are equal!!! ")
				}
                ApiService.shared.saveHistory(historyId: track.recId!, trackId: track.trackId!, isListen: Int(track.isListen))

				buff = track.trackId!
			}
		} catch {
			print("Error with request: \(error)")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при сохранении истории", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
	}

	//выбираем из трек для проигрывания
	func getTrackToPlaing() -> SongObject {
		//задаем сортировку по возрастанию даты проигрывания
		//upd 23.07.2018: меняем алгоритм - сначала проигрываем последние скачанные из непроигранных, если таких нет - в порядке очередности проигрывания
		let sectionSortDescriptor = NSSortDescriptor(key: "playingDate", ascending: false)
//		let countSortDescriptor = NSSortDescriptor(key: "countPlay", ascending: true)

		let song = getTrackFromBd(sortDescriptors: [sectionSortDescriptor], predicate: NSPredicate(format: "countPlay = %d", 0))
		guard song.trackID != nil else {
			let sectionSortDescriptor = NSSortDescriptor(key: "playingDate", ascending: true)
			return getTrackFromBd(sortDescriptors: [sectionSortDescriptor], predicate: NSPredicate(format: "countPlay > %d", 0))
		}
		return song
	}

	// Возвращает трек из БД с учетом заданных сортировки и условий
	func getTrackFromBd(sortDescriptors: [NSSortDescriptor], predicate: NSPredicate  ) -> SongObject {
		// создание запроса
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		fetchRequest.sortDescriptors = sortDescriptors
		// задаем предикат
		fetchRequest.predicate = predicate
		fetchRequest.fetchLimit = 1
		let  song = SongObject()
		do {
			//выполняем запрос к БД
			let searchResults = try self.managedObjectContext.fetch(fetchRequest)
			//если в таблице нет записей - возращаем пустой объект song
			guard searchResults.count != 0 else {
				return song
			}
			//выбираем первую запись
			let track = searchResults.first

			song.name = track?.trackName
			song.artistName = track?.artistName
			song.trackLength = track?.trackLength
			song.trackID = track?.recId
			song.path = track?.path

		} catch {
			print("Error with request: \(error)")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении трека из БД", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
		return song
	}

	// запрос groupBy для получения кол-ва проигрываний
	func getGroupedTracks () -> NSArray {
		// создаем запрос и устанавливаем конфигурации для запроса
		let fetchRequest: NSFetchRequest<NSFetchRequestResult> = NSFetchRequest()
		let entityDescription = NSEntityDescription.entity(forEntityName: "TrackEntity", in: self.managedObjectContext)
		fetchRequest.resultType = .dictionaryResultType
		fetchRequest.entity = entityDescription
		// создаем выражение для propertiesToFetch
		let keyPathExpression = NSExpression.init(forKeyPath: "countPlay")
		let countExpression = NSExpression(forFunction: "count:", arguments: [keyPathExpression])

		let expressionDescription = NSExpressionDescription()
		expressionDescription.name = "count"

		expressionDescription.expression = countExpression
		expressionDescription.expressionResultType = .integer32AttributeType

		// устанавливаем значения propertiesToFetch и propertiesToGroupBy для запроса groupby
		fetchRequest.propertiesToFetch = ["countPlay", expressionDescription]
		fetchRequest.propertiesToGroupBy = ["countPlay"]

		var resultsArray = NSArray()
		do {
			//выполняем запрос
			let res = try self.managedObjectContext.fetch(fetchRequest)
			resultsArray = res as NSArray
		} catch {
			print(error.localizedDescription)

			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении количества проигрываний", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
		return resultsArray
	}

	// возвращает соличество сущностей в таблице TrackEntity
	func getCountOfTracks() -> Int {
		// создаем запрос
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		var count = 0
		do {
			//выполняем запрос и устанавливаем count
			let searchResults = try self.managedObjectContext.fetch(fetchRequest)
			count = searchResults.count
		} catch {
			print("Error with request: \(error)")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении количества сущностей таблицы TrackEntity", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
		return count
	}

	// получает трек с найбольшим кол-вом проигрываний
	func getOldTrack (onlyListen: Bool) -> [SongObject?] {
		// устанавливаем сортировку по кол-ву поигрываний и по дате
		let countSortDescriptor = NSSortDescriptor(key: "countPlay", ascending: false)
		let dateSortDescriptor = NSSortDescriptor(key: "playingDate", ascending: true)
		let sortDescriptors = [countSortDescriptor, dateSortDescriptor]

		// создаем запрос к базе с сортировкой
		let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
		fetchRequest.sortDescriptors = sortDescriptors
//		задаем предикат
		if(onlyListen) {
			fetchRequest.predicate = NSPredicate(format: "countPlay > %d", 0)
		} else {
			fetchRequest.predicate = NSPredicate(format: "countPlay >= %d", 0)
		}
		fetchRequest.fetchLimit = 2
		var songs = [SongObject]()
		do {
			// выполняем запрос и проверяем кол-во результатов
			let searchResults = try self.managedObjectContext.fetch(fetchRequest)
			guard searchResults.count != 0 else {
				return [nil]
			}
			//берем первый обьект из результата
			for result in searchResults{
				let song = SongObject()
				
				song.name = result.trackName
				song.artistName = result.artistName
				song.trackLength = result.trackLength
				song.trackID = result.recId
				song.path = result.path
				songs.append(song)
			}
			

		} catch {
			print("Error with request: \(error)")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении трека с наибольшим количество проигрываний", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
		return songs
	}

    // возвращает время начала проигрывания трека с заданным trackId
    func getDateForTrackBy(trackId: String) -> NSDate? {
        let fetchRequest: NSFetchRequest<TrackEntity> = TrackEntity.fetchRequest()
        // устанавливаем предикат для запроса
        fetchRequest.predicate = NSPredicate(format: "recId = %@", trackId)
        if let result = try? self.managedObjectContext.fetch(fetchRequest) {
            for object in result {
                return object.playingDate
            }
        }
        return nil
    }

	//выбираем все прослушанные треки
	func getListenTracks() -> [SongObject] {
		//задаем сортировку по убыванию количества проигрываний
		let sectionSortDescriptor = NSSortDescriptor(key: "countPlay", ascending: false)
		let sortDescriptors = [sectionSortDescriptor]
		// создание запроса
		let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "TrackEntity") //TrackEntity.fetchRequest()
		fetchRequest.sortDescriptors = sortDescriptors
		fetchRequest.predicate = NSPredicate(format: "countPlay > %@", "0")
		var song = SongObject()
		var listenTracks = [SongObject]()
		do {
			//выполняем запрос к БД
			let searchResults = try self.managedObjectContext.fetch(fetchRequest)
			//если в таблице нет записей - возращаем пустой объект listenTracks
			guard searchResults.count != 0 else {
				return listenTracks
			}
			//сохраняем результат выборки в массив
			let trackEntity = searchResults as! [TrackEntity]
			for _track in trackEntity {
				song.name = _track.trackName
				song.artistName = _track.artistName
				song.trackLength = _track.trackLength
				song.trackID = _track.recId
				song.path = _track.path
				listenTracks.append(song)
				song = SongObject()
			}
		} catch {
			print("Error with request: \(error)")
			if UserDefaults.standard.bool(forKey: "writeLog"){
				CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при получении всех прослушанных треков", isError: true, errorMessage: error.localizedDescription)
				CoreDataManager.instance.saveContext()
			}
		}
		return listenTracks
	}

	func setLogRecord(eventDescription: String, isError: Bool, errorMessage: String) {
		do {

			let entityDescription = NSEntityDescription.entity(forEntityName: "LogEntity", in: self.managedObjectContext)
			let managedObject = NSManagedObject(entity: entityDescription!, insertInto: self.managedObjectContext)
			let date = Date()
			managedObject.setValue(date, forKey: "eventDate")
			managedObject.setValue(eventDescription, forKey: "eventDescription")
			managedObject.setValue(Thread.current.debugDescription, forKey: "eventThread")
			if currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable {
				managedObject.setValue(true, forKey: "hasInternet")
			} else {
				managedObject.setValue(false, forKey: "hasInternet")
			}
			managedObject.setValue(isError, forKey: "isError")
			managedObject.setValue(errorMessage, forKey: "errorMessage")
		} catch {
			print("Ошибка сохранения лога \(error.localizedDescription)")
		}
	}

	func getLogRecords() -> [LogObject] {
		let sectionSortDescriptor = NSSortDescriptor(key: "eventDate", ascending: false)
		let sortDescriptors = [sectionSortDescriptor]
		let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "LogEntity") //TrackEntity.fetchRequest()
		fetchRequest.sortDescriptors = sortDescriptors
		var logsArray = [LogObject]()
		do {
			let logResults = try self.managedObjectContext.fetch(fetchRequest)
			guard logResults.count != 0 else {
				return logsArray
			}
			let logEntity = logResults as! [LogEntity]
			for logItem in logEntity {
				let log = LogObject()
				log.eventDate = logItem.eventDate! as Date
				log.eventDescription = logItem.eventDescription!
				log.eventThread = logItem.eventThread ?? ""
				log.hasInternet = logItem.hasInternet
				log.isError = logItem.isError
				log.errorMessage = logItem.errorMessage ?? ""
				logsArray.append(log)
			}
		} catch {
			print("Error with get log block: \(error)")
		}
		return logsArray
	}

	func deleteLogRecords() {
		let fetchRequest: NSFetchRequest<LogEntity> = LogEntity.fetchRequest()
		if let result = try? self.managedObjectContext.fetch(fetchRequest) {
			for object in result {
				self.managedObjectContext.delete(object)
			}
		}
	}

	// MARK: - Core Data Saving support
	// функция сохранения контекста
	func saveContext () {
		if managedObjectContext.hasChanges {
			DispatchQueue.main.async {
				do {
					try self.managedObjectContext.save()
				} catch {
					let nserror = error as NSError
					NSLog("Unresolved error \(nserror), \(nserror.userInfo)")
//					CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка сохранения контекста", isError: true, errorMessage: error.localizedDescription)
//					CoreDataManager.instance.saveContext()
					print("Ошибка сохранения контекста \(error.localizedDescription)")
					abort()
				}
			}
		}
	}

}
