import { WebPlugin } from '@capacitor/core';

import type { CapacitorIvsPlayerPlugin } from './definitions';

export class CapacitorIvsPlayerWeb
  extends WebPlugin
  implements CapacitorIvsPlayerPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  async lowerStream(): Promise<void> {
    console.log('lowerStream');
    return;
  }
}
