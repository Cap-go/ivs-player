#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(CapacitorIvsPlayerPlugin, "CapacitorIvsPlayer",
           CAP_PLUGIN_METHOD(create, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(start, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(pause, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(delete, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(togglePip, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(toggleMute, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(toggleFullscreen, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setQuality, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getQualities, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setFrame, CAPPluginReturnPromise);
)
