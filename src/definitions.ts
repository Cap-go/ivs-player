export interface CapacitorIvsPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  togglePip(): Promise<void>;
  lowerStream(): Promise<void>;
}
