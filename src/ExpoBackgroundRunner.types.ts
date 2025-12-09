export type ExecuteEventPayload = {
  parameters: any;
};

export type ExpoBackgroundRunnerModuleEvents = {
  onExecute: (payload: ExecuteEventPayload) => void;
};

export type BackgroundRunnerTaskIcon = {
  name: string;
  type: string;
};

export type BackgroundRunnerOptions = {
  taskName?: string;
  taskTitle?: string;
  taskDesc?: string;
  taskIcon?: BackgroundRunnerTaskIcon;
  color?: string;
  linkingURI?: string;
  parameters?: any;
};
