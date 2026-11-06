import axios from "axios";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

export const api = axios.create({ baseURL: API });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("cn_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export function formatApiError(detail) {
  if (detail == null) return "Something went wrong. Please try again.";
  if (typeof detail === "string") return detail;
  if (Array.isArray(detail))
    return detail.map((e) => (e && typeof e.msg === "string" ? e.msg : JSON.stringify(e))).join(" ");
  if (detail && typeof detail.msg === "string") return detail.msg;
  return String(detail);
}

// ---- PCI band helpers (SRS: Clean 75-100, Moderate 45-74, Dirty 0-44) ----
export function pciBand(pci, settings) {
  const clean = settings?.pci_clean_min ?? 75;
  const mod = settings?.pci_moderate_min ?? 45;
  if (pci >= clean) return "good";
  if (pci >= mod) return "moderate";
  return "poor";
}

export const pciLabel = { good: "Clean", moderate: "Moderate", poor: "Dirty" };

export function cubicleList(facility) {
  return Object.values(facility?.cubicles ?? {}).sort((a, b) => a.id.localeCompare(b.id));
}

export function facilityAvgPci(facility) {
  const list = cubicleList(facility);
  if (!list.length) return 0;
  return Math.round(list.reduce((s, c) => s + c.PCI, 0) / list.length);
}

export function facilityAlerts(facility) {
  return cubicleList(facility).filter((c) => c.alert && c.alert !== "None").length;
}

export function facilityOccupancy(facility) {
  return cubicleList(facility).filter((c) => c.occupancy).length;
}

export function allCubicles(facilities) {
  return Object.values(facilities ?? {})
    .sort((a, b) => a.id.localeCompare(b.id))
    .flatMap((facility) => cubicleList(facility).map((cubicle) => ({ facility, cubicle })));
}

export function cubicleLabel(c) {
  return `${c.block} · ${c.cubicle.replace("_", " ")}`;
}

export function blockLabel(block) {
  if (block === "Mens") return "Men's";
  if (block === "Womens") return "Women's";
  return block;
}

export function formatTime(ts) {
  if (!ts) return "—";
  return new Date(ts).toLocaleString("en-IN", {
    day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit",
  });
}

export function relativeTime(ts) {
  if (!ts) return "—";
  const diff = Math.max(0, Date.now() - ts);
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}
