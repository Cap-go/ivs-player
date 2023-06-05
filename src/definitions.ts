export interface CapacitorIvsPlayerPlugin {
  create(options: { autoPlay: boolean }): Promise<void>;
  start(): Promise<void>;
  stop(): Promise<void>;
  pause(): Promise<void>;
  delete(): Promise<void>;
  togglePip(): Promise<void>;
  lowerStream(): Promise<void>;
}
