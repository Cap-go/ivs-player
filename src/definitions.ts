interface CapacitorFrame {
  x: number;
  y: number;
  width: number;
  height: number;
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
  togglePip(): Promise<void>;
  toggleFullscreen(): Promise<void>;
  setFrame(options: { x?: number, y?: number, width ?: number, height ?: number }): Promise<void>;
  getFrame(): Promise<CapacitorFrame>;
  toggleMute(): Promise<void>;
  setQuality(options: { quality: string }): Promise<void>;
  getQualities(): Promise<{ qualities: string[] }>;
}
