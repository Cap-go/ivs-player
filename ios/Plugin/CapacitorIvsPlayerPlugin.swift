import Foundation
import Capacitor
import AmazonIVSPlayer
import UIKit
import AVKit

func stateToStateName (_ state: IVSPlayer.State) -> String {
    switch state {
    case .idle:
        return "IDLE"
    case .buffering:
        return "BUFFERING"
    case .ready:
        return "READY"
    case .playing:
        return "PLAYING"
    case .ended:
        return "ENDED"
    @unknown default:
        return "UNKNOWN"
    }
}

class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    var capacitorPlugin: CapacitorIvsPlayerPlugin!

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        //        print("MyIVSPlayerDelegate state change \(state)")
        let stateName = stateToStateName(state)
        print("MyIVSPlayerDelegate \(stateName)")
        if state == .ready && capacitorPlugin.autoPlay {
            capacitorPlugin.player.play()
        }
        // when playing add to view
        if state == .playing {
            capacitorPlugin.bridge?.viewController?.view.addSubview(capacitorPlugin.playerView)
        }
        capacitorPlugin.notifyListeners("onState", data: ["state": stateName])
    }

    func player(_ player: IVSPlayer, didOutputCue cue: IVSCue) {
        capacitorPlugin.notifyListeners("onCue", data: ["cue": cue])
    }

    func player(_ player: IVSPlayer, didChangeDuration duration: CMTime) {
        capacitorPlugin.notifyListeners("onDuration", data: ["duration": duration.seconds])
    }

    func player(_ player: IVSPlayer, didFailWithError error: Error) {
        capacitorPlugin.notifyListeners("onError", data: ["error": error.localizedDescription])
    }

    func playerWillRebuffer(_ player: IVSPlayer) {
        capacitorPlugin.notifyListeners("onRebuffer", data: [:])
    }
    func player(_ player: IVSPlayer, didSeekTo time: CMTime) {
        capacitorPlugin.notifyListeners("onSeek", data: ["position": time.seconds])
    }
    func player(_ player: IVSPlayer, didChangeVideoSize videoSize: CGSize) {
        capacitorPlugin.notifyListeners("onVideoSize", data: ["videoSize": videoSize])
    }

    func player(_ player: IVSPlayer, didChangeQuality quality: IVSQuality?) {
        capacitorPlugin.notifyListeners("onQuality", data: ["quality": quality?.name ?? ""])
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
    private var _pipController: Any?
    private var isFScreen = false
    private var originalFrame: CGRect?
    private var originalParent: UIView?
    private var airplayButton = AVRoutePickerView()
    var didRestorePiP: Bool = false
    var autoPlay: Bool = false

    override public func load() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("MyIVSPlayerDelegate ‼️ Could not setup AVAudioSession: \(error)")
        }
        playerDelegate.capacitorPlugin = self
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
        print("MyIVSPlayerDelegate applicationDidBecomeActive \(pipController.isPictureInPictureActive)")
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
            print("MyIVSPlayerDelegate Error: Quality name is not set")
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
            print("MyIVSPlayerDelegate Error: Quality not found")
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

    @objc func getMute(_ call: CAPPluginCall) {
        print("MyIVSPlayerDelegate getMute")
        call.resolve(["mute": self.player.muted])
    }

    @objc func setMute(_ call: CAPPluginCall) {
        print("MyIVSPlayerDelegate setMute")
        DispatchQueue.main.async {
            self.player.muted = call.getBool("muted", !self.player.muted)
        }
        call.resolve()
    }

    @objc func _setPip(_ call: CAPPluginCall) -> Bool {
        print("MyIVSPlayerDelegate setPip")
        guard #available(iOS 15, *), let pipController = pipController else {
            call.reject("Not possible right now")
            return false
        }
        // check if isPictureInPicturePossible
        if !pipController.isPictureInPicturePossible {
            call.reject("Not possible right now")
            return false
        }
        if call.getBool("pip", false) {
            pipController.startPictureInPicture()
        } else {
            pipController.stopPictureInPicture()
        }
        return true
    }

    @objc func setPip(_ call: CAPPluginCall) {
        if (_setPip(call)) {
            call.resolve()
        }
    }

    @objc func getPip(_ call: CAPPluginCall) {
        print("getPip")
        guard #available(iOS 15, *), let pipController = pipController else {
            call.reject("Not possible right now")
            return
        }
        call.resolve(["pip": pipController.isPictureInPictureActive])
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

    @objc func _setPlayerPosition(toBack: Bool) {
        DispatchQueue.main.async {
            if toBack {
                self.webView?.backgroundColor = UIColor.clear
                self.webView?.isOpaque = false
                self.webView?.scrollView.backgroundColor = UIColor.clear
                self.webView?.scrollView.isOpaque = false
            } else {
                guard let viewController = self.bridge?.viewController else {
                    return
                }
                viewController.view.bringSubviewToFront(self.playerView)
            }
        }
    }

    @objc func setPlayerPosition(_ call: CAPPluginCall) {
        print("setPlayerPosition")
        let toBack = call.getBool("toBack", false)
        _setPlayerPosition(toBack: toBack)
        call.resolve()
    }

    public func loadUrl(url: String) {
        DispatchQueue.main.async {
            self.player.load(URL(string: url))
            print("MyIVSPlayerDelegate loadUrl")
        }
    }

    public func cyclePlayer(prevUrl: String, nextUrl: String) {
        guard let viewController = self.bridge?.viewController else {
            return
        }
        if prevUrl != nextUrl {
            // add again after 30 ms
            self.player.pause()
            self.player.load(nil)
            self.playerView.removeFromSuperview()
            self.loadUrl(url: nextUrl)
        } else {
            viewController.view.addSubview(self.playerView)
            self.loadUrl(url: nextUrl)
        }
    }

    @objc func create(_ call: CAPPluginCall) {
        let url = call.getString("url", "")
        let toBack = call.getBool("toBack", false)
        autoPlay = call.getBool("autoPlay", false)
        DispatchQueue.main.async {
            self.cyclePlayer(prevUrl: self.player.path?.absoluteString ?? "", nextUrl: url)
            print("MyIVSPlayerDelegate soon setPip")
            self._setPip(call)
            self._setFrame(call)
            self._setPlayerPosition(toBack: toBack)
        }
        call.resolve()
    }

    public func pictureInPictureController(_ pictureInPictureController: AVPictureInPictureController, restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void) {
        print("MyIVSPlayerDelegate restoreUserInterfaceForPictureInPictureStopWithCompletionHandler")
        // The user tapped the "restore" button in PiP mode, set the flag to true
        self.didRestorePiP = true
        completionHandler(true)
    }

    public func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("MyIVSPlayerDelegate didRestorePiP \(self.didRestorePiP)")
        if self.didRestorePiP {
            // This was a restore from PiP
            self.notifyListeners("expandPip", data: [:])
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
        print("MyIVSPlayerDelegate preparePictureInPicture done")
    }

    @objc func getUrl(_ call: CAPPluginCall) {
        guard let url = player.path else {
            call.reject("No url found")
            return
        }
        call.resolve(["url": url.absoluteString])
    }

    @objc func getState(_ call: CAPPluginCall) {
        let stateName = stateToStateName(player.state)
        call.resolve(["state": stateName])
    }

    @objc func pause(_ call: CAPPluginCall) {
        print("MyIVSPlayerDelegate pause")
        DispatchQueue.main.async {
            self.player.pause()
        }
        call.resolve()
    }

    @objc func start(_ call: CAPPluginCall) {
        print("MyIVSPlayerDelegate start")
        DispatchQueue.main.async {
            self.player.play()
        }
        call.resolve()
    }

    @objc func delete(_ call: CAPPluginCall) {
        print("MyIVSPlayerDelegate delete")
        DispatchQueue.main.async {
            self.player.pause()
            self.player.load(nil)
        }
        call.resolve()
    }
}
