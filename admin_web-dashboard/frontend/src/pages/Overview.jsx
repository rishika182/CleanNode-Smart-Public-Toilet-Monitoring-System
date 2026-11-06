import { Link } from "react-router-dom";
import {
  AlertTriangle, Building2, ClipboardList, DoorClosed, Sparkles, Users, Wrench,
} from "lucide-react";
import { AlertBadge, PageHeader, PciBadge, PciBar, StatCard } from "@/components/cleannode/UiBits";
import { useCleanNode } from "@/context/CleanNodeContext";
import { allCubicles, facilityAvgPci, pciBand, relativeTime, cubicleLabel } from "@/lib/cleannode";

export default function Overview() {
  const { facilities, assignments, settings } = useCleanNode();
  const facilityList = Object.values(facilities);
  const cubicles = allCubicles(facilities);

  const total = cubicles.length;
  const clean = settings?.pci_clean_min ?? 75;
  const mod = settings?.pci_moderate_min ?? 45;
  const healthy = cubicles.filter((c) => c.cubicle.PCI >= clean).length;
  const attention = cubicles.filter((c) => c.cubicle.PCI >= mod && c.cubicle.PCI < clean).length;
  const critical = cubicles.filter((c) => c.cubicle.PCI < mod || c.cubicle.alert === "Flush Issue").length;
  const avgPci = total ? Math.round(cubicles.reduce((s, c) => s + c.cubicle.PCI, 0) / total) : 0;
  const occupied = cubicles.filter((c) => c.cubicle.occupancy).length;
  const todaysUsage = Math.round(cubicles.reduce((s, c) => s + c.cubicle.ml.usage_forecast.current_usage, 0));
  const activeAssignments = Object.values(assignments).filter((a) => a.status !== "Completed");
  const highRisk = cubicles.filter((c) => c.cubicle.ml.predictive_maintenance.risk === "High");
  const mediumRisk = cubicles.filter((c) => c.cubicle.ml.predictive_maintenance.risk === "Medium");

  const lowestPci = [...facilityList]
    .map((f) => ({ facility: f, pci: facilityAvgPci(f) }))
    .sort((a, b) => a.pci - b.pci).slice(0, 4);

  const recentAlerts = cubicles
    .filter((c) => c.cubicle.alert !== "None")
    .sort((a, b) => b.cubicle.last_updated - a.cubicle.last_updated).slice(0, 6);

  return (
    <div className="space-y-6">
      <PageHeader title="Operations Overview" subtitle="Live sanitation health across all monitored BBMP facilities" />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <StatCard label="Total Facilities" value={facilityList.length} icon={Building2} testId="stat-facilities" />
        <StatCard label="Total Cubicles" value={total} icon={DoorClosed} testId="stat-cubicles" />
        <StatCard label="Clean / Healthy" value={healthy} icon={Sparkles} tone="good" testId="stat-clean" />
        <StatCard label="Needs Attention" value={attention} icon={Wrench} tone="warn" testId="stat-attention" />
        <StatCard label="Critical" value={critical} icon={AlertTriangle} tone="danger" testId="stat-critical" />
        <StatCard label="Active Duties" value={activeAssignments.length} icon={Users}
          hint={`${Object.keys(assignments).length} total duties`} testId="stat-duties" />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="panel p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Overall average PCI</p>
          <div className="mt-3 flex items-end gap-3">
            <span className="font-mono text-5xl font-bold text-foreground" data-testid="avg-pci">{avgPci}</span>
            <PciBadge value={avgPci} />
          </div>
          <div className="mt-4"><PciBar value={avgPci} /></div>
          <div className="mt-4 grid grid-cols-3 gap-2 text-center text-xs">
            <div className="rounded-lg bg-good/10 py-2 text-good">{clean}–100 Clean</div>
            <div className="rounded-lg bg-warn/15 py-2 text-warn">{mod}–{clean - 1} Moderate</div>
            <div className="rounded-lg bg-danger/10 py-2 text-danger">0–{mod - 1} Dirty</div>
          </div>
        </div>

        <div className="panel p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Current occupancy &amp; usage</p>
          <div className="mt-3 grid grid-cols-2 gap-3">
            <div className="rounded-lg bg-muted/60 p-3">
              <p className="text-xs text-muted-foreground">Occupied now</p>
              <p className="font-mono text-2xl font-bold text-foreground">{occupied}<span className="text-sm text-muted-foreground">/{total}</span></p>
            </div>
            <div className="rounded-lg bg-muted/60 p-3">
              <p className="text-xs text-muted-foreground">Est. current usage</p>
              <p className="font-mono text-2xl font-bold text-foreground">{todaysUsage}</p>
            </div>
          </div>
          <p className="mt-4 text-xs font-medium uppercase tracking-wide text-muted-foreground">Maintenance risk summary</p>
          <div className="mt-2 space-y-2 text-sm">
            <RiskRow label="High risk cubicles" value={highRisk.length} tone="danger" />
            <RiskRow label="Medium risk cubicles" value={mediumRisk.length} tone="warn" />
            <RiskRow label="Low risk cubicles" value={total - highRisk.length - mediumRisk.length} tone="good" />
          </div>
        </div>

        <div className="panel p-5">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Facilities with lowest PCI</p>
          <ul className="mt-3 space-y-3">
            {lowestPci.map(({ facility, pci }) => (
              <li key={facility.id}>
                <Link to={`/facilities/${facility.id}`} className="block rounded-lg p-2 transition-colors hover:bg-muted/70">
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-foreground">{facility.name}</p>
                      <p className="text-xs text-muted-foreground">{facility.area_name}</p>
                    </div>
                    <PciBadge value={pci} showLabel={false} />
                  </div>
                  <div className="mt-2"><PciBar value={pci} /></div>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel">
        <div className="flex items-center justify-between border-b border-border px-5 py-4">
          <div className="flex items-center gap-2">
            <ClipboardList className="size-4 text-muted-foreground" />
            <h2 className="text-sm font-semibold text-foreground">Recent alerts</h2>
          </div>
          <Link to="/alerts" className="text-xs font-medium text-primary hover:underline">View all alerts</Link>
        </div>
        {recentAlerts.length === 0 ? (
          <p className="px-5 py-10 text-center text-sm text-muted-foreground">No active alerts across the network.</p>
        ) : (
          <ul className="divide-y divide-border" data-testid="recent-alerts">
            {recentAlerts.map(({ facility, cubicle }) => (
              <li key={`${facility.id}-${cubicle.id}`} className="flex flex-wrap items-center gap-3 px-5 py-3">
                <span className={`size-2 rounded-full ${pciBand(cubicle.PCI, settings) === "poor" ? "bg-danger" : "bg-warn"}`} />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-foreground">{facility.name} · {cubicleLabel(cubicle)}</p>
                  <p className="text-xs text-muted-foreground">{facility.area_name} · updated {relativeTime(cubicle.last_updated)}</p>
                </div>
                <PciBadge value={cubicle.PCI} showLabel={false} />
                <AlertBadge alert={cubicle.alert} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

function RiskRow({ label, value, tone }) {
  const cls = { good: "bg-good", warn: "bg-warn", danger: "bg-danger" }[tone];
  return (
    <div className="flex items-center gap-2">
      <span className={`size-2 rounded-full ${cls}`} />
      <span className="flex-1 text-muted-foreground">{label}</span>
      <span className="font-mono font-semibold text-foreground">{value}</span>
    </div>
  );
}
