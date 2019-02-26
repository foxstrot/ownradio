//
//  StartupViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 18.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit

@available(iOS 10.0, *)
class StartupViewController: UIPageViewController {

	let userDefaults = UserDefaults.standard

    override func viewDidLoad() {
        super.viewDidLoad()

		let storyboard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)

		if userDefaults.object(forKey: "isAppAlreadyLaunchedOnce") == nil {
			ApiService.shared.registerDevice()
			userDefaults.set(true, forKey: "isAppAlreadyLaunchedOnce")
			print("Приложение запущено впервые")
			sleep(1)
			let viewController = storyboard.instantiateViewController(withIdentifier: "startupPageView")
			self.navigationController?.pushViewController(viewController, animated: false)
		} else {
			sleep(1)
			let viewController = storyboard.instantiateViewController(withIdentifier: "RadioViewController")
			self.navigationController?.pushViewController(viewController, animated: false)
		}
        // Do any additional setup after loading the view.
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
