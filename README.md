# expo-background-runner

Reliable background execution for Expo & React Native — foreground, background & kill-mode (Android)

## Introduction

**expo-background-runner** is a powerful Android-first background execution library for Expo and React Native applications.

It allows JavaScript tasks to continue running when your app is:

- Foreground
- Background
- Killed / Terminated (Android only)

The library is implemented as an Expo Native Module using:

- Foreground Services
- Headless JS
- Exact Alarms
- Notification-based lifecycle control

## Why expo-background-runner?

Most background libraries fail when:

- App is swiped away
- OEM battery restrictions apply
- Long-running jobs are required

**expo-background-runner** is designed to solve these issues reliably.

## Use Cases

- Background timers & polling
- Periodic API sync
- File upload / download
- Location / tracking logic
- Daily scheduled jobs
- Background notification updates
- Open app screen from notification (Deep Linking)

## Key Features

- ✅ Works with Expo, Expo CNG, React Native CLI
- ✅ Foreground + Background + Kill-mode execution (Android)
- ✅ Daily scheduling with exact alarms
- ✅ Foreground notification support
- ✅ Headless JS execution
- ✅ Deep linking from notification
- ✅ Auto-start & battery-optimization helpers

## ⚠️ Platform Warnings

**Android**

- Foreground Service requires notification
- Android 12+ → POST_NOTIFICATIONS
- Android 13+ → SCHEDULE_EXACT_ALARM
- OEMs like MIUI / Vivo / Oppo require manual Auto-Start enable

**iOS**

- ❌ Kill-mode execution NOT supported
- ❌ Long-running background JS not allowed
- ✅ Notification + Deep linking works only when app is alive

⚠️ This library is Android-first by design

## Installation

#### Expo (Managed / Dev Client)

```sh
expo install expo-background-runner
```

#### Expo CNG (Bare workflow)

```sh
npm install expo-background-runner
npx expo prebuild
```

#### React Native CLI

```sh
npm install expo-background-runner
```

## Android Configuration (MANDATORY)

#### Permissions (AndroidManifest.xml)

```sh
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

### Foreground Service Declaration

```sh
<service
  android:name="expo.modules.backgroundrunner.BackgroundNotificationController"
  android:foregroundServiceType="dataSync" />
```

### Deep Linking Setup

#### Android Intent Filter

```sh
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="yourSchemeHere" />
</intent-filter>
```

## Library API

#### start(callback, options)

Starts background execution.

```sh
BackgroundRunner.start(callback, options);
```

#### stop()

Stops all running background & scheduled tasks.

```sh
await BackgroundRunner.stop();
```

#### scheduleDaily(hour, minute, options)

Schedules a daily background task.

```sh
await BackgroundRunner.scheduleDaily(15, 30, options);
```

#### updateNotification(options)

Updates foreground notification dynamically.

```sh
await BackgroundRunner.updateNotification(options);
```

#### openAutoStartSettings()

Opens OEM auto-start / battery optimization screen.

```sh
BackgroundRunner.openAutoStartSettings();
```

### Options

| Property     | Type                                    | Description                                                                                                                                                                    |
| ------------ | --------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `taskName`   | `<string>`                              | Task name for identification.                                                                                                                                                  |
| `taskTitle`  | `<string>`                              | **Android Required**. Notification title.                                                                                                                                      |
| `taskDesc`   | `<string>`                              | **Android Required**. Notification description.                                                                                                                                |
| `taskIcon`   | [`<taskIconOptions>`](#taskIconOptions) | **Android Required**. Notification icon.                                                                                                                                       |
| `color`      | `<string>`                              | Notification color. **Default**: `"#ffffff"`.                                                                                                                                  |
| `linkingURI` | `<string>`                              | Link that will be called when the notification is clicked. Example: `"yourSchemeHere://chat/jane"`. See [Deep Linking](#deep-linking) for more info. **Default**: `undefined`. |
| `parameters` | `<any>`                                 | Parameters to pass to the task.                                                                                                                                                |

## Usage/Examples

```javascript
import BackgroundRunner from "expo-background-runner";

const sleep = (ms: number) => new Promise(r => setTimeout(r, ms));

const options = {
  taskName: "ExampleBackground",
  taskTitle: "Example Running",
  taskDesc: "Preparing...",
  color: "#ff0000",
  linkingURI: "yourSchemeHere://home",
  parameters: {
    delay: 1000,
    runningOn: "BACKGROUND_TASK",
  },
};

export async function executeTask(parameters: any, isHeadless: boolean) {
  for (let i = 0; i < 10; i++) {
    await BackgroundRunner.updateNotification({
      taskDesc: `Step ${i}`,
    });
    await sleep(1000);
  }

  if (!isHeadless) {
    await BackgroundRunner.stop();
  }
}

export default function Home() {
  const start = async () => {
    await BackgroundRunner.start(
      ({ parameters }) => executeTask(parameters, false),
      options
    );
  };

  return null;
}
```

### Scheduled Task Example

```javascript
await BackgroundRunner.scheduleDaily(15, 54, {
  taskName: "DailyTask",
  taskTitle: "Daily Task Running",
  taskDesc: "Executing scheduled task...",
  linkingURI: "yourSchemeHere://home",
  parameters: {
    delay: 1000,
    runningOn: "SCHEDULE_TASK",
  },
});
```

### Headless / Kill-Mode Setup (REQUIRED)

Note:

- Expo & Expo CNG need to create **index.js** file

Change the entry file routes in **Package.json** file

```json
  "name": "expopilot",
  "main": "index.js",
  "version": "1.0.0",
```

Create **index.js** file

```javascript
import "expo-router/entry";
import "./background";
```

Create **background.js** file

```javascript
import { AppRegistry } from "react-native";
import { executeTask } from "./src/app/(drawer)/(tabs)/assistant";

// your headless handler
async function myScheduledHandler({ parameters }) {
  console.log("HANDLESS TASK IN BACKGROUND FILE CODE : ", parameters);
  if (parameters?.runningOn === "SCHEDULE_TASK") {
    console.log("HANDLESS TASK IN _LAYOUT FILE CODE : ", parameters);
    executeTask(parameters, false);
  }
}

AppRegistry.registerHeadlessTask(
  "BackgroundRunnerTask",
  () => myScheduledHandler
);
```

If project build in React Native CLI, directly above code set in **index.js** or **App.tsx** file.

⚠️ Must be registered once at app entry

## Best Practices

- Always add delay inside loops
- Do not call `stop()` immediately in headless mode
- Enable auto-start on OEM devices
- Test on real Android devices

## Limitations

- iOS kill-mode not supported
- OEM battery restrictions apply
- Heavy CPU work not recommended

## License

MIT © iamnynvk (https://github.com/iamnynvk/)

## Support

- GitHub Issues
- Pull Requests welcome 🚀

## Credit

react-native-background-action
