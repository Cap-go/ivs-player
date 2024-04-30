// import { Camera } from '@capacitor/camera';
import { SplashScreen } from "@capacitor/splash-screen";
import { CapacitorIvsPlayer } from "@capgo/ivs-player";

window.customElements.define(
  "capacitor-welcome",
  class extends HTMLElement {
    constructor() {
      super();
      SplashScreen.hide();

      const root = this.attachShadow({ mode: "open" });

      root.innerHTML = `
    <style>
      :host {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
        display: block;
        width: 100%;
        height: 100%;
        background-color: transparent;
      }
      h1, h2, h3, h4, h5 {
        text-transform: uppercase;
      }
      .button {
        display: inline-block;
        padding: 10px;
        background-color: #73B5F6;
        color: #fff;
        font-size: 0.9em;
        border: 0;
        border-radius: 3px;
        text-decoration: none;
        cursor: pointer;
      }
      main {
        padding: 15px;
        background-color: white;
        display: flex;
        flex-direction: column;
        flex-grow: 1;
        height: 546px;
      }
      main hr { height: 1px; background-color: #eee; border: 0; }
      main h1 {
        font-size: 1.4em;
        text-transform: uppercase;
        letter-spacing: 1px;
      }
      main h2 {
        font-size: 1.1em;
      }
      main h3 {
        font-size: 0.9em;
      }
      main p {
        color: #333;
      }
      main pre {
        white-space: pre-line;
      }
      .controls {
        color: white;
        font-family: cursive;
        font-size: 50px;
        width: 100%;
        aspect-ratio: 16 / 9;
        border: -1px solid black;
        text-align: center;
        display: flex;
        align-items: center;
        justify-items: center;
        z-index: 99999999999;
        text-shadow: 2px 2px black;
      }
    </style>
    <div style="height: 100vh">
      <capacitor-welcome-titlebar>
        <h1>Capacitor</h1>
      </capacitor-welcome-titlebar>
      <div class="controls">
        Video Controls
      </div>
      <main>
        <p>
          Capacitor makes it easy to build powerful apps for the app stores, mobile web (Progressive Web Apps), and desktop, all
          with a single code base.
        </p>
        <h2>Getting Started</h2>
        <p>
          <button class="button" id="create-stream">Create Stream</button>
        </p>
        <p>
          <button class="button" id="start-stream">Start Stream</button>
        </p>
        <p>
          <button class="button" id="autostart-stream">AutoStart Stream</button>
        </p>
        <p>
          <button class="button" id="pause-stream">Pause Stream</button>
        </p>
        <p>
          <button class="button" id="delete-stream">Delete Stream</button>
        </p>
        <p>
          <button class="button" id="get-frame">Get frame</button>
        </p>
        <p>
          <button class="button" id="get-qualities">Get getQualities</button>
        </p>
        <p>
          <button class="button" id="toggle-pip">Toggle Pip</button>
        </p>
        <p>
          <img id="image" style="max-width: 100%">
        </p>
      </main>
    </div>
    `;
    }

    connectedCallback() {
      const self = this;
      CapacitorIvsPlayer.addListener("expandPip", (res) => {
        console.log("expandPip", res);
      });
      CapacitorIvsPlayer.addListener("closePip", (res) => {
        console.log("closePip", res);
      });

      self.shadowRoot
        .querySelector("#toggle-pip")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.getPip().then((res) => {
            console.log("getPip", res);
            CapacitorIvsPlayer.setPip({ pip: !res.pip });
          });
        });
      self.shadowRoot
        .querySelector("#autostart-stream")
        .addEventListener("click", async function (e) {
          const url =
            "https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8";
          CapacitorIvsPlayer.create({ url, autoPlay: true, toBack: true });
        });
      self.shadowRoot
        .querySelector("#start-stream")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.start();
        });
      self.shadowRoot
        .querySelector("#pause-stream")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.pause();
        });
      self.shadowRoot
        .querySelector("#create-stream")
        .addEventListener("click", async function (e) {
          const url =
            "https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8";
          CapacitorIvsPlayer.create({ url, autoPlay: false, toBack: true });
        });
      self.shadowRoot
        .querySelector("#delete-stream")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.delete();
        });
      self.shadowRoot
        .querySelector("#get-frame")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.getFrame().then((frame) => {
            console.log("frame", frame);
          });
        });
      self.shadowRoot
        .querySelector("#get-qualities")
        .addEventListener("click", async function (e) {
          CapacitorIvsPlayer.getQualities().then((qualities) => {
            console.log("qualities", qualities);
          });
        });
    }
  },
);

window.customElements.define(
  "capacitor-welcome-titlebar",
  class extends HTMLElement {
    constructor() {
      super();
      const root = this.attachShadow({ mode: "open" });
      root.innerHTML = `
    <style>
      :host {
        position: relative;
        display: block;
        padding: 15px 15px 15px 15px;
        text-align: center;
        background-color: #73B5F6;
      }
      ::slotted(h1) {
        margin: 0;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
        font-size: 0.9em;
        font-weight: 600;
        color: #fff;
      }
    </style>
    <slot></slot>
    `;
    }
  },
);
