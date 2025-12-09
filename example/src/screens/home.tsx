import React from "react";
import { View, StyleSheet } from "react-native";
import Button from "../components/Button";
import BackgroundRunner from "../../../src/ExpoBackgroundRunnerModule";

const sleep = (ms: number) => new Promise((res) => setTimeout(res, ms));

const optionForBackground = {
  taskName: "ExampleBackground",
  taskTitle: "Example Running",
  taskDesc: "Preparing...",
  taskIcon: { name: "", type: "" },
  color: "#ff0000",
  linkingURI: "yourSchemeHere://home",
  parameters: { delay: 1000, runningOn: "BACKGROUND_TASK" },
};

const optionForSchedule = {
  taskName: "DailyTask",
  taskTitle: "Daily Task Running",
  taskDesc: "Executing scheduled task...",
  taskIcon: { name: "", type: "" },
  color: "#ff0000",
  linkingURI: "yourSchemeHere://home",
  parameters: {
    delay: 1000,
    data: { message: "Hello from schedule" },
    runningOn: "SCHEDULE_TASK",
  },
};

export async function executeTask(parameters: any, isHeadless: boolean) {
  console.log("🚀 Task Triggered!", parameters);

  for (let i = 0; i < 10; i++) {
    console.log("Running step", i);
    await BackgroundRunner.updateNotification({
      taskDesc: `Steps ${i}`,
      color: "#000000",
    });
    await sleep(1000);
  }

  await BackgroundRunner.updateNotification({
    taskDesc: "Task Finished!",
  });

  // ❗Headless mode me stop() immediately not recommended
  if (!isHeadless) {
    await BackgroundRunner.stop();
  }
}

const home = () => {
  const onRunningProcess = async ({ parameters }: any) => {
    return executeTask(parameters, false);
  };

  const startBackgroundTask = async () => {
    try {
      await BackgroundRunner.start(onRunningProcess, optionForBackground);
    } catch (e) {
      console.error("Start error", e);
    }
  };

  const stopBackgroundTask = async () => {
    try {
      await BackgroundRunner.stop();
    } catch (e) {
      console.error("Stop error", e);
    }
  };

  const startScheduleTask = async () => {
    try {
      const hour = 15; // 7 PM
      const minute = 54;

      await BackgroundRunner.scheduleDaily(hour, minute, optionForSchedule);

      alert("Scheduled successfully!");
    } catch (e) {
      console.error("Schedule error", e);
    }
  };

  const checkPermissions = async () => {
    try {
      await BackgroundRunner.openAutoStartSettings();
    } catch (e) {
      console.error("Auto start permission error", e);
    }
  };

  return (
    <View style={styles.container}>
      <Button title={"Start Background Task"} onPress={startBackgroundTask} />
      <Button title="Stop Background Task" onPress={stopBackgroundTask} />
      <Button title="Start Schedule Task" onPress={startScheduleTask} />
      <Button title="Check Permissions" onPress={checkPermissions} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    gap: 8,
  },
});

export default home;
