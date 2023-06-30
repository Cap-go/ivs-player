#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(CapacitorIvsPlayerPlugin, "CapacitorIvsPlayer",
           CAP_PLUGIN_METHOD(create, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(start, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(pause, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(delete, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setPip, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getPip, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setMute, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getMute, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setAutoQuality, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getAutoQuality, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setQuality, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getQuality, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getQualities, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setFrame, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getFrame, CAPPluginReturnPromise);
)
