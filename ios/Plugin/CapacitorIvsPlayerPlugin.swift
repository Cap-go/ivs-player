import Foundation
import Capacitor
import AmazonIVSPlayer


class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        print("state change")
//        if state == .ready {
//            player.play()
//        }
    }

    func player(_ player: IVSPlayer, didFailWithError error: Error) {
        print("error change")
    }

    func player(_ player: IVSPlayer, didChangeDuration duration: CMTime) {
        print("duration change")
    }

    func player(_ player: IVSPlayer, didOutputCue cue: IVSCue) {
//        print("didOutputCue change")
//        switch cue {
//        case let textMetadataCue as IVSTextMetadataCue:
//            print("Received Timed Metadata (\(textMetadataCue.textDescription)): \(textMetadataCue.text)")
//        case let textCue as IVSTextCue:
//            print("Received Text Cue: “\(textCue.text)”")
//        default:
//            print("Received unknown cue (type \(cue.type))")
//        }
    }

    func playerWillRebuffer(_ player: IVSPlayer) {
        print("Player will rebuffer and resume playback")
    }
}
/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorIvsPlayerPlugin)
public class CapacitorIvsPlayerPlugin: CAPPlugin {
    private let implementation = CapacitorIvsPlayer()
    let player = IVSPlayer()
    let playerDelegate = MyIVSPlayerDelegate()
    let playerView = IVSPlayerView()
    
    private var _pipController: Any? = nil

    public override func load() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("‼️ Could not setup AVAudioSession: \(error)")
        }
        NotificationCenter.default.addObserver(self, selector: #selector(applicationDidBecomeActive(notification:)), name: UIApplication.didBecomeActiveNotification, object: nil)

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
    
    @objc func getQualities(_ call: CAPPluginCall) {
        print("getQualities")
        var qualities = String()
        for quality in self.player.qualities {
            qualities.append(quality.name)
        }
        call.resolve([qualities: qualities])
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

    @objc func toggleMute(_ call: CAPPluginCall) {
        print("toggleMute")
        DispatchQueue.main.async {
            self.player.muted = !self.player.muted
        }
        call.resolve()
    }

    @objc func togglePip(_ call: CAPPluginCall) {
        print("togglePip")
        guard #available(iOS 15, *), let pipController = pipController else {
            return
        }
        print("isPictureInPictureActive \(pipController.isPictureInPictureActive)")
        if pipController.isPictureInPictureActive {
            pipController.stopPictureInPicture()
        } else {
            pipController.startPictureInPicture()
        }
        call.resolve()
    }
    @objc func setFrame(_ call: CAPPluginCall) {
        print("setFrame x y")
        DispatchQueue.main.async {
            guard let viewController = self.bridge?.viewController else {
                call.reject("Unable to access the view controller")
                return
            }
            
            let screenSize: CGRect = UIScreen.main.bounds
            let topPadding = viewController.view.safeAreaInsets.top
            let BottomPadding = viewController.view.safeAreaInsets.bottom

            let x = call.getInt("x", 0)
            let y = call.getInt("y", Int(topPadding))
            let width = call.getInt("width", Int(screenSize.width))
            let height = call.getInt("height", Int(screenSize.width) * (9 / 16))
            self.playerView.frame = CGRect(
                x: x,
                y: y,
                width: width,
                height: height
            )
        }
        call.resolve()
    }

    
    @objc func create(_ call: CAPPluginCall) {
        player.delegate = playerDelegate

        let url = call.getString("url", "")
        let autoPlay = call.getBool("autoPlay", false)
        let toBack = call.getBool("toBack", false)
        let autoPip = call.getBool("autoPip", false)
        player.load(URL(string:url))

        DispatchQueue.main.async {
            self.playerView.player = self.player
            self.preparePictureInPicture()
            if (autoPlay) {
                self.player.play()
            }
            if #available(iOS 15, *) {
                if (autoPip && (self.pipController != nil)) {
                    self.pipController?.startPictureInPicture()
                }
            }
            guard let viewController = self.bridge?.viewController else {
                call.reject("Unable to access the view controller")
                return
            }
            
            let screenSize: CGRect = UIScreen.main.bounds
            let topPadding = viewController.view.safeAreaInsets.top
            let BottomPadding = viewController.view.safeAreaInsets.bottom

            let x = call.getInt("x", 0)
            let y = call.getInt("y", Int(topPadding))
            let width = call.getInt("width", Int(screenSize.width))
            let height = call.getInt("height", Int(screenSize.width) * (9 / 16))
                        
            self.playerView.playerLayer.zPosition = -1
            self.playerView.frame = CGRect(
                x: x,
                y: y,
                width: width,
                height: height
            )
            
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
        pipController.canStartPictureInPictureAutomaticallyFromInline = true
        print("preparePictureInPicture done")
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
    
    @objc func lowerStream(_ call: CAPPluginCall) {
        print("lowerStream")
        DispatchQueue.main.async {
            self.playerView.frame = CGRect(
                x: self.playerView.frame.minX,
                y: self.playerView.frame.minY + 20,
                width: self.playerView.frame.width,
                height: self.playerView.frame.height
            )
        }
        call.resolve()
    }
}
