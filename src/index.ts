// Reexport the native module. On web, it will be resolved to ExpoBackgroundRunnerModule.web.ts
// and on native platforms to ExpoBackgroundRunnerModule.ts
export { default } from './ExpoBackgroundRunnerModule';
export { default as ExpoBackgroundRunnerView } from './ExpoBackgroundRunnerView';
export * from  './ExpoBackgroundRunner.types';
