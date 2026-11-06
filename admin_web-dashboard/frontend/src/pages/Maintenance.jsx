import { Link } from "react-router-dom";
import { Wrench } from "lucide-react";
import { PageHeader, PciBadge, RiskBadge, EmptyState } from "@/components/cleannode/UiBits";
import { useCleanNode } from "@/context/CleanNodeContext";
import { allCubicles, relativeTime, cubicleLabel } from "@/lib/cleannode";

const ORDER = { High: 0, Medium: 1, Low: 2 };

export default function Maintenance() {
  const { facilities } = useCleanNode();
  const rows = allCubicles(facilities)
    .sort((a, b) =>
      (ORDER[a.cubicle.ml.predictive_maintenance.risk] - ORDER[b.cubicle.ml.predictive_maintenance.risk]) ||
      (b.cubicle.ml.predictive_maintenance.risk_score - a.cubicle.ml.predictive_maintenance.risk_score));

  const counts = { High: 0, Medium: 0, Low: 0 };
  rows.forEach((r) => { counts[r.cubicle.ml.predictive_maintenance.risk] += 1; });

  return (
    <div className="space-y-6">
      <PageHeader title="Predictive Maintenance" subtitle="ML-driven maintenance risk scoring per cubicle (FR6)" />

      <div className="grid gap-4 sm:grid-cols-3">
        {["High", "Medium", "Low"].map((k) => (
          <div key={k} className="panel flex items-center justify-between p-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-muted-foreground">{k} risk</p>
              <p className="font-mono text-3xl font-bold text-foreground">{counts[k]}</p>
            </div>
            <RiskBadge risk={k} />
          </div>
        ))}
      </div>

      {rows.length === 0 ? <EmptyState message="No cubicle data available." /> : (
        <div className="grid gap-4 lg:grid-cols-2" data-testid="maintenance-list">
          {rows.map(({ facility, cubicle }) => {
            const pm = cubicle.ml.predictive_maintenance;
            return (
              <div key={`${facility.id}-${cubicle.id}`} className="panel p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <span className="grid size-9 place-items-center rounded-lg bg-primary/10 text-primary"><Wrench className="size-4" /></span>
                    <div>
                      <Link to={`/facilities/${facility.id}`} className="font-medium text-foreground hover:text-primary">{facility.name}</Link>
                      <p className="text-xs text-muted-foreground">{cubicleLabel(cubicle)} · {relativeTime(pm.updated)}</p>
                    </div>
                  </div>
                  <RiskBadge risk={pm.risk} />
                </div>
                <div className="mt-4">
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>Risk score</span><span className="font-mono">{pm.risk_score}/100</span>
                  </div>
                  <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-muted">
                    <div className={`h-full rounded-full ${pm.risk === "High" ? "bg-danger" : pm.risk === "Medium" ? "bg-warn" : "bg-good"}`} style={{ width: `${pm.risk_score}%` }} />
                  </div>
                </div>
                <ul className="mt-4 space-y-1.5">
                  {pm.reasons.map((r, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-muted-foreground">
                      <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-primary" /> {r}
                    </li>
                  ))}
                </ul>
                <div className="mt-4 flex items-center justify-between border-t border-border pt-3">
                  <span className="text-xs text-muted-foreground">Current PCI</span>
                  <PciBadge value={cubicle.PCI} />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
