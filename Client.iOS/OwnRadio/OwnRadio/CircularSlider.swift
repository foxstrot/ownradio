//
//  CircularSlider.swift
//  OwnRadio
//
//  Created by Alexandr Serov on 17.01.2019.
//  Copyright © 2019 Netvox Lab. All rights reserved.
//

import UIKit

class CircularSlider: UIView {

    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */
	@IBInspectable
	open var diskFillColor: UIColor = .clear

	/**
	* The color shown for the unselected portion of the slider disk. (outside start and end values)
	* The default value of this property is the black color with alpha = 0.3.
	*/
	@IBInspectable
	open var diskColor: UIColor = .gray

	/**
	* The color shown for the selected track portion. (between start and end values)
	* The default value of this property is the tint color.
	*/
	@IBInspectable
	open var trackFillColor: UIColor = .clear

	/**
	* The color shown for the unselected track portion. (outside start and end values)
	* The default value of this property is the white color.
	*/
	@IBInspectable
	open var trackColor: UIColor = .white

	/**
	* The width of the circular line
	*
	* The default value of this property is 5.0.
	*/
	@IBInspectable
	open var lineWidth: CGFloat = 5.0

	/**
	* The width of the unselected track portion of the slider
	*
	* The default value of this property is 5.0.
	*/
	@IBInspectable
	open var backtrackLineWidth: CGFloat = 5.0

	/**
	* The shadow offset of the slider
	*
	* The default value of this property is .zero.
	*/
	@IBInspectable
	open var trackShadowOffset: CGPoint = .zero

	/**
	* The color of the shadow offset of the slider
	*
	* The default value of this property is .gray.
	*/
	@IBInspectable
	open var trackShadowColor: UIColor = .gray

	/**
	* The width of the thumb stroke line
	*
	* The default value of this property is 4.0.
	*/
	@IBInspectable
	open var thumbLineWidth: CGFloat = 4.0

	/**
	* The radius of the thumb
	*
	* The default value of this property is 13.0.
	*/
	@IBInspectable
	open var thumbRadius: CGFloat = 13.0

	/**
	* The color used to tint the thumb
	* Ignored if the endThumbImage != nil
	*
	* The default value of this property is the groupTableViewBackgroundColor.
	*/
	@IBInspectable
	open var endThumbTintColor: UIColor = .groupTableViewBackground

	/**
	* The stroke highlighted color of the end thumb
	* The default value of this property is blue
	*/
	@IBInspectable
	open var endThumbStrokeHighlightedColor: UIColor = .blue

	/**
	* The color used to tint the stroke of the end thumb
	* Ignored if the endThumbImage != nil
	*
	* The default value of this property is red.
	*/
	@IBInspectable
	open var endThumbStrokeColor: UIColor = .red

	/**
	* The image of the end thumb
	* Clears any custom color you may have provided for the end thumb.
	*
	* The default value of this property is nil
	*/
	open var endThumbImage: UIImage?

	// MARK: Accessing the Slider’s Value Limits

	/**
	* Fixed number of rounds - how many circles has user to do to reach max value (like apple bedtime clock - which have 2)
	* the default value if this property is 1
	*/
	@IBInspectable
	open var numberOfRounds: Int = 1 {
		didSet {
			assert(numberOfRounds > 0, "Number of rounds has to be positive value!")
			setNeedsDisplay()
		}
	}

	/**
	* The minimum value of the receiver.
	*
	* If you change the value of this property, and the end value of the receiver is below the new minimum, the end point value is adjusted to match the new minimum value automatically.
	* The default value of this property is 0.0.
	*/
	@IBInspectable
	open var minimumValue: CGFloat = 0.0 {
		didSet {
			if endPointValue < minimumValue {
				endPointValue = minimumValue
			}
		}
	}

	/**
	* The maximum value of the receiver.
	*
	* If you change the value of this property, and the end value of the receiver is above the new maximum, the end value is adjusted to match the new maximum value automatically.
	* The default value of this property is 1.0.
	*/
	@IBInspectable
	open var maximumValue: CGFloat = 1.0 {
		didSet {
			if endPointValue > maximumValue {
				endPointValue = maximumValue
			}
		}
	}

	/**
	* The value of the endThumb (changed when the user change the position of the end thumb)
	*
	* If you try to set a value that is above the maximum value, the property automatically resets to the maximum value.
	* And if you try to set a value that is below the minimum value, the property automatically resets  to the minimum value.
	*
	* The default value of this property is 0.5
	*/
	open var endPointValue: CGFloat = 0.5 {
		didSet {
			if oldValue == endPointValue {
				return
			}
			if endPointValue > maximumValue {
				endPointValue = maximumValue
			} else if endPointValue < minimumValue {
				endPointValue = minimumValue
			}

			setNeedsDisplay()
		}
	}

	/**
	* The radius of circle
	*/
	internal var radius: CGFloat {
		get {
			// the minimum between the height/2 and the width/2
			var radius =  min(bounds.center.x, bounds.center.y)
			// all elements should be inside the view rect, for that we should subtract the highest value between the radius of thumb and the line width
			radius -= max(lineWidth, (thumbRadius + thumbLineWidth))
			return radius
		}
	}

	///  See superclass documentation
	override open var isHighlighted: Bool {
		didSet {
			setNeedsDisplay()
		}
	}

	// MARK: init methods

	/**
	See superclass documentation
	*/
	override public init(frame: CGRect) {
		super.init(frame: frame)

		setup()
	}

	/**
	See superclass documentation
	*/
	required public init?(coder aDecoder: NSCoder) {
		super.init(coder: aDecoder)

		setup()
	}

	internal func setup() {
		trackFillColor = tintColor
	}

	struct Circle {
		var origin = CGPoint.zero
		var radius: CGFloat = 0

		init(origin: CGPoint, radius: CGFloat) {
			assert(radius >= 0, NSLocalizedString("Illegal radius", comment: ""))
			self.origin = origin
			self.radius = radius
		}
	}

	struct Arc {
		var circle = Circle(origin: CGPoint.zero, radius: 0)
		var startAngle: CGFloat = 0.0
		var endAngle: CGFloat = 0.0
		init(circle: Circle, startAngle: CGFloat, endAngle: CGFloat) {

			self.circle = circle
			self.startAngle = startAngle
			self.endAngle = endAngle
		}
	}

	override open func draw(_ rect: CGRect) {
		guard let context = UIGraphicsGetCurrentContext() else { return }

	}

	func drawCircularSlider(inContext context: CGContext) {
		diskColor.setFill()
		trackColor.setStroke()
		let circle = Circle(origin: bounds.center, radius: self.radius)
		let sliderArc = Arc(circle: circle, startAngle: 0.0, endAngle: CGFloat(2 * Double.pi))
	}

	static func drawArc(withArc arc: Arc, lineWidth: CGFloat = 2, mode: CGPathDrawingMode = .fillStroke, inContext context: CGContext) {

		let circle = arc.circle
		let origin = circle.origin

		UIGraphicsPushContext(context)
		context.beginPath()

		context.setLineWidth(lineWidth)
		context.setLineCap(CGLineCap.round)
		context.addArc(center: origin, radius: circle.radius, startAngle: arc.startAngle, endAngle: arc.endAngle, clockwise: false)
		context.move(to: CGPoint(x: origin.x, y: origin.y))
		context.drawPath(using: mode)

		UIGraphicsPopContext()
	}

}
extension CGRect {
	var center: CGPoint {
		get {
			let center = CGPoint(x: midX, y: midY)
			return center
		}
	}
}
