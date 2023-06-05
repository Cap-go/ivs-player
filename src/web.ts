import { WebPlugin } from '@capacitor/core';

import type { CapacitorIvsPlayerPlugin } from './definitions';

export class CapacitorIvsPlayerWeb
  extends WebPlugin
  implements CapacitorIvsPlayerPlugin
{
  async create(options: { autoPlay: boolean }): Promise<void> {
    console.log('ECHO', options);
    return;
  }
  async start(): Promise<void> {
    console.log('start');
    return;
  }
  async stop(): Promise<void> {
    console.log('stop');
    return;
  }
  async pause(): Promise<void> {
    console.log('pause');
    return;
  }
  async delete(): Promise<void> {
    console.log('delete');
    return;
  }
  async togglePip(): Promise<void> {
    console.log('togglePip');
  }
  async lowerStream(): Promise<void> {
    console.log('lowerStream');
    return;
  }
}
