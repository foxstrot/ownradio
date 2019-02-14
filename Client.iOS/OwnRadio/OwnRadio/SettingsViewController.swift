//
//  SettingsViewController.swift
//  OwnRadio
//
//  Created by Alexandra Polunina on 26.07.17.
//  Copyright © 2017 Netvox Lab. All rights reserved.
//

import UIKit
import Foundation
import StoreKit


class SettingsViewController: UITableViewController, SKProductsRequestDelegate, SKPaymentTransactionObserver{
	
	
	@IBOutlet weak var maxMemoryLbl: UILabel!
	@IBOutlet weak var stepper: UIStepper!
	@IBOutlet weak var onlyWiFiSwitch: UISwitch!
	@IBOutlet weak var freeSpaceLbl: UILabel!
	@IBOutlet weak var cacheFolderSize: UILabel!
	@IBOutlet weak var listenTracksSize: UILabel!
	@IBOutlet weak var delAllTracksLbl: UILabel!
	@IBOutlet weak var versionLbl: UILabel!
	@IBOutlet weak var deviceIdLbl: UILabel!
    
	@IBOutlet weak var fromFreeSpace: UILabel!

	@IBOutlet weak var delAllTracksCell: UITableViewCell!
	@IBOutlet weak var countPlayingTracksTable: UILabel!
    @IBOutlet weak var countTracksLbl: UILabel!
	@IBOutlet weak var buySubscribeBtn: UIButton!
	
	//получаем таблицу с количеством треков сгруппированных по количестсву их прослушиваний
	var playedTracks: NSArray = CoreDataManager.instance.getGroupedTracks()
	
    let tracksUrlString = FileManager.applicationSupportDir().appending("/Tracks/")
	
	var remoteAudioControls: RemoteAudioControls?
	
	var productsRequest = SKProductsRequest()
	let subId = "test.subscribtion"
	var productsArray = [SKProduct]()
	var productID = ""
	
	
	override func viewDidLoad() {
		super.viewDidLoad()
		
		fetchAvailableProducts()
		
		let userDefaults = UserDefaults.standard

		onlyWiFiSwitch.isOn = (userDefaults.object(forKey: "isOnlyWiFi") as? Bool)!
		
		stepper.wraps = true
		stepper.autorepeat = true
		stepper.value = (userDefaults.object(forKey: "maxMemorySize") as? Double)!
		
		stepper.minimumValue = 10.0
		stepper.maximumValue = 50.0
		stepper.stepValue = 10.0
        
        let maxMemorySizePercent = userDefaults.integer(forKey: "maxMemorySize")
        let maxMemorySizeGB = Int64(Float(Float(maxMemorySizePercent) / 100) * Float(DiskStatus.freeDiskSpaceInBytes))
        
		maxMemoryLbl.text = maxMemorySizePercent.description  + "%"
        fromFreeSpace.text = "*от свободной памяти " + DiskStatus.GBFormatter(Int64(DiskStatus.freeDiskSpaceInBytes)) + " Gb"
		freeSpaceLbl.text = "Свободно " + DiskStatus.GBFormatter(Int64(DiskStatus.freeDiskSpaceInBytes)) + " Gb"
		
        
		cacheFolderSize.text = "Занято " + DiskStatus.GBFormatter(Int64(DiskStatus.folderSize(folderPath: tracksUrlString))) + "Gb (из " + DiskStatus.GBFormatter(maxMemorySizeGB) + " Gb)"
		countTracksLbl.text = CoreDataManager.instance.chekCountOfEntitiesFor(entityName: "TrackEntity").description + " треков"
        
		let listenTracks = CoreDataManager.instance.getListenTracks()
		listenTracksSize.text = "Из них уже прослушанных " + DiskStatus.GBFormatter(Int64(DiskStatus.listenTracksSize(folderPath:tracksUrlString, tracks: listenTracks))) + " Gb (" + listenTracks.count.description + " треков)"
		
		let tapDelAllTracks = UITapGestureRecognizer(target: self, action: #selector(self.tapDelAllTracks(sender:)))
		delAllTracksCell.isUserInteractionEnabled =  true
		delAllTracksCell.addGestureRecognizer(tapDelAllTracks)
		
		if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
			if (Bundle.main.infoDictionary?["CFBundleVersion"] as? String) != nil {
				versionLbl.text =  "Version: v" + version
			}
		}
		deviceIdLbl.text = "DeviceID: " + (UIDevice.current.identifierForVendor?.uuidString.lowercased())!
		
		
		var str = "" as NSString
		for track in playedTracks {
		let dict = track as! [String: Any]
		let countOfPlay = dict["countPlay"] as? Int
		let countOfTracks = dict["count"] as? Int
		if countOfPlay != nil && countOfTracks != nil {
			if str == "" {
				str = NSString(format: "Count play: %d - Count tracks: %d", countOfPlay! , countOfTracks!)
			} else {
				str = NSString(format: "%@ \nCount play: %d - Count tracks: %d", str, countOfPlay! , countOfTracks!)
			}
			}
		}
		
		countPlayingTracksTable.numberOfLines = playedTracks.count
		countPlayingTracksTable.text = str as String
		
		CoreDataManager.instance.setLogRecord(eventDescription: "Переход в настройки", isError: false, errorMessage: "")
		CoreDataManager.instance.saveContext()
		
	}
	
	
	
	
	@IBAction func onlyWiFiSwitchValueChanged(_ sender: UISwitch) {
		UserDefaults.standard.set(onlyWiFiSwitch.isOn, forKey: "isOnlyWiFi")
		UserDefaults.standard.synchronize()
	}
	
