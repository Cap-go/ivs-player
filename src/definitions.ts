interface CapacitorFrame {
  x: number;
  y: number;
  width: number;
  height: number;
}
export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
export type CapacitorIvsPlayerState =
  | "IDLE"
  | "BUFFERING"
  | "READY"
  | "PLAYING"
  | "ENDED"
  | "UNKNOWN";
export interface CapacitorIvsPlayerPlugin {
  create(options: {
    url: string;
    pip?: boolean;
    title?: string;
    subtitle?: string;
    cover?: string;
    autoPlay?: boolean;
    toBack?: boolean;
    x?: number;
    y?: number;
    width?: number;

    height?: number;
  }): Promise<void>;
  start(): Promise<void>;
  cast(): Promise<void>;
  getCastStatus(): Promise<{ isActive: boolean }>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  getUrl(): Promise<{ url: string }>;
  getState(): Promise<{ state: CapacitorIvsPlayerState }>;
  setPlayerPosition(options?: { toBack: boolean }): Promise<void>;
  getPlayerPosition(): Promise<{ toBack: boolean }>;
  setAutoQuality(options?: { autoQuality?: boolean }): Promise<void>;
  getAutoQuality(): Promise<{ autoQuality: boolean }>;
  setPip(options?: { pip?: boolean }): Promise<void>;
  getPip(): Promise<{ pip: boolean }>;
  /**
   * Set the frame of the player view, all number have to be positive and integers
   * @param options {x: number, y: number, width: number, height: number}
   * @returns
   * @since 1.0.0
   */
  setFrame(options?: {
    x?: number;
    y?: number;
    width?: number;
    height?: number;
  }): Promise<void>;
  getFrame(): Promise<CapacitorFrame>;
  setMute(options?: { muted?: boolean }): Promise<void>;
  getMute(): Promise<{ mute: boolean }>;
  setQuality(options?: { quality: string }): Promise<void>;
  getQuality(): Promise<{ quality: string }>;
  getQualities(): Promise<{ qualities: string[] }>;

  /**
   * Get the native Capacitor plugin version 
   *
   * @returns {Promise<{ id: string }>} an Promise with version for this device
   * @throws An error if the something went wrong
   */
    getPluginVersion(): Promise<{ version: string }>;
  /**
   * Listen for start pip
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "startPip",
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for stop pip
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "stopPip",
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for expend pip
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "expandPip",
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for close pip
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "closePip",
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for state changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onState",
    listenerFunc: (data: { state: CapacitorIvsPlayerState }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for cue changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onCues",
    listenerFunc: (data: { cues: string }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for duration changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onDuration",
    listenerFunc: (data: { duration: number }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for errors
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onError",
    listenerFunc: (data: { error: string }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for rebuffering
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onRebuffering",
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for position changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onSeekCompleted",
    listenerFunc: (data: { position: number }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for video size changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onVideoSize",
    listenerFunc: (data: { width: number; height: number }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for quality changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onQuality",
    listenerFunc: (data: { quality: string }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Listen for cast status changes
   *
   * @since 1.0.0
   */
  addListener(
    eventName: "onCastStatus",
    listenerFunc: (data: { isActive: boolean }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  /**
   * Remove all listeners for this plugin.
   *
   * @since 1.0.0
   */
  removeAllListeners(): Promise<void>;
}
