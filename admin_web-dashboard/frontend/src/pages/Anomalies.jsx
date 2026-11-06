import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Radar } from "lucide-react";
import { PageHeader, PciBadge, SeverityBadge, EmptyState } from "@/components/cleannode/UiBits";
import { api, relativeTime, cubicleLabel } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";

export default function Anomalies() {
  const { last_sync } = useCleanNode();
  const [items, setItems] = useState(null);

  useEffect(() => {
    let active = true;
    const load = () => api.get("/anomalies").then(({ data }) => { if (active) setItems(data.anomalies || []); }).catch(() => setItems([]));
    load();
    const t = setInterval(load, 8000);
    return () => { active = false; clearInterval(t); };
  }, [last_sync]);

  if (items === null) return <EmptyState message="Running anomaly detection…" />;

  const counts = { Critical: 0, Moderate: 0, Minor: 0 };
  items.forEach((i) => { const s = i.cubicle.ml.multisensor_anomaly.severity; if (counts[s] !== undefined) counts[s] += 1; });

  return (
    <div className="space-y-6">
      <PageHeader title="Anomaly Detection" subtitle="Multi-sensor anomaly detection across temperature, humidity, gas, occupancy & flush" />

      <div className="grid gap-4 sm:grid-cols-3">
        {["Critical", "Moderate", "Minor"].map((k) => (
          <div key={k} className="panel flex items-center justify-between p-4">
            <div>
              <p className="text-xs uppercase tracking-wide text-muted-foreground">{k}</p>
              <p className="font-mono text-3xl font-bold text-foreground">{counts[k]}</p>
            </div>
            <SeverityBadge severity={k} />
          </div>
        ))}
      </div>

      {items.length === 0 ? <EmptyState message="No anomalies detected. All sensor readings are nominal." /> : (
        <div className="grid gap-4 lg:grid-cols-2" data-testid="anomaly-list">
          {items.map((a) => {
            const ma = a.cubicle.ml.multisensor_anomaly;
            return (
              <div key={`${a.facility_id}-${a.cubicle.id}`} className="panel p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <span className="grid size-9 place-items-center rounded-lg bg-primary/10 text-primary"><Radar className="size-4" /></span>
                    <div>
                      <Link to={`/facilities/${a.facility_id}`} className="font-medium text-foreground hover:text-primary">{a.facility_name}</Link>
                      <p className="text-xs text-muted-foreground">{cubicleLabel(a.cubicle)} · {relativeTime(ma.updated)}</p>
                    </div>
                  </div>
                  <SeverityBadge severity={ma.severity} />
                </div>
                <div className="mt-3">
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>Severity score</span><span className="font-mono">{ma.severity_score}/100</span>
                  </div>
                  <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-muted">
                    <div className="h-full rounded-full bg-danger" style={{ width: `${ma.severity_score}%` }} />
                  </div>
                </div>
                <ul className="mt-4 space-y-1.5">
                  {ma.anomalies.map((r, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-muted-foreground">
                      <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-danger" /> {r}
                    </li>
                  ))}
                </ul>
                <div className="mt-4 flex flex-wrap gap-1.5 border-t border-border pt-3">
                  {ma.sensors_checked.map((s) => (
                    <span key={s} className="rounded-full bg-muted px-2 py-0.5 text-[11px] text-muted-foreground">{s}</span>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
