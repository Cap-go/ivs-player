import Foundation
import Capacitor
import AmazonIVSPlayer
import UIKit
import AVKit

class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
//        print("MyIVSPlayerDelegate state change \(state)")
    }

    func player(_ player: IVSPlayer, didFailWithError error: Error) {
//        print("MyIVSPlayerDelegate error change \(error)")
    }

    func player(_ player: IVSPlayer, didChangeDuration duration: CMTime) {
//        print("MyIVSPlayerDelegate duration change \(duration)")
    }

    func player(_ player: IVSPlayer, didOutputCue cue: IVSCue) {
//        print("MyIVSPlayerDelegate didOutputCue change \(cue)")
    }

    func playerWillRebuffer(_ player: IVSPlayer) {
//        print("MyIVSPlayerDelegate Player will rebuffer and resume playback")
    }
}

class TouchThroughView: IVSPlayerView {
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let view = super.hitTest(point, with: event)
        return view == self ? nil : view
    }
}

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorIvsPlayerPlugin)
public class CapacitorIvsPlayerPlugin: CAPPlugin, AVPictureInPictureControllerDelegate {
    let player = IVSPlayer()
    let playerDelegate = MyIVSPlayerDelegate()
    let playerView = TouchThroughView()
    private var _pipController: Any? = nil
    private var isFScreen = false
    private var originalFrame: CGRect?
    private var originalParent: UIView?
    private var airplayButton = AVRoutePickerView()
    var didRestorePiP: Bool = false

