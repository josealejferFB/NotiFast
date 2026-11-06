import React from "react";
import { AbsoluteFill } from "remotion";
import { TransitionSeries, linearTiming } from "@remotion/transitions";
import { fade } from "@remotion/transitions/fade";
import { S01Hook } from "./scenes/S01Hook";
import { S02DaceImportScreen } from "./scenes/S02DaceImportScreen";
import { S03ScheduleScreen } from "./scenes/S03ScheduleScreen";
import { S04NotificationsScreen } from "./scenes/S04NotificationsScreen";
import { S05NotesScreen } from "./scenes/S05NotesScreen";
import { S06TeachersScreen } from "./scenes/S06TeachersScreen";
import { S07CoversScreen } from "./scenes/S07CoversScreen";
import { S08GradesScreen } from "./scenes/S08GradesScreen";
import { S09Cta } from "./scenes/S09Cta";

const FADE = 12;
const transition = (
  <TransitionSeries.Transition
    presentation={fade()}
    timing={linearTiming({ durationInFrames: FADE })}
  />
);

export const NotiFastPromo: React.FC = () => {
  return (
    <AbsoluteFill style={{ backgroundColor: "#FBF2F4" }}>
      <TransitionSeries>
        <TransitionSeries.Sequence durationInFrames={87}>
          <S01Hook />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={222}>
          <S02DaceImportScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={252}>
          <S03ScheduleScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={237}>
          <S04NotificationsScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={252}>
          <S05NotesScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={222}>
          <S06TeachersScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={222}>
          <S07CoversScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={192}>
          <S08GradesScreen />
        </TransitionSeries.Sequence>
        {transition}
        <TransitionSeries.Sequence durationInFrames={150}>
          <S09Cta />
        </TransitionSeries.Sequence>
      </TransitionSeries>
    </AbsoluteFill>
  );
};
