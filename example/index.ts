import { registerRootComponent } from "expo";
import App from "./App";
import { AppRegistry } from "react-native";
import { executeTask } from "./src/screens/home";

// your headless handler
async function myScheduledHandler({ parameters }: any) {
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

// registerRootComponent calls AppRegistry.registerComponent('main', () => App);
// It also ensures that whether you load the app in Expo Go or in a native build,
// the environment is set up appropriately
registerRootComponent(App);
