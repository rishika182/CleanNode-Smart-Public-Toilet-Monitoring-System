import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  ArrowLeft, Thermometer, Droplet, Wind, Users2, Clock, Waves, Activity,
} from "lucide-react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import {
  PageHeader, PciBadge, PciBar, AlertBadge, RiskBadge, SeverityBadge, OccupancyBadge,
  Metric, EmptyState, StatusBadge, PriorityBadge,
} from "@/components/cleannode/UiBits";
import { useCleanNode } from "@/context/CleanNodeContext";
import { api, cubicleList, relativeTime, formatTime } from "@/lib/cleannode";

export default function FacilityDetail() {
  const { facilityId } = useParams();
  const { facilities } = useCleanNode();
  const facility = facilities[facilityId];
  const [history, setHistory] = useState([]);
  const [duties, setDuties] = useState([]);

  useEffect(() => {
    let active = true;
    api.get(`/facilities/${facilityId}`).then(({ data }) => {
      if (!active) return;
      setHistory(data.history || []);
      setDuties(data.assignments || []);
    }).catch(() => {});
    return () => { active = false; };
  }, [facilityId]);

  if (!facility) return <EmptyState message="Facility not found or still loading…" />;
  const cubs = cubicleList(facility);

  const chartData = [...history]
    .sort((a, b) => a.timestamp - b.timestamp)
    .slice(-40)
    .map((h) => ({ t: new Date(h.timestamp).toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" }), PCI: h.PCI, gas: h.gas }));

  return (
    <div className="space-y-6">
      <Link to="/facilities" className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-primary">
        <ArrowLeft className="size-4" /> Back to facilities
      </Link>
      <PageHeader title={facility.name} subtitle={`${facility.area_name} · ${facility.blocks.join(", ")} · ${cubs.length} cubicles`} />

      <div className="panel p-5">
        <div className="flex items-center gap-2 pb-3">
          <Activity className="size-4 text-muted-foreground" />
          <h2 className="text-sm font-semibold">PCI &amp; gas trend</h2>
        </div>
        {chartData.length === 0 ? (
          <p className="py-10 text-center text-sm text-muted-foreground">No historical samples yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={chartData} margin={{ top: 5, right: 10, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="t" tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} minTickGap={30} />
              <YAxis tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} />
              <Tooltip contentStyle={{ borderRadius: 10, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
              <Line type="monotone" dataKey="PCI" stroke="hsl(var(--primary))" strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="gas" stroke="hsl(var(--warn))" strokeWidth={1.5} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="grid gap-4 lg:grid-cols-2 xl:grid-cols-3">
        {cubs.map((c) => (
          <div key={c.id} className="panel p-5" data-testid={`cubicle-${c.id}`}>
            <div className="flex items-start justify-between gap-2">
              <div>
                <p className="font-semibold text-foreground">{c.block} · {c.cubicle.replace("_", " ")}</p>
                <p className="text-xs text-muted-foreground">Updated {relativeTime(c.last_updated)}</p>
              </div>
              <PciBadge value={c.PCI} />
            </div>
            <div className="mt-3"><PciBar value={c.PCI} /></div>
            <div className="mt-3 flex flex-wrap gap-2">
              <AlertBadge alert={c.alert} />
              <OccupancyBadge occupied={c.occupancy} />
              <span className="inline-flex items-center gap-1.5 rounded-full bg-muted px-2.5 py-0.5 text-xs text-muted-foreground">
                <Waves className="size-3" /> {c.water_status}
              </span>
            </div>
            <div className="mt-4 grid grid-cols-3 gap-2">
              <Metric label="Temp" value={c.temperature} unit="°C" />
              <Metric label="Humidity" value={c.humidity} unit="%" />
              <Metric label="Gas" value={c.gas} unit="ppm" />
              <Metric label="Occupancy" value={c.occupancy_count} />
              <Metric label="Since clean" value={c.hours_since_cleaned} unit="h" />
              <Metric label="Usage" value={c.ml.usage_forecast.current_usage} />
            </div>
            <div className="mt-4 space-y-2 border-t border-border pt-3">
              <div className="flex items-center justify-between">
                <span className="text-xs text-muted-foreground">Predictive maintenance</span>
                <RiskBadge risk={c.ml.predictive_maintenance.risk} />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs text-muted-foreground">Anomaly severity</span>
                <SeverityBadge severity={c.ml.multisensor_anomaly.severity} />
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="panel">
        <div className="border-b border-border px-5 py-4"><h2 className="text-sm font-semibold">Duty assignments for this facility</h2></div>
        {duties.length === 0 ? (
          <p className="px-5 py-8 text-center text-sm text-muted-foreground">No duty assignments recorded.</p>
        ) : (
          <ul className="divide-y divide-border">
            {duties.map((d) => (
              <li key={d.id} className="flex flex-wrap items-center gap-3 px-5 py-3">
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-foreground">{d.block_id} · {d.cubicle_id.replace("_", " ")} — {d.staff_name}</p>
                  <p className="text-xs text-muted-foreground">Assigned {formatTime(d.assigned_time)} · {d.assigned_by}</p>
                </div>
                <PriorityBadge priority={d.priority} />
                <StatusBadge status={d.status} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
