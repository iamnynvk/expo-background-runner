import { registerWebModule, NativeModule } from 'expo';

import { ExpoBackgroundRunnerModuleEvents } from './ExpoBackgroundRunner.types';

class ExpoBackgroundRunnerModule extends NativeModule<ExpoBackgroundRunnerModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoBackgroundRunnerModule, 'ExpoBackgroundRunnerModule');
