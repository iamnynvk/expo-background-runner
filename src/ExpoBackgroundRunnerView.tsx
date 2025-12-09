import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoBackgroundRunnerViewProps } from './ExpoBackgroundRunner.types';

const NativeView: React.ComponentType<ExpoBackgroundRunnerViewProps> =
  requireNativeView('ExpoBackgroundRunner');

export default function ExpoBackgroundRunnerView(props: ExpoBackgroundRunnerViewProps) {
  return <NativeView {...props} />;
}
