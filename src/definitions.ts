export interface CapacitorIvsPlayerPlugin {
  create(options: { autoPlay: boolean, url: string, autoPip: boolean, toBack: boolean }): Promise<void>;
  start(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  togglePip(): Promise<void>;
  toggleMute(): Promise<void>;
  setQuality(options: { quality: string }): Promise<void>;
  getQualities(): Promise<{ qualities: string[] }>;
  lowerStream(): Promise<void>;
}
