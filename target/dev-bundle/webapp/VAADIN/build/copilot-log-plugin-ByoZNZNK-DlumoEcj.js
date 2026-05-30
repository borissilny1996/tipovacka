import { bT as lc, cT as y, bd as ee, b_ as m$1, al as Ro, ay as Ut, a2 as Mu, aV as be, ci as pl, cR as wt, S as Je, n as Cu, bV as le, aE as Vl } from "./indexhtml-D1xuee7b.js";
import { b as b$1 } from "./state-BA6hnnsv-DRyppH94.js";
import { l } from "./base-panel-DwlRfMDt-DKRsjRcW.js";
import { o } from "./icons-B1bSjv1h-o7ZGzefn.js";
import "./commonjsHelpers-CUmg6egw.js";
const A = 'copilot-log-panel ul{list-style-type:none;margin:0;padding:0}copilot-log-panel ul li{align-items:start;display:flex;gap:var(--space-50);padding:var(--space-100) var(--space-50);position:relative}copilot-log-panel ul li:before{border-bottom:1px dashed var(--divider-primary-color);content:"";inset:auto 0 0 calc(var(--size-m) + var(--space-100));position:absolute}copilot-log-panel ul li span.icon{display:flex;flex-shrink:0;justify-content:center;width:var(--size-m)}copilot-log-panel ul li.information span.icon{color:var(--blue-color)}copilot-log-panel ul li.warning span.icon{color:var(--warning-color)}copilot-log-panel ul li.error span.icon{color:var(--error-color)}copilot-log-panel ul li .message{display:flex;flex-direction:column;flex-grow:1;overflow:hidden}copilot-log-panel ul li:not(.expanded) span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-log-panel ul li button svg{transition:transform .15s cubic-bezier(.2,0,0,1)}copilot-log-panel ul li button[aria-expanded=true] svg{transform:rotate(90deg)}copilot-log-panel ul li code{margin-top:var(--space-50)}copilot-log-panel ul li.expanded .secondary{margin-top:var(--space-100)}copilot-log-panel .secondary a{display:block;margin-bottom:var(--space-50)}', B = () => {
  const e = { hour: "numeric", minute: "numeric", second: "numeric", fractionalSecondDigits: 3 };
  let t;
  const a = navigator.language ?? "", s = a.indexOf("@"), o2 = s === -1 ? a : a.slice(0, s);
  try {
    t = new Intl.DateTimeFormat(Intl.getCanonicalLocales(o2), e);
  } catch (n) {
    console.error("Failed to create date time formatter for ", o2, n), t = new Intl.DateTimeFormat("en-US", e);
  }
  return t;
}, C = B();
var _ = Object.defineProperty, b = Object.getOwnPropertyDescriptor, u = (e, t, a, s) => {
  for (var o2 = s > 1 ? void 0 : s ? b(t, a) : t, n = e.length - 1, i; n >= 0; n--)
    (i = e[n]) && (o2 = (s ? i(t, a, o2) : i(o2)) || o2);
  return s && o2 && _(t, a, o2), o2;
};
class F {
  constructor() {
    this.showTimestamps = false, Je(this);
  }
  toggleShowTimestamps() {
    this.showTimestamps = !this.showTimestamps;
  }
}
const h = new F();
let d = class extends l {
  constructor() {
    super(...arguments), this.unreadErrors = false, this.messages = [], this.nextMessageId = 1, this.transitionDuration = 0, this.errorHandlersAdded = false;
  }
  connectedCallback() {
    if (super.connectedCallback(), this.onCommand("log", (e) => {
      this.handleLogEventData({ type: e.data.type, message: e.data.message });
    }), this.onEventBus("log", (e) => this.handleLogEvent(e)), this.onEventBus("update-log", (e) => this.updateLog(e.detail)), this.onEventBus("notification-shown", (e) => this.handleNotification(e)), this.onEventBus("clear-log", () => this.clear()), this.reaction(
      () => m$1.sectionPanelResizing,
      () => {
        this.requestUpdate();
      }
    ), this.transitionDuration = parseInt(
      window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"),
      10
    ), !this.errorHandlersAdded) {
      const e = (t) => {
        Cu(() => {
          le.attentionRequiredPanelTag = "copilot-log-panel";
        }), this.log(be.ERROR, t.message, !!t.internal, t.details, t.link);
      };
      Ro((t) => {
        e(t);
      }), Ut.forEach((t) => {
        e(t);
      }), Ut.length = 0, this.errorHandlersAdded = true;
    }
  }
  clear() {
    this.messages = [];
  }
  handleNotification(e) {
    this.log(e.detail.type, e.detail.message, true, e.detail.details, e.detail.link);
  }
  handleLogEvent(e) {
    this.handleLogEventData(e.detail);
  }
  handleLogEventData(e) {
    this.log(
      e.type,
      e.message,
      !!e.internal,
      e.details,
      e.link,
      Mu(e.expandedMessage),
      Mu(e.expandedDetails),
      e.id
    );
  }
  activate() {
    this.unreadErrors = false, this.updateComplete.then(() => {
      const e = this.renderRoot.querySelector(".message:last-child");
      e && e.scrollIntoView();
    });
  }
  render() {
    return ee`
      <style>
        ${A}
      </style>
      <ul>
        ${this.messages.map((e) => this.renderMessage(e))}
      </ul>
    `;
  }
  renderMessage(e) {
    let t, a;
    return e.type === be.ERROR ? (a = o.alertTriangle, t = "Error") : e.type === be.WARNING ? (a = o.warning, t = "Warning") : (a = o.info, t = "Info"), ee`
      <li
        class="${e.type} ${e.expanded ? "expanded" : ""} ${e.details || e.link ? "has-details" : ""}"
        data-id="${e.id}">
        <span aria-label="${t}" class="icon" title="${t}">${a}</span>
        <span class="message" @click=${() => this.toggleExpanded(e)}>
          <span class="timestamp" ?hidden=${!h.showTimestamps}>${N(e.timestamp)}</span>
          <span class="primary">
            ${e.expanded && e.expandedMessage ? e.expandedMessage : e.message}
          </span>
          ${e.expanded ? ee` <span class="secondary"> ${e.expandedDetails ?? e.details} </span>` : ee` <span class="secondary" ?hidden="${!e.details && !e.link}">
                ${Mu(e.details)}
                ${e.link ? ee` <a href="${e.link}" target="_blank">Learn more</a>` : ""}
              </span>`}
        </span>
        <!-- TODO: a11y, button needs aria-controls with unique ids -->
        <button
          aria-controls="content"
          aria-expanded="${e.expanded}"
          aria-label="Expand details"
          class="icon"
          @click=${() => this.toggleExpanded(e)}
          ?hidden=${!this.canBeExpanded(e)}>
          <span>${o.chevronRight}</span>
        </button>
      </li>
    `;
  }
  log(e, t, a, s, o2, n, i, E) {
    const T = this.nextMessageId;
    this.nextMessageId += 1, i || (i = t);
    const g = {
      id: T,
      type: e,
      message: t,
      details: s,
      link: o2,
      dontShowAgain: false,
      deleted: false,
      expanded: false,
      expandedMessage: n,
      expandedDetails: i,
      timestamp: /* @__PURE__ */ new Date(),
      internal: a,
      userId: E
    };
    for (this.messages.push(g); this.messages.length > d.MAX_LOG_ROWS; )
      this.messages.shift();
    return this.requestUpdate(), this.updateComplete.then(() => {
      const m = this.renderRoot.querySelector(".message:last-child");
      m ? (setTimeout(() => m.scrollIntoView({ behavior: "smooth" }), this.transitionDuration), this.unreadErrors = false) : e === be.ERROR && (this.unreadErrors = true);
    }), g;
  }
  updateLog(e) {
    let t = this.messages.find((a) => a.userId === e.id);
    t || (t = this.log(be.INFORMATION, "<Log message to update was not found>", false)), Object.assign(t, e), pl(t.expandedDetails) && (t.expandedDetails = Mu(t.expandedDetails)), this.requestUpdate();
  }
  updated() {
    const e = this.querySelector(".row:last-child");
    e && this.isTooLong(e.querySelector(".firstrowmessage")) && e.querySelector("button.expand")?.removeAttribute("hidden");
  }
  toggleExpanded(e) {
    this.canBeExpanded(e) && (e.expanded = !e.expanded, this.requestUpdate()), wt("use-log", { source: "toggleExpanded" });
  }
  canBeExpanded(e) {
    if (e.expandedMessage || e.expanded)
      return true;
    const t = this.querySelector(`[data\\-id="${e.id}"]`)?.querySelector(
      ".firstrowmessage"
    );
    return this.isTooLong(t);
  }
  isTooLong(e) {
    return e && e.offsetWidth < e.scrollWidth;
  }
};
d.MAX_LOG_ROWS = 1e3;
u([
  b$1()
], d.prototype, "unreadErrors", 2);
u([
  b$1()
], d.prototype, "messages", 2);
d = u([
  Vl("copilot-log-panel")
], d);
let x = class extends lc {
  createRenderRoot() {
    return this;
  }
  render() {
    return ee`
      <style>
        copilot-log-panel-actions {
          display: contents;
        }
      </style>
      <button
        aria-label="Clear log"
        class="icon"
        title="Clear log"
        @click=${() => {
      y.emit("clear-log", {});
    }}>
        <span>${o.trash}</span>
      </button>
      <button
        aria-label="Toggle timestamps"
        class="icon"
        title="Toggle timestamps"
        @click=${() => {
      h.toggleShowTimestamps();
    }}>
        <span class="${h.showTimestamps ? "on" : "off"}"> ${o.clock} </span>
      </button>
    `;
  }
};
x = u([
  Vl("copilot-log-panel-actions")
], x);
const $ = {
  header: "Log",
  expanded: true,
  panelOrder: 0,
  panel: "bottom",
  floating: false,
  tag: "copilot-log-panel",
  actionsTag: "copilot-log-panel-actions",
  individual: true
}, U = {
  init(e) {
    e.addPanel($);
  }
};
window.Vaadin.copilot.plugins.push(U);
le.addPanel($);
function N(e) {
  return C.format(e);
}
export {
  x as Actions,
  d as CopilotLogPanel
};
