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
* [`togglePip()`](#togglepip)
* [`setFrame(...)`](#setframe)
* [`toggleMute()`](#togglemute)
* [`setQuality(...)`](#setquality)
* [`getQualities()`](#getqualities)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### create(...)

```typescript
create(options: { url: string; autoPlay?: boolean; autoPip?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }) => Promise<void>
```

| Param         | Type                                                                                                                                            |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ url: string; autoPlay?: boolean; autoPip?: boolean; toBack?: boolean; x?: number; y?: number; width?: number; height?: number; }</code> |

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


### togglePip()

```typescript
togglePip() => Promise<void>
```

--------------------


### setFrame(...)

```typescript
setFrame(options: { x: number; y: number; width?: number; height?: number; }) => Promise<void>
```

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code>{ x: number; y: number; width?: number; height?: number; }</code> |

--------------------


### toggleMute()

```typescript
toggleMute() => Promise<void>
```

--------------------


### setQuality(...)

```typescript
setQuality(options: { quality: string; }) => Promise<void>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ quality: string; }</code> |

--------------------


### getQualities()

```typescript
getQualities() => Promise<{ qualities: string[]; }>
```

**Returns:** <code>Promise&lt;{ qualities: string[]; }&gt;</code>

--------------------

</docgen-api>
