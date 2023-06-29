export interface CapacitorIvsPlayerPlugin {
  create(options: { url: string, autoPlay?: boolean,  autoPip?:
     boolean, toBack?: boolean,
    x?: number, y?: number,
    width ?: number, height ?: number,
    }): Promise<void>;
  start(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  togglePip(): Promise<void>;
  setFrame(options: { x: number, y: number, width ?: number, height ?: number }): Promise<void>;
  toggleMute(): Promise<void>;
  setQuality(options: { quality: string }): Promise<void>;
  getQualities(): Promise<{ qualities: string[] }>;
  lowerStream(): Promise<void>;
}
