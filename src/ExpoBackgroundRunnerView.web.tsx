import * as React from 'react';

import { ExpoBackgroundRunnerViewProps } from './ExpoBackgroundRunner.types';

export default function ExpoBackgroundRunnerView(props: ExpoBackgroundRunnerViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
