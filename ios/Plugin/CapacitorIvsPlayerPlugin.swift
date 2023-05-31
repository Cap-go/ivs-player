import Foundation
import Capacitor
import AmazonIVSPlayer


class MyIVSPlayerDelegate: NSObject, IVSPlayer.Delegate {

    func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
//        updateForState(state)
        print("state change")
    }

    func player(_ player: IVSPlayer, didFailWithError error: Error) {
//        presentError(error, componentName: "Player")
        print("error change")
    }

    func player(_ player: IVSPlayer, didChangeDuration duration: CMTime) {
//        updateForDuration(duration: duration)
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
    
    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        print("hello")
        player.delegate = playerDelegate
//        playerView.player = player
        player.load(URL(string:"https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8"))
        DispatchQueue.main.async {
            self.playerView.player = self.player
            self.player.play()
            guard let viewController = self.bridge?.viewController else {
                call.reject("Unable to access the view controller")
                return
            }
            
            let screenSize: CGRect = UIScreen.main.bounds
            let window = UIApplication.shared.windows.first
            let topPadding = window?.safeAreaInsets.top ?? 0
            let playerWidth = self.playerView.playerLayer.videoRect.width
            let playerHeight = self.playerView.playerLayer.videoRect.height
            
            self.playerView.frame = viewController.view.bounds
            self.playerView.playerLayer.position.x = 0 + screenSize.width / 2
            self.playerView.playerLayer.position.y = topPadding + (screenSize.width * (9/16)) / 2
            viewController.view.addSubview(self.playerView)
        }
//        let playerView = player.view
//        playerView.frame = viewController.view.bounds
//        viewController.view.addSubview(playerView)
//        DispatchQueue.main.async {
//            self.webView?.backgroundColor = UIColor.clear
//            self.webView?.isOpaque = false
//            self.webView?.scrollView.backgroundColor = UIColor.clear
//            self.webView?.scrollView.isOpaque = false
//            self.webView?.superview?.bringSubviewToFront(self.webView!)
//        }
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
