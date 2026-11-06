import { cn } from "@/lib/utils";
import { pciBand, pciLabel } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4 pb-2 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-foreground" data-testid="page-title">{title}</h1>
        {subtitle ? <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p> : null}
      </div>
      {actions}
    </div>
  );
}

export function StatCard({ label, value, hint, icon: Icon, tone = "default", testId }) {
  const toneMap = {
    default: "bg-primary/10 text-primary",
    good: "bg-good/15 text-good",
    warn: "bg-warn/20 text-warn",
    danger: "bg-danger/15 text-danger",
    info: "bg-info/15 text-info",
  };
  return (
    <div className="panel flex items-start gap-4 p-4 transition-transform hover:-translate-y-0.5" data-testid={testId}>
      <span className={cn("grid size-10 shrink-0 place-items-center rounded-lg", toneMap[tone])}>
        {Icon ? <Icon className="size-5" /> : null}
      </span>
      <div className="min-w-0">
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
        <p className="mt-1 text-2xl font-bold leading-none text-foreground font-mono">{value}</p>
        {hint ? <p className="mt-1.5 truncate text-xs text-muted-foreground">{hint}</p> : null}
      </div>
    </div>
  );
}

const chip = "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium";

export function PciBadge({ value, showLabel = true }) {
  const { settings } = useCleanNode();
  const band = pciBand(value, settings);
  const cls = { good: "bg-good/15 text-good", moderate: "bg-warn/20 text-warn", poor: "bg-danger/15 text-danger" }[band];
  return (
    <span className={cn(chip, cls, "font-mono")}>
      PCI {value}{showLabel ? <span className="font-sans opacity-80">· {pciLabel[band]}</span> : null}
    </span>
  );
}

export function PciBar({ value }) {
  const { settings } = useCleanNode();
  const band = pciBand(value, settings);
  const cls = { good: "bg-good", moderate: "bg-warn", poor: "bg-danger" }[band];
  return (
    <div className="h-1.5 w-full overflow-hidden rounded-full bg-muted">
      <div className={cn("h-full rounded-full transition-all duration-500", cls)} style={{ width: `${value}%` }} />
    </div>
  );
}

export function AlertBadge({ alert }) {
  if (!alert || alert === "None")
    return <span className={cn(chip, "bg-muted text-muted-foreground")}>No alert</span>;
  const cls = alert === "Flush Issue" || alert === "Needs Cleaning" ? "bg-danger/15 text-danger" : "bg-warn/20 text-warn";
  return <span className={cn(chip, cls)}>{alert}</span>;
}

export function RiskBadge({ risk }) {
  const cls = { Low: "bg-good/15 text-good", Medium: "bg-warn/20 text-warn", High: "bg-danger/15 text-danger" }[risk] || "bg-muted text-muted-foreground";
  return <span className={cn(chip, cls)}>{risk} risk</span>;
}

export function SeverityBadge({ severity }) {
  const cls = {
    None: "bg-muted text-muted-foreground",
    Minor: "bg-info/15 text-info",
    Moderate: "bg-warn/20 text-warn",
    Critical: "bg-danger/15 text-danger",
  }[severity] || "bg-muted text-muted-foreground";
  return <span className={cn(chip, cls)}>{severity}</span>;
}

export function StatusBadge({ status }) {
  const cls = {
    Pending: "bg-muted text-muted-foreground",
    Dispatched: "bg-info/15 text-info",
    Accepted: "bg-accent text-accent-foreground",
    "In Progress": "bg-warn/20 text-warn",
    Completed: "bg-good/15 text-good",
  }[status] || "bg-muted text-muted-foreground";
  return <span className={cn(chip, cls)}>{status}</span>;
}

export function PriorityBadge({ priority }) {
  const cls = {
    "Danger Zone": "bg-danger text-danger-foreground",
    Critical: "bg-danger/15 text-danger",
    High: "bg-warn/20 text-warn",
    Medium: "bg-info/15 text-info",
    Low: "bg-good/15 text-good",
  }[priority] || "bg-muted text-muted-foreground";
  return <span className={cn(chip, cls)}>{priority}</span>;
}

export function OccupancyBadge({ occupied }) {
  return (
    <span className={cn(chip, occupied ? "bg-warn/20 text-warn" : "bg-good/15 text-good")}>
      {occupied ? "Occupied" : "Vacant"}
    </span>
  );
}

export function Metric({ label, value, unit }) {
  return (
    <div className="rounded-lg bg-muted/60 px-3 py-2">
      <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className="font-mono text-sm font-semibold text-foreground">
        {value}{unit ? <span className="ml-0.5 text-xs font-normal text-muted-foreground">{unit}</span> : null}
      </p>
    </div>
  );
}

export function EmptyState({ message }) {
  return (
    <div className="panel grid place-items-center px-6 py-16 text-center text-sm text-muted-foreground">{message}</div>
  );
}
