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
    let playerDelegate = CapacitorIVSPlayer()
    let playerView = TouchThroughView()
    private var _pipController: Any?
    private var isFScreen = false
    private var originalFrame: CGRect?
    private var originalParent: UIView?
    private var airplayButton = AVRoutePickerView()
    var didRestorePiP: Bool = false
    var autoPlay: Bool = false
    var isCastActive: Bool = false
    var avPlayer: AVPlayer?

    override public func load() {
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

    func handleNewAirPlaySource() {
        print("AirPlay is active")

        self.playerView.player?.pause()
        avPlayer = AVPlayer(url: self.player.path!)
        // Create AVPlayerLayer from AVPlayer
        let playerLayer = AVPlayerLayer(player: avPlayer)
        // Set frame and other properties if you wish here for your playerLayer
        playerLayer.frame = self.playerView.frame

        // Also remove any attached player first, if exist
        self.playerView.player = nil

        self.playerView.layer.addSublayer(playerLayer)
        player.play()
        self.notifyListeners("onState", data: ["state": "PLAYING"])
        // send to listner
        isCastActive = true
        self.notifyListeners("onCastStatus", data: ["isActive": true])
    }

    func handleAirPlaySourceDeactivated() {
        print("AirPlay is disabled")
        // Remove the AVPlayerLayer
        if let sublayers = self.playerView.layer.sublayers {
            for layer in sublayers {
                if let playerLayer = layer as? AVPlayerLayer {
                    playerLayer.player = nil
                    playerLayer.removeFromSuperlayer()
                }
            }
        }
        // Re-attach the original player to the playerView
        self.playerView.player = self.player
        self.player.play()
        self.notifyListeners("onState", data: ["state": "PLAYING"])
        isCastActive = false
        self.notifyListeners("onCastStatus", data: ["isActive": false])
    }

    @objc func handleAudioRouteChange(_ notification: NSNotification) {
        guard let userInfo = notification.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let _ = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else {
            return
        }
        let session = AVAudioSession.sharedInstance()
        print("handleAudioRouteChange \(reasonValue) \(userInfo)")
        for output in session.currentRoute.outputs {
            print("output \(output.portType)")
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
        DispatchQueue.main.async {
            self.player.pause()
        }
    }

    @objc func applicationDidBecomeActive(notification: Notification) {
        guard #available(iOS 15, *), let pipController = pipController else {
            return
        }
        print("CapacitorIVSPlayer applicationDidBecomeActive \(pipController.isPictureInPictureActive)")
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
        if !pipController.isPictureInPicturePossible || isCastActive {
            return false
        }
        if call.getBool("pip", false) {
            pipController.startPictureInPicture()
        } else {
            pipController.stopPictureInPicture()
        }
        print("CapacitorIVSPlayer _setPip done")
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
        print("getPip")
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
        print("CapacitorIVSPlayer _setFrame done")
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

    public func loadUrl(url: String) {
        self.player.load(URL(string: url))
        print("CapacitorIVSPlayer loadUrl")
    }

    public func cyclePlayer(prevUrl: String, nextUrl: String) -> Bool {
        guard let viewController = self.bridge?.viewController else {
            return false
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
        return true
    }

    @objc func cast(_ call: CAPPluginCall) {
        print("CapacitorIVSPlayer cast")

        DispatchQueue.main.async {
            // Create AVPlayer if needed and start playing
            if self.avPlayer == nil {
                self.avPlayer = AVPlayer(url: self.player.path!)
            }

            // Add a AVRoutePickerView to show airplay dialog. You can create this button and add it to your desired place in UI
            let routePickerView = AVRoutePickerView(frame: CGRect(x: 0, y: 0, width: 30.0, height: 30.0))
            routePickerView.activeTintColor = UIColor.blue
            routePickerView.tintColor = UIColor.white
            self.bridge?.viewController?.view.addSubview(routePickerView) // Assumes bridge.viewController is the view you want to add to

            // Pressing the button programmatically to show airplay modal
            for subview in routePickerView.subviews {
                if let button = subview as? UIButton {
                    button.sendActions(for: .touchUpInside)
                    routePickerView.isHidden = true
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
        let toBack = call.getBool("toBack", false)
        autoPlay = call.getBool("autoPlay", false)
        DispatchQueue.main.async {
            let title = call.getString("title", "")
            let subTitle = call.getString("subtitle", "")
            let cover = call.getString("cover", "")
            self.setupNowPlayingInfo(title: title, subTitle: subTitle, url: cover)
            self.setupRemoteTransportControls()
            let setupDone = self.cyclePlayer(prevUrl: self.player.path?.absoluteString ?? "", nextUrl: url)
            print("CapacitorIVSPlayer soon setPip")
            self._setPip(call)
            let FrameDone = self._setFrame(call)
            let PlayerPositionDone = self._setPlayerPosition(toBack: toBack)
            if setupDone && FrameDone && PlayerPositionDone {
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
        completionHandler(true)
    }

    public func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("CapacitorIVSPlayer didRestorePiP \(self.didRestorePiP)")
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
