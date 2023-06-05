export interface CapacitorIvsPlayerPlugin {
  create(options: { autoPlay: boolean, url: string }): Promise<void>;
  start(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  togglePip(): Promise<void>;
  lowerStream(): Promise<void>;
}
