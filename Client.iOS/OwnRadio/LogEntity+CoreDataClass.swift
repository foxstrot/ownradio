//
//  LogEntity+CoreDataClass.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 11.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import Foundation
import CoreData


public class LogEntity: NSManagedObject {
	convenience init() {
		self.init(entity: CoreDataManager.instance.entityForName(entityName: "LogEntity"), insertInto: CoreDataManager.instance.managedObjectContext)
	}
}
