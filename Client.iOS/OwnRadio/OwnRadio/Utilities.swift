//
//  Utilities.swift
//  OwnRadio
//
//  Created by Roman Litoshko on 12/2/16.
//  Copyright © 2016 Roll'n'Code. All rights reserved.
//
//	Support methods

import Foundation
import SystemConfiguration

protocol Utilities {
}

extension FileManager {
	class func applicationSupportDir() -> String {
		var paths = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true) as [String]
		return paths[0]
	}

	class func docDir() -> String {
		var paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true) as [String]
		return paths[0]
	}

	class func cachesDir() -> String {
		var paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true) as [String]
		return paths[0]
	}
}

extension NSObject: Utilities {
	enum ReachabilityStatus {
		case notReachable
		case reachableViaWWAN
		case reachableViaWiFi
	}

	var currentReachabilityStatus: ReachabilityStatus {

		let isOnlyWiFi = UserDefaults.standard.object(forKey: "isOnlyWiFi") as? Bool
		var zeroAddress = sockaddr_in()
		zeroAddress.sin_len = UInt8(MemoryLayout<sockaddr_in>.size)
		zeroAddress.sin_family = sa_family_t(AF_INET)

		guard let defaultRouteReachability = withUnsafePointer(to: &zeroAddress, {
			$0.withMemoryRebound(to: sockaddr.self, capacity: 1) {
				SCNetworkReachabilityCreateWithAddress(nil, $0)
			}
		}) else {
			return .notReachable
		}

		var flags: SCNetworkReachabilityFlags = []
		if !SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags) {
			return .notReachable
		}

		if flags.contains(.reachable) == false {
			// The target host is not reachable.
			return .notReachable
		} else if flags.contains(.isWWAN) == true {
			// WWAN connections are OK if the calling application is using the CFNetwork APIs.
			// Если в настройках задано "только wifi" - возвращаем "нет подключения"
			if isOnlyWiFi == false {
				return .reachableViaWWAN
			} else {
				return .notReachable
			}
		} else if flags.contains(.connectionRequired) == false {
			// If the target host is reachable and no connection is required then we'll assume that you're on Wi-Fi...
			return .reachableViaWiFi
		} else if (flags.contains(.connectionOnDemand) == true || flags.contains(.connectionOnTraffic) == true) && flags.contains(.interventionRequired) == false {
			// The connection is on-demand (or on-traffic) if the calling application is using the CFSocketStream or higher APIs and no [user] intervention is needed
			return .reachableViaWiFi
		} else {
			return .notReachable
		}
	}

}
