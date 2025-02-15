import Foundation
import Capacitor
import AmazonIVSPlayer
import UIKit
import AVKit
import MediaPlayer

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

class CapacitorIVSPlayer: NSObject, IVSPlayer.Delegate {

    var capacitorPlugin: CapacitorIvsPlayerPlugin!

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        //        print("CapacitorIVSPlayer state change \(state)")
        let stateName = stateToStateName(state)
        print("CapacitorIVSPlayer \(stateName)")
        if state == .ready && capacitorPlugin.autoPlay &&
            !capacitorPlugin.isCastActive {
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
public class CapacitorIvsPlayerPlugin: CAPPlugin, AVPictureInPictureControllerDelegate, MPRemoteCommandCenterDelegate, CAPBridgedPlugin {
    public let identifier = "CapacitorIvsPlayerPlugin"
    public let jsName = "CapacitorIvsPlayer"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "create", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "start", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pause", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getState", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getUrl", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "delete", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setPip", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPip", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setMute", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getMute", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setAutoQuality", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getAutoQuality", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setPlayerPosition", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPlayerPosition", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setQuality", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getQuality", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getQualities", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setFrame", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getFrame", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setBackgroundState", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getBackgroundState", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "removeAllListeners", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "cast", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getCastStatus", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]
    private let PLUGIN_VERSION = "0.13.34"

    let player = IVSPlayer()
    let playerDelegate = CapacitorIVSPlayer()
    let playerView = TouchThroughView()
    private var _pipController: Any?
    private var isFScreen = false
    private var originalFrame: CGRect?
    private var originalParent: UIView?
    private var airplayButton = AVRoutePickerView()
    var didRestorePiP: Bool = false
    var isClosed: Bool = true
    var toBack: Bool = false
    var autoPlay: Bool = false
    var isCastActive: Bool = false
    var avPlayer: AVPlayer?
    var backgroundState: String = "PAUSED"

    override public func load() {
        self.webView?.backgroundColor = UIColor.black
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("CapacitorIVSPlayer ‼️ Could not setup AVAudioSession: \(error)")
        }
        playerDelegate.capacitorPlugin = self
        NotificationCenter.default.addObserver(self, selector: #selector(applicationDidBecomeActive(notification:)), name: UIApplication.didBecomeActiveNotification, object: nil)

        NotificationCenter.default.addObserver(self, selector: #selector(applicationDidEnterBackground(_ :)), name: UIApplication.didEnterBackgroundNotification, object: nil)

        NotificationCenter.default.addObserver(self, selector: #selector(deviceWillLock), name: UIApplication.protectedDataWillBecomeUnavailableNotification, object: nil)

        let routeChangeNotification = AVAudioSession.routeChangeNotification
        NotificationCenter.default.addObserver(self, selector: #selector(handleAudioRouteChange(_:)), name: routeChangeNotification, object: nil)

        player.delegate = playerDelegate
        self.playerView.player = self.player
        self.preparePictureInPicture()
    }

    func createAvPlayer(url: URL?) {
        guard let url = url else {
            return
        }
        if self.avPlayer != nil {
            self.avPlayer?.replaceCurrentItem(with: nil)
        }
        self.avPlayer = AVPlayer(url: url)
        // Create AVPlayerLayer from AVPlayer
        let playerLayer = AVPlayerLayer(player: avPlayer)
        // Set frame and other properties if you wish here for your playerLayer
        playerLayer.frame = self.playerView.frame

        // Also remove any attached player first, if exist
        self.playerView.player = nil

        self.playerView.layer.addSublayer(playerLayer)
    }

    func handleNewAirPlaySource() {
        print("CapacitorIVSPlayer AirPlay is active")
        self.airplayButton.removeFromSuperview() // try to hide the airplay selector
        self.playerView.player?.pause()
        createAvPlayer(url: self.player.path!)
        avPlayer?.play()
        // set PLAYING after 1 sec
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            self.notifyListeners("onState", data: ["state": "PLAYING"])
        }
        // send to listner
        isCastActive = true
        self.notifyListeners("onCastStatus", data: ["isActive": true])
    }

    func removeAvPlayer() {
        // Pause the AVPlayer
        self.avPlayer?.pause()
        self.avPlayer?.rate = 0.0
        print("CapacitorIVSPlayer removeAvPlayer")
        // Detach AVPlayer from AVPlayerLayer
        if let sublayers = self.playerView.layer.sublayers {
            for layer in sublayers {
                if let playerLayer = layer as? AVPlayerLayer {
                    playerLayer.player = nil
                }
            }
        }

        // Remove the AVPlayerLayer
        if let sublayers = self.playerView.layer.sublayers {
            for layer in sublayers {
                if layer is AVPlayerLayer {
                    layer.removeFromSuperlayer()
                }
            }
        }

        // Clear the AVPlayer
        self.avPlayer?.replaceCurrentItem(with: nil)
    }

    func handleAirPlaySourceDeactivated() {
        print("CapacitorIVSPlayer AirPlay is disabled")
        removeAvPlayer()
        isCastActive = false
        self.notifyListeners("onCastStatus", data: ["isActive": false])
        // Re-attach the original player to the playerView
        if isClosed {
            return
        }
        self.playerView.player = self.player
        self.player.play()
        self.notifyListeners("onState", data: ["state": "PLAYING"])

    }

    @objc func handleAudioRouteChange(_ notification: NSNotification) {
        guard let userInfo = notification.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let _ = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else {
            return
        }
        let session = AVAudioSession.sharedInstance()
        print("CapacitorIVSPlayer handleAudioRouteChange \(reasonValue) \(userInfo)")
        for output in session.currentRoute.outputs {
            print("CapacitorIVSPlayer output \(output.portType)")
            if output.portType == AVAudioSession.Port.airPlay && !isCastActive {
                handleNewAirPlaySource()
            } else if output.portType == AVAudioSession.Port.builtInSpeaker && isCastActive {
                handleAirPlaySourceDeactivated()
            }
        }
    }

    @objc func applicationDidEnterBackground(_ notification: NSNotification) {
        print("CapacitorIVSPlayer applicationDidEnterBackground")
        guard #available(iOS 15, *), let pipController = pipController else {
            print("CapacitorIVSPlayer !pipController")
            playerView.player?.pause()
            return
        }
        print("CapacitorIVSPlayer isPictureInPicturePossible: \(pipController.isPictureInPicturePossible)")
        print("CapacitorIVSPlayer isPictureInPictureSuspended: \(pipController.isPictureInPictureSuspended)")
        print("CapacitorIVSPlayer isPictureInPictureActive: \(pipController.isPictureInPictureActive)")
        if !pipController.isPictureInPictureActive {
            playerView.player?.pause()
        }
    }

    @objc func deviceWillLock() {
        print("CapacitorIVSPlayer deviceWillLock")
        if self.backgroundState != "PLAYING" {
            DispatchQueue.main.async {
                self.player.pause()
            }
        }
    }

    @objc func applicationDidBecomeActive(notification: Notification) {
        guard #available(iOS 15, *), let pipController = pipController else {
            return
        }
        print("CapacitorIVSPlayer applicationDidBecomeActive \(pipController.isPictureInPictureActive)")
        if pipController.isPictureInPictureActive {
            pipController.stopPictureInPicture()
            self.notifyListeners("stopPip", data: [:])
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

    func fetchImage(from url: URL, completion: @escaping (UIImage?) -> Void) {
        let task = URLSession.shared.dataTask(with: url) { (data, _, error) in
            guard let data = data, let image = UIImage(data: data), error == nil else {
                completion(nil)
                return
            }
            completion(image)
        }
        task.resume()
    }

    func setupNowPlayingInfo(title: String, subTitle: String, url: String) {
        var nowPlayingInfo: [String: Any] = [
            MPMediaItemPropertyTitle: title,
            MPMediaItemPropertyArtist: subTitle,
            MPMediaItemPropertyMediaType: MPMediaType.anyVideo.rawValue,
            MPNowPlayingInfoPropertyIsLiveStream: true
        ]
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        if let imageUrl = URL(string: url) {
            fetchImage(from: imageUrl) { fetchedImage in
                guard let image = fetchedImage else { return }

                let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in return image }
                nowPlayingInfo.updateValue(artwork, forKey: MPMediaItemPropertyArtwork)
                MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
            }
        }
    }

    func setupRemoteTransportControls() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [unowned self] _ in
            if self.player.state != .playing {
                self.player.play()
                return .success
            }
            return .commandFailed
        }

        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [unowned self] _ in
            if self.player.state == .playing {
                self.player.pause()
                return .success
            }
            return .commandFailed
        }
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.PLUGIN_VERSION])
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
            print("CapacitorIVSPlayer Error: Quality name is not set")
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
            print("CapacitorIVSPlayer Error: Quality not found")
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
        print("CapacitorIVSPlayer getMute")
        call.resolve(["mute": self.player.muted])
    }

    @objc func setMute(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer setMute")
        DispatchQueue.main.async {
            if self.isCastActive && (self.avPlayer != nil) {
                self.avPlayer?.isMuted = call.getBool("mute", !self.avPlayer!.isMuted)
            } else {
                self.player.muted = call.getBool("mute", !self.player.muted)
            }
        }
        call.resolve()
    }

    @objc func _setPip(_ call: CAPPluginCall) -> Bool {
        print("CapacitorIVSPlayer setPip")
        guard #available(iOS 15, *), let pipController = pipController else {
            return false
        }
        // check if isPictureInPicturePossible
        if !pipController.isPictureInPicturePossible {
            return false
        }
        print("CapacitorIVSPlayer isCastActive \(isCastActive)")
        if isCastActive {
            return false
        }
        let ispip = call.getBool("pip", false)
        if ispip {
            isClosed = true
            pipController.startPictureInPicture()
            self.notifyListeners("startPip", data: [:])
        } else {
            isClosed = false
            pipController.stopPictureInPicture()
            self.notifyListeners("stopPip", data: [:])
        }
        print("CapacitorIVSPlayer _setPip \(ispip) done")
        return true
    }

    @objc func setPip(_ call: CAPPluginCall) {
        if _setPip(call) {
            call.resolve()
        } else {
            call.reject("Not possible right now")
        }
    }

    @objc func getPip(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer getPip")
        guard #available(iOS 15, *), let pipController = pipController else {
            call.reject("Not possible right now")
            return
        }
        call.resolve(["pip": pipController.isPictureInPictureActive])
    }

    @objc func _setFrame(_ call: CAPPluginCall) -> Bool {
        guard let viewController = self.bridge?.viewController else {
            return false
        }

        let screenSize: CGRect = UIScreen.main.bounds
        let topPadding = viewController.view.safeAreaInsets.top

        let x = Int(round(call.getFloat("x", Float(0))))
        let y = Int(round(call.getFloat("y", Float(topPadding))))
        let width = Int(round(call.getFloat("width", Float(screenSize.width))))
        let height = Int(round(call.getFloat("height", Float(screenSize.width * (9.0 / 16.0)))))
        self.playerView.playerLayer.zPosition = -1
        self.playerView.frame = CGRect(
            x: x,
            y: y,
            width: width,
            height: height
        )
        print("CapacitorIVSPlayer _setFrame x:\(x) y:\(y) width:\(width) height:\(height) done")
        return true
    }

    @objc func setFrame(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer setFrame x y")
        DispatchQueue.main.async {
            if self._setFrame(call) {
                call.resolve()
            } else {
                call.reject("Unable to _setFrame")
            }
        }
        call.resolve()
    }

    @objc func _setPlayerPosition(toBack: Bool) -> Bool {
        self.toBack = toBack
        if toBack {
            self.webView?.backgroundColor = UIColor.clear
            self.webView?.isOpaque = false
            self.webView?.scrollView.backgroundColor = UIColor.clear
            self.webView?.scrollView.isOpaque = false
        } else {
            guard let viewController = self.bridge?.viewController else {
                return false
            }
            viewController.view.bringSubviewToFront(self.playerView)
        }
        print("CapacitorIVSPlayer _setPlayerPosition done")
        return true
    }

    @objc func setPlayerPosition(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer setPlayerPosition")
        let toBack = call.getBool("toBack", false)
        DispatchQueue.main.async {
            if self._setPlayerPosition(toBack: toBack) {
                call.resolve()
            } else {
                call.reject("Unable to _setPlayerPosition")
            }
        }
    }

    @objc func getPlayerPosition(_ call: CAPPluginCall) {
        call.resolve(["toBack": self.toBack])
    }

    @objc func _setBackgroundState(backgroundState: String) -> Bool {
        if ["PAUSED", "PLAYING"].contains(backgroundState) {
            self.backgroundState = backgroundState
        } else {
            return false
        }
        print("CapacitorIVSPlayer _setBackgroundState done")
        return true
    }

    @objc func setBackgroundState(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer setBackgroundState")
        let backgroundState = call.getString("backgroundState", "PAUSED")
        DispatchQueue.main.async {
            if self._setBackgroundState(backgroundState: backgroundState) {
                call.resolve()
            } else {
                call.reject("Invalid backgroundState: \(backgroundState)")
            }
        }
    }

    @objc func getBackgroundState(_ call: CAPPluginCall) {
        call.resolve(["backgroundState": self.backgroundState])
    }

    public func loadUrl(url: String) {
        let u = URL(string: url)
        self.player.load(u)
        if self.isCastActive {
            self.createAvPlayer(url: u)
            self.avPlayer?.play()
        }
        print("CapacitorIVSPlayer loadUrl")
    }

    public func cyclePlayer(prevUrl: String, nextUrl: String) -> Bool {
        self.removeAvPlayer()
        if prevUrl != nextUrl {
            // add again after 30 ms
            self.player.pause()
            self.player.load(nil)
            self.playerView.removeFromSuperview()
        }
        self.loadUrl(url: nextUrl)
        return true
    }

    @objc func cast(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer cast")

        DispatchQueue.main.async {
            // Create AVPlayer if needed and start playing
            if self.avPlayer != nil {
                self.avPlayer?.replaceCurrentItem(with: nil)
            }
            self.avPlayer = AVPlayer(url: self.player.path!)

            // Add a AVRoutePickerView to show airplay dialog. You can create this button and add it to your desired place in UI
            self.airplayButton = AVRoutePickerView(frame: CGRect(x: 0, y: 0, width: 30.0, height: 30.0))
            self.airplayButton.activeTintColor = UIColor.blue
            self.airplayButton.tintColor = UIColor.white
            self.bridge?.viewController?.view.addSubview(self.airplayButton) // Assumes bridge.viewController is the view you want to add to

            // Pressing the button programmatically to show airplay modal
            for subview in self.airplayButton.subviews {
                if let button = subview as? UIButton {
                    button.sendActions(for: .touchUpInside)
                    self.airplayButton.isHidden = true
                    break
                }
            }
        }
        call.resolve()
    }

    @objc func getCastStatus(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer getCastStatus")
        call.resolve(["isActive": isCastActive])
    }

    @objc func create(_ call: CAPPluginCall) {
        let url = call.getString("url", "")
        autoPlay = call.getBool("autoPlay", false)
        toBack = call.getBool("toBack", false)
        DispatchQueue.main.async {
            let title = call.getString("title", "")
            let subTitle = call.getString("subtitle", "")
            let cover = call.getString("cover", "")
            self.setupNowPlayingInfo(title: title, subTitle: subTitle, url: cover)
            self.setupRemoteTransportControls()
            let setupDone = self.cyclePlayer(prevUrl: self.player.path?.absoluteString ?? "", nextUrl: url)
            print("CapacitorIVSPlayer setupDone \(setupDone)")
            self._setPip(call)
            let FrameDone = self._setFrame(call)
            let PlayerPositionDone = self._setPlayerPosition(toBack: self.toBack)
            if setupDone && FrameDone && PlayerPositionDone {
                self.isClosed = false
                print("CapacitorIVSPlayer success create")
                call.resolve()
            } else {
                call.reject("Unable to cyclePlayer \(setupDone) or _setFrame \(FrameDone) or _setPlayerPosition \(PlayerPositionDone)")
            }
        }
    }

    public func pictureInPictureController(_ pictureInPictureController: AVPictureInPictureController, restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void) {
        print("CapacitorIVSPlayer restoreUserInterfaceForPictureInPictureStopWithCompletionHandler")
        // The user tapped the "restore" button in PiP mode, set the flag to true
        self.didRestorePiP = true
        self.isClosed = false
        completionHandler(true)
    }

    public func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("CapacitorIVSPlayer didRestorePiP \(self.didRestorePiP)")
        if self.didRestorePiP {
            // This was a restore from PiP
            self.notifyListeners("expandPip", data: [:])
            self.didRestorePiP = false
            print("CapacitorIVSPlayer expandPip done")
        } else {
            // This was a close PiP
            self.notifyListeners("closePip", data: [:])
            print("CapacitorIVSPlayer closePip done")
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
        print("CapacitorIVSPlayer preparePictureInPicture done")
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
        print("CapacitorIVSPlayer pause")
        DispatchQueue.main.async {
            if self.isCastActive && (self.avPlayer != nil) {
                self.avPlayer?.pause()
            } else {
                self.player.pause()
            }
        }
        call.resolve()
    }

    @objc func start(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer start")
        DispatchQueue.main.async {
            if self.isCastActive && (self.avPlayer != nil) {
                self.avPlayer?.play()
            } else {
                self.player.play()
            }
        }
        call.resolve()
    }

    @objc func delete(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer delete")
        DispatchQueue.main.async {
            if self.isCastActive && (self.avPlayer != nil) {
                self.avPlayer?.pause()
            } else {
                self.player.pause()
                self.player.load(nil)
            }
        }
        call.resolve()
    }
}
