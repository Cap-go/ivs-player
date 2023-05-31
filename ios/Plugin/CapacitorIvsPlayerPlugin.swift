import Foundation
import Capacitor
import AmazonIVSPlayer


class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        print("state change")
    }

    func player(_ player: IVSPlayer, didFailWithError error: Error) {
        print("error change")
    }

    func player(_ player: IVSPlayer, didChangeDuration duration: CMTime) {
        print("duration change")
    }

    func player(_ player: IVSPlayer, didOutputCue cue: IVSCue) {
        print("didOutputCue change")
        switch cue {
        case let textMetadataCue as IVSTextMetadataCue:
            print("Received Timed Metadata (\(textMetadataCue.textDescription)): \(textMetadataCue.text)")
        case let textCue as IVSTextCue:
            print("Received Text Cue: “\(textCue.text)”")
        default:
            print("Received unknown cue (type \(cue.type))")
        }
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

    @available(iOS 15, *)
    private var pipController: AVPictureInPictureController? {
       get {
           return _pipController as! AVPictureInPictureController?
       }
       set {
           _pipController = newValue
       }
   }
    
    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        print("hello")
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
            let topPadding = window?.safeAreaInsets.top ?? 0
            
            self.playerView.frame = viewController.view.bounds
            self.playerView.playerLayer.position.x = 0 + screenSize.width / 2
            self.playerView.playerLayer.position.y = topPadding + (screenSize.width * (9/16)) / 2
            self.playerView.playerLayer.zPosition = -1
            viewController.view.addSubview(self.playerView)
            DispatchQueue.main.async {
                self.webView?.backgroundColor = UIColor.clear
                self.webView?.isOpaque = false
                self.webView?.scrollView.backgroundColor = UIColor.clear
                self.webView?.scrollView.isOpaque = false
//                self.webView?.superview?.bringSubviewToFront(self.webView!)
            }
        }
        call.resolve([
            "value": implementation.echo(value)
        ])
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

    }
}
