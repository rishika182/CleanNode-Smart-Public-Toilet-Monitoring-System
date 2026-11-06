import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { AlertTriangle, Check, ShieldCheck } from "lucide-react";
import { PageHeader, PciBadge, AlertBadge, EmptyState } from "@/components/cleannode/UiBits";
import { api, relativeTime, blockLabel } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter, DialogClose,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";

export default function Alerts() {
  const { last_sync } = useCleanNode();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => api.get("/alerts").then(({ data }) => { setItems(data.alerts || []); setLoading(false); }).catch(() => setLoading(false));

  useEffect(() => {
    let active = true;
    const run = () => { if (active) load(); };
    run();
    const t = setInterval(run, 8000);
    return () => { active = false; clearInterval(t); };
  }, [last_sync]);

  const critical = items.filter((i) => i.cubicle.alert === "Flush Issue" || i.cubicle.alert === "Needs Cleaning" || i.cubicle.PCI < 45);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Alerts"
        subtitle="Threshold-based alerts — acknowledge or resolve as Municipal Authority (FR3)"
        actions={
          <span className="inline-flex items-center gap-1.5 rounded-full bg-danger/10 px-3 py-1 text-sm font-medium text-danger">
            <AlertTriangle className="size-4" /> {critical.length} critical
          </span>
        }
      />
      {loading ? <EmptyState message="Loading alerts…" /> :
        items.length === 0 ? <EmptyState message="No active alerts. All facilities are within thresholds." /> : (
          <div className="panel divide-y divide-border" data-testid="alerts-list">
            {items.map((a) => (
              <div key={`${a.facility_id}-${a.cubicle.id}`} className="flex flex-wrap items-center gap-3 px-5 py-4">
                <span className={`grid size-9 place-items-center rounded-lg ${a.cubicle.PCI < 45 ? "bg-danger/15 text-danger" : "bg-warn/20 text-warn"}`}>
                  <AlertTriangle className="size-4" />
                </span>
                <div className="min-w-0 flex-1">
                  <Link to={`/facilities/${a.facility_id}`} className="font-medium text-foreground hover:text-primary">{a.facility_name}</Link>
                  <p className="text-xs text-muted-foreground">{blockLabel(a.cubicle.block)} · {a.cubicle.cubicle.replace("_", " ")} · {a.area_name} · {relativeTime(a.cubicle.last_updated)}</p>
                  {a.ack?.handled && <p className="mt-0.5 text-xs text-good">Acknowledged by {a.ack.by}{a.ack.note ? ` — "${a.ack.note}"` : ""}</p>}
                </div>
                <div className="text-right text-xs text-muted-foreground">
                  <p>Gas {a.cubicle.gas} ppm</p>
                  <p>Water {a.cubicle.water_status}</p>
                </div>
                <PciBadge value={a.cubicle.PCI} showLabel={false} />
                <AlertBadge alert={a.cubicle.alert} />
                {a.ack?.handled
                  ? <span className="inline-flex items-center gap-1 rounded-full bg-good/15 px-2.5 py-0.5 text-xs font-medium text-good"><ShieldCheck className="size-3.5" /> Handled</span>
                  : <AckDialog alert={a} onDone={load} />}
              </div>
            ))}
          </div>
        )}
    </div>
  );
}

function AckDialog({ alert, onDone }) {
  const [open, setOpen] = useState(false);
  const [note, setNote] = useState("");

  const act = async (resolved) => {
    try {
      await api.post("/alerts/ack", { facility_id: alert.facility_id, cubicle_id: alert.cubicle.id, note, resolved });
      toast.success(resolved ? "Alert resolved & dismissed" : "Alert acknowledged");
      setOpen(false); onDone();
    } catch { toast.error("Action failed"); }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline" className="h-8" data-testid={`ack-btn-${alert.facility_id}-${alert.cubicle.id}`}>
          <Check className="mr-1 size-3.5" /> Handle
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Handle alert · {alert.facility_name}</DialogTitle></DialogHeader>
        <div className="space-y-2">
          <p className="text-sm text-muted-foreground">{blockLabel(alert.cubicle.block)} · {alert.cubicle.cubicle.replace("_", " ")} — {alert.cubicle.alert} (PCI {alert.cubicle.PCI})</p>
          <Label htmlFor="ack-note">Action note</Label>
          <Textarea id="ack-note" value={note} onChange={(e) => setNote(e.target.value)} placeholder="e.g. Dispatched staff / cleaning scheduled" data-testid="ack-note" />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => act(false)} data-testid="ack-acknowledge">Acknowledge</Button>
          <Button onClick={() => act(true)} data-testid="ack-resolve">Resolve & dismiss</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
