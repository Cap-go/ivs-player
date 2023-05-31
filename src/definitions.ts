export interface CapacitorIvsPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  lowerStream(): Promise<void>;
}
