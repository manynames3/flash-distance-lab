# Flash Distance Lab

Flash Distance Lab is an interactive flash-photography lesson available as a static web app and a native Android app. Move the light, change its power, and see how the subject's exposure responds to inverse-square light falloff.

Live site: [https://flash-distance-lab.pages.dev](https://flash-distance-lab.pages.dev)

## About

The lab makes a practical lighting rule visible: when the distance from a point light to the subject doubles, the subject receives one quarter of the light. The simulator pairs a movable studio scene with exposure readouts, a graduated `-4` to `+4` stop meter, power presets, and matching-power guidance so learners can connect the math to a real lighting decision.

The same lesson is developed for two surfaces: the browser experience is deployed from GitHub through Cloudflare Pages, while the native Android client is maintained on the `android-app` branch and can be built as an installable APK.

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

## Technology stack

- Web: semantic HTML, vanilla JavaScript, and responsive CSS
- Web visual system: the supplied studio photograph combined with CSS-rendered light, stand, beam, distance line, and exposure meter states
- Android: Java, Android SDK, native Android `View` layouts, and custom `Canvas` rendering
- Android build: Gradle `9.3.1`, Android Gradle Plugin `8.12.0`, Java `17`, compile/target SDK `36`, minimum SDK `24`
- Source and deployment: GitHub for version control, Cloudflare Pages for the web app

The Android app has no runtime web-wrapper or third-party UI dependency; its lesson logic and controls are implemented with platform APIs.

## Android app

The native Android client lives in the `android/` directory on the `android-app` branch. It uses the same inverse-square model, scene asset, graduated stop meter, distance presets, and flash-power controls as the web simulator. The movable flash stand, fixed subject, 30-degree beam, and readouts are rendered natively for Android.

To build a debug APK:

```bash
cd android
./gradlew assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`. Android Studio can open the `android/` directory directly.
