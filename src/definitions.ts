export interface CapacitorIvsPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
