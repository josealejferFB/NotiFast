import { Config } from "@remotion/cli/config";

Config.setVideoImageFormat("jpeg");
Config.setStillImageFormat("jpeg");
Config.setOverwriteOutput(true);
Config.setConcurrency(4);
Config.setChromiumOpenGlRenderer("angle");