    public override func load() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("‼️ Could not setup AVAudioSession: \(error)")
        }
        NotificationCenter.default.addObserver(self, selector: #selector(applicationDidBecomeActive(notification:)), name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(deviceWillLock), name: UIApplication.protectedDataWillBecomeUnavailableNotification, object: nil)
        player.delegate = playerDelegate
        self.playerView.player = self.player
        self.preparePictureInPicture()
    }

    @objc func deviceWillLock() {
        DispatchQueue.main.async {
            self.player.pause()
        }
    }

    @objc func applicationDidBecomeActive(notification: Notification) {
        guard #available(iOS 15, *), let pipController = pipController else {
            return
        }
        print("applicationDidBecomeActive \(pipController.isPictureInPictureActive)")
        if pipController.isPictureInPictureActive {
            pipController.stopPictureInPicture()
        }
    }
    
    @available(iOS 15, *)
    private var pipController: AVPictureInPictureController? {
        get {
            return _pipController as! AVPictureInPictureController?
        }
        set {
            _pipController = newValue
        }
    }

    @objc func getAutoQuality(_ call: CAPPluginCall) {

        call.resolve(["autoQuality": self.player.autoQualityMode])
    }

    @objc func setAutoQuality(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.player.autoQualityMode = call.getBool("autoQuality", !self.player.autoQualityMode)
        }
        call.resolve()
    }
    
    @objc func getQualities(_ call: CAPPluginCall) {
        var qualities = [String]()
        for quality in self.player.qualities {
            qualities.append(quality.name)
        }
        call.resolve(["qualities": qualities])
    }

    @objc func getQuality(_ call: CAPPluginCall) {
        call.resolve(["quality": self.player.quality?.name ?? ""])
    }
        
    @objc func setQuality(_ call: CAPPluginCall) {
        guard let targetQualityName = call.getString("quality") else {
            print("Error: Quality name is not set")
            call.reject("Quality name is not set")
            return
        }

        var selectedQuality: IVSQuality?
        
        // find quality in list
        for quality in self.player.qualities {
            if quality.name == targetQualityName {
                selectedQuality = quality
                break
            }
        }
        
        // Check if we found quality
        guard let targetQuality = selectedQuality else {
            print("Error: Quality not found")
            call.reject("Quality not found")
            return
        }
        
        // Set quality
        DispatchQueue.main.async {
            self.player.quality = targetQuality
        }

        call.resolve()
    }

    @objc func getFrame(_ call: CAPPluginCall) {
        let frame = playerView.frame
        let frameDict: [String: CGFloat] = [
        "x": frame.origin.x,
        "y": frame.origin.y,
        "width": frame.size.width,
        "height": frame.size.height
        ]
        call.resolve(frameDict)
    }
    
    @objc func setMute(_ call: CAPPluginCall) {
        print("setMute")
        DispatchQueue.main.async {
            self.player.muted = call.getBool("muted", !self.player.muted)
        }
        call.resolve()
    }
    
    @objc func setPip(_ call: CAPPluginCall) {
        print("setPip")
        guard #available(iOS 15, *), let pipController = pipController else {
            return
        }
        // check if isPictureInPicturePossible
        if (!pipController.isPictureInPicturePossible) {
            call.reject("Not possible right now")
            return
        }
        if call.getBool("pip", !pipController.isPictureInPictureActive) {
            pipController.startPictureInPicture()
        } else {
            pipController.stopPictureInPicture()
        }
        call.resolve()
    }
    
    @objc func _setFrame(_ call: CAPPluginCall) {
        guard let viewController = self.bridge?.viewController else {
            call.reject("Unable to access the view controller")
            return
        }
        
        let screenSize: CGRect = UIScreen.main.bounds
        let topPadding = viewController.view.safeAreaInsets.top

        let x = call.getInt("x", 0)
        let y = call.getInt("y", Int(topPadding))
        let width = call.getInt("width", Int(screenSize.width))
        let height = call.getInt("height", Int(screenSize.width * (9.0 / 16.0)))
        self.playerView.playerLayer.zPosition = -1
        self.playerView.frame = CGRect(
            x: x,
            y: y,
            width: width,
            height: height
        )
    }

    @objc func setFrame(_ call: CAPPluginCall) {
        print("setFrame x y")
        DispatchQueue.main.async {
            self._setFrame(call)
        }
        call.resolve()
    }

    
    @objc func create(_ call: CAPPluginCall) {
        let url = call.getString("url", "")
        let autoPlay = call.getBool("autoPlay", false)
        let toBack = call.getBool("toBack", false)
        player.load(URL(string:url))
        DispatchQueue.main.async {
            if (autoPlay) {
                self.player.play()
            }
            self._setFrame(call)
            guard let viewController = self.bridge?.viewController else {
                call.reject("Unable to access the view controller")
                return
            }
            if (toBack) {
                viewController.view.addSubview(self.playerView)
                DispatchQueue.main.async {
                    self.webView?.backgroundColor = UIColor.clear
                    self.webView?.isOpaque = false
                    self.webView?.scrollView.backgroundColor = UIColor.clear
                    self.webView?.scrollView.isOpaque = false
                }
            } else {
                viewController.view.addSubview(self.playerView)
                viewController.view.bringSubviewToFront(self.playerView)
            }
        }
        call.resolve()
    }
    
    public func pictureInPictureController(_ pictureInPictureController: AVPictureInPictureController, restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void) {
        print("restoreUserInterfaceForPictureInPictureStopWithCompletionHandler")
        // The user tapped the "restore" button in PiP mode, set the flag to true
        self.didRestorePiP = true
        completionHandler(true)
    }

    public func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("MyIVSPlayerDelegate didRestorePiP \(self.didRestorePiP)")
        if self.didRestorePiP {
            // This was a restore from PiP
            self.notifyListeners("tooglePip", data: ["pip": false])
            self.didRestorePiP = false
        } else {
           // This was a close PiP
            self.notifyListeners("closePip", data: [:])
        }
    }

    
    private func preparePictureInPicture() {

        guard #available(iOS 15, *), AVPictureInPictureController.isPictureInPictureSupported() else {
            return
        }
        
        if let existingController = self.pipController {
            if existingController.ivsPlayerLayer == playerView.playerLayer {
                return
            }
            self.pipController = nil
        }

        guard let pipController = AVPictureInPictureController(ivsPlayerLayer: playerView.playerLayer) else {
            return
        }

        self.pipController = pipController
        pipController.delegate = self
        pipController.canStartPictureInPictureAutomaticallyFromInline = true
        print("preparePictureInPicture done")
    }
    

    @objc func getUrl(_ call: CAPPluginCall) {
        guard let url = player.path else {
            call.reject("No url found")
            return
        }
        call.resolve(["url": url.absoluteString])
    }

    @objc func getState(_ call: CAPPluginCall) {
        let isPlaying = player.state == .playing
        call.resolve(["isPlaying": isPlaying])
    }
    
    @objc func pause(_ call: CAPPluginCall) {
        print("pause")
        DispatchQueue.main.async {
            self.player.pause()
        }
        call.resolve()
    }

    @objc func start(_ call: CAPPluginCall) {
        print("start")
        DispatchQueue.main.async {
            self.player.play()
        }
        call.resolve()
    }

    @objc func delete(_ call: CAPPluginCall) {
        print("delete")
        DispatchQueue.main.async {
            self.player.pause()
            self.playerView.removeFromSuperview()
        }
        call.resolve()
    }
}
