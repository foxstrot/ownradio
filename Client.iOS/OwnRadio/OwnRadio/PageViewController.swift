//
//  PageViewController.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 18.02.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit
import Alamofire

class PageViewController: UIPageViewController, UIPageViewControllerDelegate, UIPageViewControllerDataSource {

	var timerAutoSkip: DispatchSourceTimer!
	var timerRunPlayer: DispatchSourceTimer!
	var reachability = NetworkReachabilityManager(host: "http://api.ownradio.ru/v5")

	var pageControl = UIPageControl()
	lazy var orderedViewControllers: [UIViewController] = {
		return [self.newVc(viewController: "firstSlide"),
				self.newVc(viewController: "secondSlide"),
				self.newVc(viewController: "thirdSlide")]
	}()

    override func viewDidLoad() {
        super.viewDidLoad()

		self.dataSource = self
		self.delegate = self

		if let firstViewController = orderedViewControllers.first {
			setViewControllers([firstViewController], direction: .forward, animated: true, completion: nil)
		}

		configurePageControl()
		reachability?.listener = { [unowned self] status in
			if status != NetworkReachabilityManager.NetworkReachabilityStatus.notReachable {
				self.downloadTracks()
			}
		}
		reachability?.startListening()

        // Do any additional setup after loading the view.
    }

	override func viewDidAppear(_ animated: Bool) {
		if timerAutoSkip != nil {
			timerAutoSkip.cancel()
		}
		runAutoskip()
	}
	//Инициализация таймера перелистываний
	func runAutoskip() {
		let queue = DispatchQueue(label: "AutoSkipTimer", attributes: .concurrent)
		timerAutoSkip = DispatchSource.makeTimerSource(queue: queue)
		timerAutoSkip.scheduleRepeating(deadline: .now() + 10, interval: .seconds(10))
		timerAutoSkip.setEventHandler(handler: {
			DispatchQueue.main.sync {
				self.goToNextPage(animated: true)
				if self.pageControl.currentPage == self.orderedViewControllers.count - 1 {
					self.pageControl.currentPage = 0
					self.runPlayerView()
				} else {
					self.pageControl.currentPage = self.pageControl.currentPage + 1
				}
			}
		})
		timerAutoSkip.resume()
	}
	//перелистывание страницы
	func goToNextPage(animated: Bool) {
		guard let currentViewController = self.viewControllers?.first else {return}
		guard let nextViewController = dataSource?.pageViewController(self, viewControllerAfter: currentViewController) else {return}
		self.setViewControllers([nextViewController], direction: .forward, animated: animated, completion: nil)
	}

	override func viewDidDisappear(_ animated: Bool) {
		if timerAutoSkip != nil {
			timerAutoSkip.cancel()
		}
	}

	//Инициализация слайда
	func newVc(viewController: String) -> UIViewController {
		return UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: viewController)
	}

	//Создание и конфигурирование индикатора с точками
	func configurePageControl() {
		pageControl = UIPageControl(frame: CGRect(x: 0, y: UIScreen.main.bounds.maxY - 50, width: UIScreen.main.bounds.width, height: 50))
		self.pageControl.numberOfPages = orderedViewControllers.count
		self.pageControl.currentPage = 0
		self.pageControl.tintColor = UIColor.black
		self.pageControl.pageIndicatorTintColor = UIColor(red: 0.0, green: 0.76, blue: 1.00, alpha: 1.0)
		self.pageControl.currentPageIndicatorTintColor = UIColor.black
		self.view.addSubview(pageControl)
	}

	func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
		let pageContentViewController = pageViewController.viewControllers![0]
		self.pageControl.currentPage = orderedViewControllers.index(of: pageContentViewController)!

		if timerRunPlayer != nil {
			timerRunPlayer.cancel()
		}

		if self.pageControl.currentPage == orderedViewControllers.count - 1 {
			self.runPlayerView()
		}
		if timerAutoSkip != nil {
			timerAutoSkip.cancel()
			runAutoskip()
		}
	}

	//Инициализация таймера запуска основного view
	func runPlayerView() {
		let queue = DispatchQueue(label: "AutoSkipTimer", attributes: .concurrent)
		timerRunPlayer = DispatchSource.makeTimerSource(queue: queue)
		timerRunPlayer.scheduleOneshot(deadline: .now() + 1)
		timerRunPlayer.setEventHandler(handler: {
			DispatchQueue.main.async {
				let storyboard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
				let viewController = storyboard.instantiateViewController(withIdentifier: "RadioViewController")
				self.navigationController?.pushViewController(viewController, animated: false)
			}
		})
		timerRunPlayer.resume()
	}

	func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
		guard let viewControllerIndex = orderedViewControllers.index(of: viewController) else {
			return nil
		}

		let previousIndex = viewControllerIndex - 1

		guard previousIndex >= 0 else {
			return orderedViewControllers.last
		}

		guard orderedViewControllers.count > previousIndex else {
			return nil
		}
		return orderedViewControllers[previousIndex]
	}

	func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
		guard let viewControllerIndex = orderedViewControllers.index(of: viewController) else {
			return nil
		}

		let nextIndex = viewControllerIndex + 1
		let orderedViewControllersCount = orderedViewControllers.count

		guard orderedViewControllersCount != nextIndex else {
			return orderedViewControllers.first
		}

		guard orderedViewControllersCount > nextIndex else {
			return nil
		}

		return orderedViewControllers[nextIndex]

	}
	func downloadTracks() {
		guard currentReachabilityStatus != NSObject.ReachabilityStatus.notReachable else {
			return
		}
		DispatchQueue.global(qos: .utility).async {
			Downloader.sharedInstance.load(isSelfFlag: false) {print("First download run")}
		}
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
