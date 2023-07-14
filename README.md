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
* [`pause()`](#pause)
* [`delete()`](#delete)
* [`getUrl()`](#geturl)
* [`getState()`](#getstate)
* [`setPlayerPosition(...)`](#setplayerposition)
* [`setAutoQuality(...)`](#setautoquality)
* [`getAutoQuality()`](#getautoquality)
* [`setPip(...)`](#setpip)
* [`getPip()`](#getpip)
* [`setFrame(...)`](#setframe)
* [`getFrame()`](#getframe)
* [`setMute(...)`](#setmute)
* [`getMute()`](#getmute)
* [`setQuality(...)`](#setquality)
* [`getQuality()`](#getquality)
* [`getQualities()`](#getqualities)
* [`addListener('togglePip', ...)`](#addlistenertogglepip)
* [`addListener('closePip', ...)`](#addlistenerclosepip)
* [`addListener('onState', ...)`](#addlisteneronstate)
* [`addListener('onCues', ...)`](#addlisteneroncues)
* [`addListener('onDuration', ...)`](#addlisteneronduration)
* [`addListener('onError', ...)`](#addlisteneronerror)
* [`addListener('onRebuffering', ...)`](#addlisteneronrebuffering)
* [`addListener('onSeekCompleted', ...)`](#addlisteneronseekcompleted)
* [`addListener('onVideoSize', ...)`](#addlisteneronvideosize)
* [`addListener('onQuality', ...)`](#addlisteneronquality)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### create(...)

```typescript
create(options: { url: string; autoPlay?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }) => Promise<void>
```

| Param         | Type                                                                                                                         |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ url: string; autoPlay?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }</code> |

--------------------


### start()

```typescript
start() => Promise<void>
```

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
getState() => Promise<{ isPlaying: boolean; }>
```

**Returns:** <code>Promise&lt;{ isPlaying: boolean; }&gt;</code>

--------------------


### setPlayerPosition(...)

```typescript
setPlayerPosition(options?: { toBack: boolean; } | undefined) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ toBack: boolean; }</code> |

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

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code>{ x?: number; y?: number; width?: number; height?: number; }</code> |

--------------------


### getFrame()

```typescript
getFrame() => Promise<CapacitorFrame>
```

**Returns:** <code>Promise&lt;<a href="#capacitorframe">CapacitorFrame</a>&gt;</code>

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


### addListener('togglePip', ...)

```typescript
addListener(eventName: "togglePip", listenerFunc: (data: { pip: boolean; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                              |
| ------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'togglePip'</code>                          |
| **`listenerFunc`** | <code>(data: { pip: boolean; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('closePip', ...)

```typescript
addListener(eventName: "closePip", listenerFunc: () => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'closePip'</code>    |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onState', ...)

```typescript
addListener(eventName: "onState", listenerFunc: (data: { state: CapacitorIvsPlayerState; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                                                                       |
| ------------------ | ---------------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'onState'</code>                                                                                     |
| **`listenerFunc`** | <code>(data: { state: <a href="#capacitorivsplayerstate">CapacitorIvsPlayerState</a>; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onCues', ...)

```typescript
addListener(eventName: "onCues", listenerFunc: (data: { cues: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                              |
| ------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'onCues'</code>                             |
| **`listenerFunc`** | <code>(data: { cues: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onDuration', ...)

```typescript
addListener(eventName: "onDuration", listenerFunc: (data: { duration: number; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`eventName`**    | <code>'onDuration'</code>                             |
| **`listenerFunc`** | <code>(data: { duration: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onError', ...)

```typescript
addListener(eventName: "onError", listenerFunc: (data: { error: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                               |
| ------------------ | -------------------------------------------------- |
| **`eventName`**    | <code>'onError'</code>                             |
| **`listenerFunc`** | <code>(data: { error: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onRebuffering', ...)

```typescript
addListener(eventName: "onRebuffering", listenerFunc: () => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                         |
| ------------------ | ---------------------------- |
| **`eventName`**    | <code>'onRebuffering'</code> |
| **`listenerFunc`** | <code>() =&gt; void</code>   |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onSeekCompleted', ...)

```typescript
addListener(eventName: "onSeekCompleted", listenerFunc: (data: { position: number; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`eventName`**    | <code>'onSeekCompleted'</code>                        |
| **`listenerFunc`** | <code>(data: { position: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onVideoSize', ...)

```typescript
addListener(eventName: "onVideoSize", listenerFunc: (data: { width: number; height: number; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                               |
| ------------------ | ------------------------------------------------------------------ |
| **`eventName`**    | <code>'onVideoSize'</code>                                         |
| **`listenerFunc`** | <code>(data: { width: number; height: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onQuality', ...)

```typescript
addListener(eventName: "onQuality", listenerFunc: (data: { quality: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                 |
| ------------------ | ---------------------------------------------------- |
| **`eventName`**    | <code>'onQuality'</code>                             |
| **`listenerFunc`** | <code>(data: { quality: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

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

</docgen-api>


# Credits

This plugin was created originally for [Kick.com](https://kick.com) by [Capgo](https://capgo.app)
