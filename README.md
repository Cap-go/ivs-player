# @capgo/ivs-player
  <a href="https://capgo.app/"><img src='https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png' alt='Capgo - Instant updates for capacitor'/></a>
  
<div align="center">
<h2><a href="https://capgo.app/">Check out: Capgo — live updates for capacitor</a></h2>
</div>

[Ivs player](https://docs.aws.amazon.com/ivs/latest/userguide/player.html) for Capacitor app Android and IOS.

## Install

```bash
npm install @capgo/ivs-player
npx cap sync
```


## API

<docgen-index>

* [`create(...)`](#create)
* [`start()`](#start)
* [`cast()`](#cast)
* [`getCastStatus()`](#getcaststatus)
* [`pause()`](#pause)
* [`delete()`](#delete)
* [`getUrl()`](#geturl)
* [`getState()`](#getstate)
* [`setPlayerPosition(...)`](#setplayerposition)
* [`getPlayerPosition()`](#getplayerposition)
* [`setAutoQuality(...)`](#setautoquality)
* [`getAutoQuality()`](#getautoquality)
* [`setPip(...)`](#setpip)
* [`getPip()`](#getpip)
* [`setFrame(...)`](#setframe)
* [`getFrame()`](#getframe)
* [`setBackgroundState(...)`](#setbackgroundstate)
* [`getBackgroundState()`](#getbackgroundstate)
* [`setMute(...)`](#setmute)
* [`getMute()`](#getmute)
* [`setQuality(...)`](#setquality)
* [`getQuality()`](#getquality)
* [`getQualities()`](#getqualities)
* [`getPluginVersion()`](#getpluginversion)
* [`addListener('startPip', ...)`](#addlistenerstartpip-)
* [`addListener('stopPip', ...)`](#addlistenerstoppip-)
* [`addListener('expandPip', ...)`](#addlistenerexpandpip-)
* [`addListener('closePip', ...)`](#addlistenerclosepip-)
* [`addListener('onState', ...)`](#addlisteneronstate-)
* [`addListener('onCues', ...)`](#addlisteneroncues-)
* [`addListener('onDuration', ...)`](#addlisteneronduration-)
* [`addListener('onError', ...)`](#addlisteneronerror-)
* [`addListener('onRebuffering', ...)`](#addlisteneronrebuffering-)
* [`addListener('onSeekCompleted', ...)`](#addlisteneronseekcompleted-)
* [`addListener('onVideoSize', ...)`](#addlisteneronvideosize-)
* [`addListener('onQuality', ...)`](#addlisteneronquality-)
* [`addListener('onCastStatus', ...)`](#addlisteneroncaststatus-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### create(...)

```typescript
create(options: { url: string; pip?: boolean; title?: string; subtitle?: string; cover?: string; autoPlay?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }) => Promise<void>
```

| Param         | Type                                                                                                                                                                                           |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ url: string; pip?: boolean; title?: string; subtitle?: string; cover?: string; autoPlay?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }</code> |

--------------------


### start()

```typescript
start() => Promise<void>
```

--------------------


### cast()

```typescript
cast() => Promise<void>
```

--------------------


### getCastStatus()

```typescript
getCastStatus() => Promise<{ isActive: boolean; }>
```

**Returns:** <code>Promise&lt;{ isActive: boolean; }&gt;</code>

--------------------


### pause()

```typescript
pause() => Promise<void>
```

--------------------


### delete()

```typescript
delete() => Promise<void>
```

--------------------


### getUrl()

```typescript
getUrl() => Promise<{ url: string; }>
```

**Returns:** <code>Promise&lt;{ url: string; }&gt;</code>

--------------------


### getState()

```typescript
getState() => Promise<{ state: CapacitorIvsPlayerState; }>
```

**Returns:** <code>Promise&lt;{ state: <a href="#capacitorivsplayerstate">CapacitorIvsPlayerState</a>; }&gt;</code>

--------------------


### setPlayerPosition(...)

```typescript
setPlayerPosition(options?: { toBack: boolean; } | undefined) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ toBack: boolean; }</code> |

--------------------


### getPlayerPosition()

```typescript
getPlayerPosition() => Promise<{ toBack: boolean; }>
```

**Returns:** <code>Promise&lt;{ toBack: boolean; }&gt;</code>

--------------------


### setAutoQuality(...)

```typescript
setAutoQuality(options?: { autoQuality?: boolean | undefined; } | undefined) => Promise<void>
```

| Param         | Type                                    |
| ------------- | --------------------------------------- |
| **`options`** | <code>{ autoQuality?: boolean; }</code> |

--------------------


### getAutoQuality()

```typescript
getAutoQuality() => Promise<{ autoQuality: boolean; }>
```

**Returns:** <code>Promise&lt;{ autoQuality: boolean; }&gt;</code>

--------------------


### setPip(...)

```typescript
setPip(options?: { pip?: boolean | undefined; } | undefined) => Promise<void>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ pip?: boolean; }</code> |

--------------------


### getPip()

```typescript
getPip() => Promise<{ pip: boolean; }>
```

**Returns:** <code>Promise&lt;{ pip: boolean; }&gt;</code>

--------------------


### setFrame(...)

```typescript
setFrame(options?: { x?: number | undefined; y?: number | undefined; width?: number | undefined; height?: number | undefined; } | undefined) => Promise<void>
```

Set the frame of the player view, all number have to be positive and integers

| Param         | Type                                                                      | Description                                         |
| ------------- | ------------------------------------------------------------------------- | --------------------------------------------------- |
| **`options`** | <code>{ x?: number; y?: number; width?: number; height?: number; }</code> | : number, y: number, width: number, height: number} |

**Since:** 1.0.0

--------------------


### getFrame()

```typescript
getFrame() => Promise<CapacitorFrame>
```

**Returns:** <code>Promise&lt;<a href="#capacitorframe">CapacitorFrame</a>&gt;</code>

--------------------


### setBackgroundState(...)

```typescript
setBackgroundState(options: { backgroundState: CapacitorIvsPlayerBackgroundState; }) => Promise<void>
```

| Param         | Type                                                                                                                  |
| ------------- | --------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ backgroundState: <a href="#capacitorivsplayerbackgroundstate">CapacitorIvsPlayerBackgroundState</a>; }</code> |

--------------------


### getBackgroundState()

```typescript
getBackgroundState() => Promise<{ backgroundState: CapacitorIvsPlayerBackgroundState; }>
```

**Returns:** <code>Promise&lt;{ backgroundState: <a href="#capacitorivsplayerbackgroundstate">CapacitorIvsPlayerBackgroundState</a>; }&gt;</code>

--------------------


### setMute(...)

```typescript
setMute(options?: { muted?: boolean | undefined; } | undefined) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ muted?: boolean; }</code> |

--------------------


### getMute()

```typescript
getMute() => Promise<{ mute: boolean; }>
```

**Returns:** <code>Promise&lt;{ mute: boolean; }&gt;</code>

--------------------


### setQuality(...)

```typescript
setQuality(options?: { quality: string; } | undefined) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ quality: string; }</code> |

--------------------


### getQuality()

```typescript
getQuality() => Promise<{ quality: string; }>
```

**Returns:** <code>Promise&lt;{ quality: string; }&gt;</code>

--------------------


### getQualities()

```typescript
getQualities() => Promise<{ qualities: string[]; }>
```

**Returns:** <code>Promise&lt;{ qualities: string[]; }&gt;</code>

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<{ version: string; }>
```

Get the native Capacitor plugin version

**Returns:** <code>Promise&lt;{ version: string; }&gt;</code>

--------------------


### addListener('startPip', ...)

```typescript
addListener(eventName: "startPip", listenerFunc: () => void) => Promise<PluginListenerHandle>
```

Listen for start pip

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'startPip'</code>    |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('stopPip', ...)

```typescript
addListener(eventName: "stopPip", listenerFunc: () => void) => Promise<PluginListenerHandle>
```

Listen for stop pip

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'stopPip'</code>     |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('expandPip', ...)

```typescript
addListener(eventName: "expandPip", listenerFunc: () => void) => Promise<PluginListenerHandle>
```

Listen for expend pip

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'expandPip'</code>   |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('closePip', ...)

```typescript
addListener(eventName: "closePip", listenerFunc: () => void) => Promise<PluginListenerHandle>
```

Listen for close pip

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'closePip'</code>    |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onState', ...)

```typescript
addListener(eventName: "onState", listenerFunc: (data: { state: CapacitorIvsPlayerState; }) => void) => Promise<PluginListenerHandle>
```

Listen for state changes

| Param              | Type                                                                                                       |
| ------------------ | ---------------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'onState'</code>                                                                                     |
| **`listenerFunc`** | <code>(data: { state: <a href="#capacitorivsplayerstate">CapacitorIvsPlayerState</a>; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onCues', ...)

```typescript
addListener(eventName: "onCues", listenerFunc: (data: { cues: string; }) => void) => Promise<PluginListenerHandle>
```

Listen for cue changes

| Param              | Type                                              |
| ------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'onCues'</code>                             |
| **`listenerFunc`** | <code>(data: { cues: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onDuration', ...)

```typescript
addListener(eventName: "onDuration", listenerFunc: (data: { duration: number; }) => void) => Promise<PluginListenerHandle>
```

Listen for duration changes

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`eventName`**    | <code>'onDuration'</code>                             |
| **`listenerFunc`** | <code>(data: { duration: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onError', ...)

```typescript
addListener(eventName: "onError", listenerFunc: (data: { error: string; }) => void) => Promise<PluginListenerHandle>
```

Listen for errors

| Param              | Type                                               |
| ------------------ | -------------------------------------------------- |
| **`eventName`**    | <code>'onError'</code>                             |
| **`listenerFunc`** | <code>(data: { error: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onRebuffering', ...)

```typescript
addListener(eventName: "onRebuffering", listenerFunc: () => void) => Promise<PluginListenerHandle>
```

Listen for rebuffering

| Param              | Type                         |
| ------------------ | ---------------------------- |
| **`eventName`**    | <code>'onRebuffering'</code> |
| **`listenerFunc`** | <code>() =&gt; void</code>   |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onSeekCompleted', ...)

```typescript
addListener(eventName: "onSeekCompleted", listenerFunc: (data: { position: number; }) => void) => Promise<PluginListenerHandle>
```

Listen for position changes

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`eventName`**    | <code>'onSeekCompleted'</code>                        |
| **`listenerFunc`** | <code>(data: { position: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onVideoSize', ...)

```typescript
addListener(eventName: "onVideoSize", listenerFunc: (data: { width: number; height: number; }) => void) => Promise<PluginListenerHandle>
```

Listen for video size changes

| Param              | Type                                                               |
| ------------------ | ------------------------------------------------------------------ |
| **`eventName`**    | <code>'onVideoSize'</code>                                         |
| **`listenerFunc`** | <code>(data: { width: number; height: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onQuality', ...)

```typescript
addListener(eventName: "onQuality", listenerFunc: (data: { quality: string; }) => void) => Promise<PluginListenerHandle>
```

Listen for quality changes

| Param              | Type                                                 |
| ------------------ | ---------------------------------------------------- |
| **`eventName`**    | <code>'onQuality'</code>                             |
| **`listenerFunc`** | <code>(data: { quality: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### addListener('onCastStatus', ...)

```typescript
addListener(eventName: "onCastStatus", listenerFunc: (data: { isActive: boolean; }) => void) => Promise<PluginListenerHandle>
```

Listen for cast status changes

| Param              | Type                                                   |
| ------------------ | ------------------------------------------------------ |
| **`eventName`**    | <code>'onCastStatus'</code>                            |
| **`listenerFunc`** | <code>(data: { isActive: boolean; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 1.0.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners for this plugin.

**Since:** 1.0.0

--------------------


### Interfaces


#### CapacitorFrame

| Prop         | Type                |
| ------------ | ------------------- |
| **`x`**      | <code>number</code> |
| **`y`**      | <code>number</code> |
| **`width`**  | <code>number</code> |
| **`height`** | <code>number</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### CapacitorIvsPlayerState

<code>"IDLE" | "BUFFERING" | "READY" | "PLAYING" | "ENDED" | "UNKNOWN"</code>


#### CapacitorIvsPlayerBackgroundState

<code>"PAUSED" | "PLAYING"</code>

</docgen-api>


# Credits

This plugin was created originally for [Kick.com](https://kick.com) by [Capgo](https://capgo.app)
