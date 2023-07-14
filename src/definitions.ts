interface CapacitorFrame {
  x: number;
  y: number;
  width: number;
  height: number;
}
export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
export type CapacitorIvsPlayerState = "IDLE" | "BUFFERING" | "READY" | "PLAYING" | "ENDED" | "UNKNOWN"; 
export interface CapacitorIvsPlayerPlugin {
  create(options: {
    url: string,
    autoPlay?: boolean,
    toBack?: boolean,
    x?: number, y?: number,
    width ?: number, height ?: number,
    }): Promise<void>;
  start(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  getUrl(): Promise<{ url: string }>;
  getState(): Promise<{ isPlaying: boolean }>;
  setPlayerPosition(options?: { toBack: boolean }): Promise<void>;
  setAutoQuality(options?: { autoQuality?: boolean }): Promise<void>;
  getAutoQuality(): Promise<{ autoQuality: boolean }>;
  setPip(options?: { pip?: boolean }): Promise<void>;
  getPip(): Promise<{ pip: boolean }>;
  setFrame(options?: { x?: number, y?: number, width ?: number, height ?: number }): Promise<void>;
  getFrame(): Promise<CapacitorFrame>;
  setMute(options?: { muted?: boolean }): Promise<void>;
  getMute(): Promise<{ mute: boolean }>;
  setQuality(options?: { quality: string }): Promise<void>;
  getQuality(): Promise<{ quality: string }>;
  getQualities(): Promise<{ qualities: string[] }>;
  addListener(
    eventName: "togglePip",
    listenerFunc: (data: {
      pip: boolean;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "closePip",
    listenerFunc: () => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onState",
    listenerFunc: (data: {
      state: CapacitorIvsPlayerState;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onCues",
    listenerFunc: (data: {
      cues: string;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onDuration",
    listenerFunc: (data: {
      duration: number;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onError",
    listenerFunc: (data: {
      error: string;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onRebuffering",
    listenerFunc: () => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onSeekCompleted",
    listenerFunc: (data: {
      position: number;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onVideoSize",
    listenerFunc: (data: {
      width: number;
      height: number;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: "onQuality",
    listenerFunc: (data: {
      quality: string;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
