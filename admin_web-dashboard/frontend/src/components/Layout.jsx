import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
  Activity, AlertTriangle, BarChart3, Building2, History, LayoutDashboard,
  Radar, Settings, Users, Wrench, Droplets, Search, Cloud, CloudOff,
  ShieldCheck, LogOut, Map, Menu, X,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useCleanNode } from "@/context/CleanNodeContext";
import { useAuth } from "@/context/AuthContext";
import { relativeTime } from "@/lib/cleannode";
import { Input } from "@/components/ui/input";

const GROUPS = [
  {
    label: "Monitoring",
    items: [
      { title: "Overview", to: "/", icon: LayoutDashboard, end: true },
      { title: "Facilities", to: "/facilities", icon: Building2 },
      { title: "Live Monitor", to: "/live", icon: Activity },
      { title: "Alerts", to: "/alerts", icon: AlertTriangle },
      { title: "City Heatmap", to: "/heatmap", icon: Map },
    ],
  },
  {
    label: "AI / ML Intelligence",
    items: [
      { title: "Predictive Maintenance", to: "/maintenance", icon: Wrench },
      { title: "Usage Analytics", to: "/analytics", icon: BarChart3 },
      { title: "Anomaly Detection", to: "/anomalies", icon: Radar },
    ],
  },
  {
    label: "Operations",
    items: [
      { title: "Staff & Duties", to: "/staff", icon: Users },
      { title: "History", to: "/history", icon: History },
      { title: "Settings", to: "/settings", icon: Settings, adminOnly: true },
    ],
  },
];

function SidebarInner({ role, onNavigate }) {
  return (
    <div className="flex h-full flex-col bg-sidebar text-sidebar-foreground">
      <div className="flex items-center gap-2.5 border-b border-sidebar-border px-4 py-4">
        <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
          <Droplets className="size-5" />
        </span>
        <div className="min-w-0">
          <p className="truncate text-sm font-bold">CleanNode</p>
          <p className="truncate text-[10px] font-semibold tracking-widest text-sidebar-foreground/60">
            BBMP SMART SANITATION
          </p>
        </div>
      </div>
      <nav className="flex-1 space-y-5 overflow-y-auto px-3 py-4">
        {GROUPS.map((group) => (
          <div key={group.label}>
            <p className="px-2 pb-1.5 text-[10px] font-semibold uppercase tracking-widest text-sidebar-foreground/45">
              {group.label}
            </p>
            <div className="space-y-0.5">
              {group.items
                .filter((it) => !it.adminOnly || role === "admin")
                .map((it) => (
                  <NavLink
                    key={it.to}
                    to={it.to}
                    end={it.end}
                    onClick={onNavigate}
                    data-testid={`nav-${it.to === "/" ? "overview" : it.to.slice(1)}`}
                    className={({ isActive }) =>
                      cn(
                        "flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm transition-colors",
                        isActive
                          ? "bg-sidebar-primary/20 font-semibold text-white"
                          : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-white",
                      )
                    }
                  >
                    <it.icon className="size-4 shrink-0" />
                    <span className="truncate">{it.title}</span>
                  </NavLink>
                ))}
            </div>
          </div>
        ))}
      </nav>
    </div>
  );
}

export default function Layout({ children }) {
  const { connected, source, last_sync, search, setSearch } = useCleanNode();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const isLive = source === "firebase";

  return (
    <div className="flex min-h-screen w-full bg-background">
      {/* Desktop sidebar */}
      <aside className="hidden w-64 shrink-0 md:block">
        <div className="fixed inset-y-0 w-64">
          <SidebarInner role={user?.role} />
        </div>
      </aside>

      {/* Mobile drawer */}
      {open && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div className="absolute inset-0 bg-black/50" onClick={() => setOpen(false)} />
          <div className="absolute inset-y-0 left-0 w-64">
            <SidebarInner role={user?.role} onNavigate={() => setOpen(false)} />
          </div>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-border bg-surface/90 px-3 backdrop-blur md:px-6">
          <button
            className="grid size-9 place-items-center rounded-lg hover:bg-muted md:hidden"
            onClick={() => setOpen(true)}
            data-testid="mobile-menu-btn"
          >
            <Menu className="size-5" />
          </button>
          <div className="hidden md:block">
            <p className="text-sm font-semibold leading-tight text-foreground">
              CleanNode <span className="text-muted-foreground">· Smart Public Toilet Monitoring</span>
            </p>
            <p className="text-[11px] text-muted-foreground">
              Bruhat Bengaluru Mahanagara Palike — Sanitation Control Centre
            </p>
          </div>

          <div className="relative ml-auto w-full max-w-xs">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onFocus={() => navigate("/facilities")}
              placeholder="Search facility, cubicle or area"
              className="h-9 bg-card pl-8"
              data-testid="global-search"
            />
          </div>

          <div className="hidden items-center gap-2 lg:flex">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-good/15 px-2.5 py-1 text-xs font-medium text-good">
              <ShieldCheck className="size-3.5" /> System Online
            </span>
            <span
              className={cn(
                "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium",
                connected ? "bg-info/15 text-info" : "bg-danger/15 text-danger",
              )}
              data-testid="data-source-badge"
            >
              {connected ? <Cloud className="size-3.5" /> : <CloudOff className="size-3.5" />}
              {isLive ? "Firebase Live" : "Demo Mode"}
              <span className="opacity-70">· {relativeTime(last_sync)}</span>
            </span>
          </div>

          <div className="flex items-center gap-2 rounded-full border border-border bg-card py-1 pl-3 pr-1">
            <div className="hidden leading-tight sm:block">
              <p className="text-xs font-semibold text-foreground" data-testid="user-name">{user?.name}</p>
              <p className="text-[10px] capitalize text-muted-foreground">{user?.role}</p>
            </div>
            <button
              onClick={() => { logout(); navigate("/login"); }}
              className="grid size-7 place-items-center rounded-full bg-danger/10 text-danger transition-colors hover:bg-danger/20"
              title="Log out"
              data-testid="logout-btn"
            >
              <LogOut className="size-4" />
            </button>
          </div>
        </header>

        <main className="flex-1 space-y-6 p-4 md:p-6">{children}</main>
      </div>
    </div>
  );
}
