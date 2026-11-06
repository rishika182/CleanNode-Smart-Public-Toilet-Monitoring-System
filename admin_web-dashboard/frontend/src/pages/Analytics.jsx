import { useEffect, useState } from "react";
import { TrendingUp, Clock, BarChart3 } from "lucide-react";
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend,
} from "recharts";
import { PageHeader, StatCard, EmptyState } from "@/components/cleannode/UiBits";
import { api } from "@/lib/cleannode";

export default function Analytics() {
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get("/analytics").then(({ data }) => setData(data)).catch(() => setData(false));
  }, []);

  if (data === null) return <EmptyState message="Loading usage analytics…" />;
  if (data === false) return <EmptyState message="Analytics unavailable." />;

  const totalUsage = data.per_facility.reduce((s, f) => s + f.usage, 0);
  const totalPredicted = data.per_facility.reduce((s, f) => s + f.predicted, 0);

  return (
    <div className="space-y-6">
      <PageHeader title="Usage Analytics" subtitle="Footfall forecasting & usage patterns for proactive scheduling (FR6)" />

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Peak hour" value={data.peak_hour?.hour ?? "—"} icon={Clock} tone="warn"
          hint={`${data.peak_hour?.usage ?? 0} est. visits`} />
        <StatCard label="Current usage" value={Math.round(totalUsage)} icon={TrendingUp} tone="info" />
        <StatCard label="Predicted usage" value={Math.round(totalPredicted)} icon={BarChart3} />
      </div>

      <div className="panel p-5">
        <h2 className="pb-3 text-sm font-semibold">Hourly usage pattern (last 3 days)</h2>
        <ResponsiveContainer width="100%" height={260}>
          <AreaChart data={data.hours} margin={{ top: 5, right: 10, left: -18, bottom: 0 }}>
            <defs>
              <linearGradient id="usageFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity={0.5} />
                <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity={0.02} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
            <XAxis dataKey="hour" tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))" }} minTickGap={20} />
            <YAxis tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} />
            <Tooltip contentStyle={{ borderRadius: 10, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
            <Area type="monotone" dataKey="usage" stroke="hsl(var(--primary))" strokeWidth={2} fill="url(#usageFill)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="panel p-5">
          <h2 className="pb-3 text-sm font-semibold">Average PCI by day</h2>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={data.days} margin={{ top: 5, right: 10, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="day" tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} />
              <Tooltip contentStyle={{ borderRadius: 10, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
              <Bar dataKey="avg_pci" fill="hsl(var(--chart-2))" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="panel p-5">
          <h2 className="pb-3 text-sm font-semibold">Usage vs predicted by facility</h2>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={data.per_facility} margin={{ top: 5, right: 10, left: -18, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
              <XAxis dataKey="facility" tick={{ fontSize: 9, fill: "hsl(var(--muted-foreground))" }} interval={0} angle={-12} height={40} />
              <YAxis tick={{ fontSize: 11, fill: "hsl(var(--muted-foreground))" }} />
              <Tooltip contentStyle={{ borderRadius: 10, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
              <Legend wrapperStyle={{ fontSize: 11 }} />
              <Bar dataKey="usage" fill="hsl(var(--primary))" radius={[6, 6, 0, 0]} />
              <Bar dataKey="predicted" fill="hsl(var(--chart-3))" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
