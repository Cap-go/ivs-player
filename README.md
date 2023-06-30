# @capgo/ivs-player

Ivs player for capacitor app

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
* [`setAutoQuality(...)`](#setautoquality)
* [`getAutoQuality()`](#getautoquality)
* [`setPip(...)`](#setpip)
* [`getPip()`](#getpip)
* [`setFrame(...)`](#setframe)
* [`getFrame()`](#getframe)
* [`setMute()`](#setmute)
* [`getMute()`](#getmute)
* [`setQuality(...)`](#setquality)
* [`getQuality()`](#getquality)
* [`getQualities()`](#getqualities)
* [`addListener('tooglePip', ...)`](#addlistenertooglepip)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### create(...)

```typescript
create(options: { url: string; autoPlay?: boolean; autoPip?: boolean; autoFulscreen?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }) => Promise<void>
```

| Param         | Type                                                                                                                                                                     |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ url: string; autoPlay?: boolean; autoPip?: boolean; autoFulscreen?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }</code> |

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


### setAutoQuality(...)

```typescript
setAutoQuality(options: { autoQuality?: boolean; }) => Promise<void>
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
setPip(options: { pip?: boolean; }) => Promise<void>
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
setFrame(options: { x?: number; y?: number; width?: number; height?: number; }) => Promise<void>
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


### setMute()

```typescript
setMute() => Promise<void>
```

--------------------


### getMute()

```typescript
getMute() => Promise<{ mute: boolean; }>
```

**Returns:** <code>Promise&lt;{ mute: boolean; }&gt;</code>

--------------------


### setQuality(...)

```typescript
setQuality(options: { quality: string; }) => Promise<void>
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


### addListener('tooglePip', ...)

```typescript
addListener(eventName: "tooglePip", listenerFunc: (data: { pip: boolean; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                              |
| ------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'tooglePip'</code>                          |
| **`listenerFunc`** | <code>(data: { pip: boolean; }) =&gt; void</code> |

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

</docgen-api>
