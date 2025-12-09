import { NativeModule, requireNativeModule } from 'expo';

import { ExpoBackgroundRunnerModuleEvents } from './ExpoBackgroundRunner.types';

declare class ExpoBackgroundRunnerModule extends NativeModule<ExpoBackgroundRunnerModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoBackgroundRunnerModule>('ExpoBackgroundRunner');
