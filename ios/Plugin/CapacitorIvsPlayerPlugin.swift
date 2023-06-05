import Foundation
import Capacitor
import AmazonIVSPlayer


class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        print("state change")
        if state == .ready {
            player.play()
        }
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
    
    @objc func create(_ call: CAPPluginCall) {
        player.delegate = playerDelegate
        player.load(URL(string:"https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8"))

        DispatchQueue.main.async {
            self.playerView.player = self.player
            self.preparePictureInPicture()
            self.player.play()
            guard let viewController = self.bridge?.viewController else {
                call.reject("Unable to access the view controller")
                return
            }
            
            let screenSize: CGRect = UIScreen.main.bounds
            let window = UIApplication.shared.windows.first
            let topPadding = viewController.view.safeAreaInsets.top
                        
            self.playerView.playerLayer.zPosition = -1
            self.playerView.frame = CGRect(
                x: 0,
                y: topPadding,
                width: screenSize.width,
                height: screenSize.width * (9.0 / 16.0)
            )
            
            viewController.view.addSubview(self.playerView)
            
            DispatchQueue.main.async {
                self.webView?.backgroundColor = UIColor.clear
                self.webView?.isOpaque = false
                self.webView?.scrollView.backgroundColor = UIColor.clear
                self.webView?.scrollView.isOpaque = false
//                self.webView?.superview?.bringSubviewToFront(self.webView!)
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
