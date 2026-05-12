# Flash Distance Lab

Flash Distance Lab is an interactive education site for learning how flash exposure changes as the light moves closer to or farther from a portrait subject.

Live site: [https://flash-distance-lab.pages.dev](https://flash-distance-lab.pages.dev)

The simulator is built around the inverse-square law:

```text
subject light = flash power / distance^2
```

That means distance changes are dramatic. Moving a flash from 4 ft to 8 ft does not make the light half as strong; it makes the subject receive one quarter as much light, or two stops less exposure. To hold the same exposure after doubling distance, flash power has to increase by four times.

## What it teaches

- How flash-to-subject distance changes exposure
- Why doubling distance costs two stops of light
- How flash power fractions relate to stop changes
- How to compensate distance changes by adjusting power
- How a light meter-style stop scale maps brightness changes from `-4` to `+4`

## How to use it

Move the distance slider to reposition the strobe relative to the subject. The visual scene updates with the light and stand moving across the studio, while the readouts show the exact exposure change.

Change the flash power slider or power buttons to see how adding or subtracting power offsets the distance change. For example:

| Setup | Result |
| --- | --- |
| 4 ft at 1/16 power | Reference exposure |
| 8 ft at 1/16 power | 1/4 light, -2 stops |
| 8 ft at 1/4 power | Same exposure as the 4 ft reference |
| 2 ft at 1/16 power | 4x light, +2 stops |

## Why it matters

Flash exposure is controlled by distance and power together. Once you understand that relationship, lighting choices become more predictable: move the light for softness and shape, then adjust power to restore the exposure you want.

## Cloudflare Pages

Use Cloudflare Pages Git integration with these settings:

- Production branch: `main`
- Framework preset: `None`
- Build command: `exit 0`
- Build output directory: `.`

Cloudflare Pages will deploy the static files from the repository root and rebuild automatically on pushes to `main`.
