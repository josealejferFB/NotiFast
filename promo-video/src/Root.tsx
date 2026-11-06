import React, { useEffect, useState } from "react";
import { Composition, cancelRender, continueRender, delayRender, staticFile } from "remotion";
import { NotiFastPromo } from "./Video";
import { CANVAS } from "./tokens";

const FONT_FACES = [
  { family: "Plus Jakarta Sans", weight: "400", file: "fonts/PlusJakartaSans-Regular.ttf" },
  { family: "Plus Jakarta Sans", weight: "500", file: "fonts/PlusJakartaSans-Medium.ttf" },
  { family: "Plus Jakarta Sans", weight: "600", file: "fonts/PlusJakartaSans-SemiBold.ttf" },
  { family: "Plus Jakarta Sans", weight: "700", file: "fonts/PlusJakartaSans-Bold.ttf" },
  { family: "Plus Jakarta Sans", weight: "800", file: "fonts/PlusJakartaSans-Bold.ttf" },
  { family: "JetBrains Mono", weight: "400", file: "fonts/JetBrainsMono-Regular.ttf" },
  { family: "JetBrains Mono", weight: "500", file: "fonts/JetBrainsMono-Medium.ttf" },
  { family: "JetBrains Mono", weight: "600", file: "fonts/JetBrainsMono-SemiBold.ttf" },
  { family: "JetBrains Mono", weight: "700", file: "fonts/JetBrainsMono-SemiBold.ttf" },
];

export const RemotionRoot: React.FC = () => {
  const [handle] = useState(() => delayRender("Cargando fuentes locales de NotiFast"));

  useEffect(() => {
    let cancelled = false;
    const loadFonts = async () => {
      try {
        const faces = FONT_FACES.map((font) => {
          const face = new FontFace(font.family, `url(${staticFile(font.file)})`, {
            weight: font.weight,
            style: "normal",
          });
          return face.load().then((loaded) => {
            if (!cancelled) {
              document.fonts.add(loaded);
            }
          });
        });
        await Promise.all(faces);
        await document.fonts.ready;
        if (!cancelled) {
          continueRender(handle);
        }
      } catch (error) {
        if (!cancelled) {
          cancelRender(error);
        }
      }
    };
    loadFonts();
    return () => {
      cancelled = true;
    };
  }, [handle]);

  return (
    <Composition
      id="NotiFastPromo"
      component={NotiFastPromo}
      durationInFrames={CANVAS.durationInFrames}
      fps={CANVAS.fps}
      width={CANVAS.width}
      height={CANVAS.height}
    />
  );
};
