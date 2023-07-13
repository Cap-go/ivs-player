import { WebPlugin } from '@capacitor/core';

import type { CapacitorIvsPlayerPlugin } from './definitions';

export class CapacitorIvsPlayerWeb
  extends WebPlugin
  implements CapacitorIvsPlayerPlugin
{
  async create(options: { autoPlay: boolean, url: string, toBack: boolean }): Promise<void> {
    console.log('create', options);
    return;
  }
  async start(): Promise<void> {
    console.log('start');
    return;
  }
  async pause(): Promise<void> {
    console.log('pause');
    return;
  }
  async getUrl(): Promise<{ url: string }> {
    console.log('getUrl');
    return { url: '' };
  }
  async getState(): Promise<{ isPlaying: boolean }> {
    console.log('getState');
    return { isPlaying: false };
  }
  async setPlayerPosition(options: { toBack: boolean }): Promise<void> {
    console.log('setPlayerPosition', options);
    return;
  }
  async delete(): Promise<void> {
    console.log('delete');
    return;
  }
  async setFrame(options: { x: number, y: number, width: number, height: number }): Promise<void> {
    console.log('setPosition', options);
    return;
  }
  async getFrame(): Promise<{ x: number, y: number, width: number, height: number }> {
    console.log('getPosition');
    return { x: 0, y: 0, width: 0, height: 0 };
  }
  async setPip(): Promise<void> {
    console.log('setPip');
  }
  async getPip(): Promise<{ pip: boolean }> {
    console.log('getPip');
    return { pip: false };
  }
  async setFullscreen(): Promise<void> {
    console.log('toggleFullscreen');
    return;
  }
  async getFullscreen(): Promise<{ fullscreen: boolean }> {
    console.log('getFullscreen');
    return { fullscreen: false };
  }
  async setMute(): Promise<void> {
    console.log('toggleMute');
    return;
  }
  async getMute(): Promise<{ mute: boolean }> {
    console.log('getMute');
    return { mute: false };
  }
  async setQuality(options: { quality: string }): Promise<void> {
    console.log('setQuality', options);
    return;
  }
  async getQualities(): Promise<{ qualities: string[] }> {
    console.log('getQualities');
    return { qualities: [] };
  }
  async getQuality(): Promise<{ quality: string }> {
    console.log('getQuality');
    return { quality: '' };
  }
  async setAutoQuality(options: { autoQuality: boolean }): Promise<void> {
    console.log('setAutoQuality', options);
    return;
  }
  async getAutoQuality(): Promise<{ autoQuality: boolean }> {
    console.log('getAutoQuality');
    return { autoQuality: false };
  }
}