	override func remoteControlReceived(with event: UIEvent?) {
		guard let remoteControls = remoteAudioControls else {
			print("Remote controls not set")
			return
		}
		remoteControls.remoteControlReceived(with: event)
	}
	
	//Сохраняем настроки "занимать не более" и выводим актуальное значение при его изменении
	@IBAction func stepperValueChanged(_ sender: UIStepper) {
		maxMemoryLbl.text = Int(stepper.value).description + "%"
		UserDefaults.standard.set(stepper.value, forKey: "maxMemorySize")
		UserDefaults.standard.synchronize()
        
        let maxMemorySizePercent = UserDefaults.standard.integer(forKey: "maxMemorySize")
        let maxMemorySizeGB = Int64(Float(Float(maxMemorySizePercent) / 100) * Float(DiskStatus.freeDiskSpaceInBytes))
        cacheFolderSize.text = "Занято " + DiskStatus.GBFormatter(Int64(DiskStatus.folderSize(folderPath: self.tracksUrlString))) + "Gb (из " + DiskStatus.GBFormatter(maxMemorySizeGB) + " Gb)"
	}
	
	
	@IBAction func btnfillCacheClick(_ sender: UIButton) {
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			return
		}
		
		DispatchQueue.global(qos: .utility).async {
			Downloader.sharedInstance.fillCache()
		}
	}
	
	@IBAction func btnBuySubClick(_ sender: Any) {
		purchaseSubscribe()
	}
	
	func fetchAvailableProducts(){
		let productsIdentifiers = NSSet(object: subId)
		productsRequest = SKProductsRequest(productIdentifiers: productsIdentifiers as! Set<String>)
		productsRequest.delegate = self
		productsRequest.start()
	}
	
	func tapDelAllTracks(sender: UITapGestureRecognizer) {
		let dellAllTracksAlert = UIAlertController(title: "Удаление всех треков", message: "Вы уверены что хотите удалить все треки из кэша? Приложение не сможет проигрывать треки в офлайне пока не будет наполнен кэш.", preferredStyle: UIAlertControllerStyle.alert)
		
		dellAllTracksAlert.addAction(UIAlertAction(title: "ОК", style: .default, handler: { (action: UIAlertAction!) in
			let tracksUrlString = FileManager.applicationSupportDir().appending("/Tracks/")
			// получаем содержимое папки Tracks
			if let tracksContents = try? FileManager.default.contentsOfDirectory(atPath: tracksUrlString ){
				
				for track in tracksContents {
					// проверка для удаления только треков
					if track.contains("mp3") {
						let path = tracksUrlString.appending(track)
						do{
							print(path)
							try FileManager.default.removeItem(atPath: path)
							
						} catch  {
							print("Ошибка при удалении файла  - \(error)")
							CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при удалении файла", isError: true, errorMessage: error.localizedDescription)
							CoreDataManager.instance.saveContext()
						}
					}
				}
				
				//удаляем треки из базы
				CoreDataManager.instance.deleteAllTracks()
				CoreDataManager.instance.saveContext()
				self.viewDidLoad()
			}
			
			
		}))
		
		dellAllTracksAlert.addAction(UIAlertAction(title: "ОТМЕНА", style: .cancel, handler: { (action: UIAlertAction!) in
			
		}))
		
		present(dellAllTracksAlert, animated: true, completion: nil)
		
		
	}
	
	func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
		for transaction: AnyObject in transactions{
			if let trans = transaction as? SKPaymentTransaction{
				switch trans.transactionState{
				case .purchased:
					SKPaymentQueue.default().finishTransaction(transaction as! SKPaymentTransaction)
					
					if productID == subId{
						//Сохранить инфу о том, что подписка куплена
					}
					break
				case .failed:
					SKPaymentQueue.default().finishTransaction(transaction as! SKPaymentTransaction)
					print("Transaction failed")
					break
					
				case .restored:
					SKPaymentQueue.default().finishTransaction(transaction as! SKPaymentTransaction)
					break
				case .purchasing:
					SKPaymentQueue.default().finishTransaction(transaction as! SKPaymentTransaction)
				case .deferred:
					SKPaymentQueue.default().finishTransaction(transaction as! SKPaymentTransaction)
				}
			}
		}
	}
	
	func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
		if response.products.count > 0{
			productsArray = response.products
		}
	}
	
	func canPurchase() -> Bool {return SKPaymentQueue.canMakePayments() }
	
	func purchaseSubscribe(){
		if productsArray.count > 0 {
			if self.canPurchase(){
				let payment = SKPayment(product: productsArray[0])
				SKPaymentQueue.default().add(self)
				SKPaymentQueue.default().add(payment)
				
				productID = productsArray[0].productIdentifier
				
			}
			else{
				print("Purchases disabled")
			}
		}
		else{
			print("Products not fetched")
		}
	}
	
	
	@IBAction func delListenTracksBtn(_ sender: UIButton) {
		let dellListenTracksAlert = UIAlertController(title: "Удаление прослушанных треков", message: "Вы хотите удалить прослушанные треки из кэша?", preferredStyle: UIAlertControllerStyle.alert)
		
		dellListenTracksAlert.addAction(UIAlertAction(title: "ОК", style: .default, handler: { (action: UIAlertAction!) in
			
			let tracksUrlString =  FileManager.applicationSupportDir().appending("/Tracks/")
			
			let listenTracks = CoreDataManager.instance.getListenTracks()
			print("\(listenTracks.count)")
			for _track in listenTracks {
				let path = tracksUrlString.appending((_track.path!))
				
				if FileManager.default.fileExists(atPath: path) {
					do{
						// удаляем файл
						try FileManager.default.removeItem(atPath: path)
					}
					catch {
						print("Ошибка при удалении файла - \(error)")
						CoreDataManager.instance.setLogRecord(eventDescription: "Ошибка при удалении прослушаннх треков", isError: true, errorMessage: error.localizedDescription)
						CoreDataManager.instance.saveContext()
					}
				}
				// удаляем трек с базы
				CoreDataManager.instance.deleteTrackFor(trackID: _track.trackID)
				CoreDataManager.instance.saveContext()
			}
			
			self.viewDidLoad()
		}))
		
		dellListenTracksAlert.addAction(UIAlertAction(title: "ОТМЕНА", style: .cancel, handler: { (action: UIAlertAction!) in
		}))
		
		present(dellListenTracksAlert, animated: true, completion: nil)
	}
	
	@IBAction func writeToDevelopers(_ sender: UIButton) {
		UIApplication.shared.openURL(NSURL(string: "http://www.vk.me/write-87060547")! as URL)
	}
	
	@IBAction func oneTapAction(_ sender: Any) {
		//Обновление состояния таймера по нажатию на экран
		if UserDefaults.standard.bool(forKey: "timerState"){
			UserDefaults.standard.set(Int(Date().timeIntervalSince1970), forKey:  "updateTimerDate")
		}
	}
	
	@IBAction func rateAppBtn(_ sender: UIButton) {
		UIApplication.shared.openURL(NSURL(string: "itms://itunes.apple.com/ru/app/ownradio/id1179868370")! as URL)
	}
	
//	override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
////		if indexPath.section == 4 && indexPath.row == 0 {
////
////			return 100
////		} else {
////			let row = tableView.cellForRow(at: indexPath)// dequeueReusableCell(withIdentifier: "Cell")//(at: indexPath) //.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
////			let h = row?.bounds.size.height
////			print (h ?? 1)
//			return UITableViewAutomaticDimension
////		}
//	}
	// MARK: UITableViewDataSource
	//	 override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
	////		if section == 4 {
	//			return self.playedTracks.count-1
	////		}
	////		if (section == 0) {
	////			return 1;
	////		} else {
	////			var frcSection = section - 1;
	////			id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:frcSection];
	////			return sectionInfo numberOfObjects];
	////		}
	//	}
	
//		 override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//			let cell = tableView.cellForRow(at: indexPath) //countListeningTableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
//			
////			let dict = playedTracks[indexPath.row] as! [String: Any]
////			let countOfPlay = dict["countPlay"] as? Int
////			let countOfTracks = dict["count"] as? Int
////			if countOfPlay != nil && countOfTracks != nil {
////				let str = NSString(format: "Count play: %d - Count tracks: %d", countOfPlay! , countOfTracks! )
////				cell.textLabel?.text = str as String
////			}
//			return cell
//		}
}
