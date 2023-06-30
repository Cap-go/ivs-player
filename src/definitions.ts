interface CapacitorFrame {
  x: number;
  y: number;
  width: number;
  height: number;
}
export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
export interface CapacitorIvsPlayerPlugin {
  create(options: {
    url: string,
    autoPlay?: boolean,
    autoPip?: boolean, 
    autoFulscreen?: boolean,
    toBack?: boolean,
    x?: number, y?: number,
    width ?: number, height ?: number,
    }): Promise<void>;
  start(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  setAutoQuality(options: { autoQuality?: boolean }): Promise<void>;
  getAutoQuality(): Promise<{ autoQuality: boolean }>;
  setPip(options: { pip?: boolean }): Promise<void>;
  getPip(): Promise<{ pip: boolean }>;
  setFrame(options: { x?: number, y?: number, width ?: number, height ?: number }): Promise<void>;
  getFrame(): Promise<CapacitorFrame>;
  setMute(options: { muted?: boolean }): Promise<void>;
  getMute(): Promise<{ mute: boolean }>;
  setQuality(options: { quality: string }): Promise<void>;
  getQuality(): Promise<{ quality: string }>;
  getQualities(): Promise<{ qualities: string[] }>;
  addListener(
    eventName: "tooglePip",
    listenerFunc: (data: {
      pip: boolean;
    }) => void
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
