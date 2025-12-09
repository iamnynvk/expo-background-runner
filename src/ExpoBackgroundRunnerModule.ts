import { NativeModule, requireNativeModule } from "expo";
import { AppRegistry } from "react-native";
import {
  ExecuteEventPayload,
  ExpoBackgroundRunnerModuleEvents,
} from "./ExpoBackgroundRunner.types";

declare class ExpoBackgroundRunnerModule extends NativeModule<ExpoBackgroundRunnerModuleEvents> {
  startNative(options: any): Promise<void>;
  updateNotification(options: any): Promise<void>;
  stop(): Promise<void>;
  isRunning(): boolean;
  scheduleDaily(hour: number, minute: number, options: any): Promise<void>;
  openAutoStartSettings(): Promise<void>;

  addListener<EventName extends keyof ExpoBackgroundRunnerModuleEvents>(
    eventName: EventName,
    listener: ExpoBackgroundRunnerModuleEvents[EventName]
  ): import("expo-modules-core").EventSubscription;
  removeListeners(count: number): void;
}

const nativeModule = requireNativeModule<ExpoBackgroundRunnerModule>(
  "ExpoBackgroundRunner"
);

// INTERNAL STATE
let activeCallback: null | ((params: any) => Promise<void>) = null;
let defaultHandler: null | ((params: any) => Promise<void>) = null;

/**
 * When native fires "onExecute" we try active -> default -> warn.
 * This works when app process has JS runtime already running.
 */
nativeModule.addListener("onExecute", async (payload: ExecuteEventPayload) => {
  try {
    if (activeCallback) {
      await activeCallback(payload);
      return;
    }
    if (defaultHandler) {
      await defaultHandler(payload);
      return;
    }
    console.warn(
      "BackgroundRunner: onExecute fired but no JS handler registered",
      payload
    );
  } catch (err) {
    console.error("❌ BackgroundRunner handler crashed:", err);
  }
});

// ---- PUBLIC API ----
export default {
  async start(callback: any, options: any) {
    activeCallback = callback;
    return nativeModule.startNative(options);
  },

  async stop() {
    activeCallback = null;
    return nativeModule.stop();
  },

  async updateNotification(options: any) {
    return nativeModule.updateNotification(options);
  },

  isRunning() {
    return nativeModule.isRunning();
  },

  async scheduleDaily(hour: number, minute: number, options: any) {
    try {
      return nativeModule.scheduleDaily(hour, minute, options);
    } catch (error) {
      console.error("❌ BackgroundRunner scheduleDaily error:", error);
    }
  },

  async openAutoStartSettings() {
    return nativeModule.openAutoStartSettings();
  },

  /**
   * Register a fallback headless handler. IMPORTANT: call this at top-level
   * (e.g. in index.js or root module import), not inside a screen useEffect,
   * so it's available to headless JS when Android starts the process.
   */
  registerDefault(handler: any) {
    defaultHandler = handler;

    // Register headless task so RN knows how to run BackgroundRunnerTask when app
    // process is started by Android (headless).
    try {
      AppRegistry.registerHeadlessTask("BackgroundRunnerTask", () => {
        return async (params: any) => {
          if (defaultHandler) {
            try {
              await defaultHandler(params);
            } catch (e) {
              console.error("BackgroundRunner headless handler threw:", e);
            }
          } else {
            console.warn(
              "BackgroundRunner headless invoked but no default handler"
            );
          }
          // must return a resolved promise
          return Promise.resolve();
        };
      });
    } catch (e) {
      // registerHeadlessTask can only be called once per process for same key,
      // ignore if already registered.
      // Log for debugging
      console.warn(
        "BackgroundRunner: registerHeadlessTask error (ignored):",
        e
      );
    }
  },
};
