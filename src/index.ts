import { registerPlugin } from "@capacitor/core";

import type { CapacitorIvsPlayerPlugin } from "./definitions";

const CapacitorIvsPlayer = registerPlugin<CapacitorIvsPlayerPlugin>(
  "CapacitorIvsPlayer",
  {
    web: () => import("./web").then((m) => new m.CapacitorIvsPlayerWeb()),
  }
);

export * from "./definitions";
export { CapacitorIvsPlayer };
