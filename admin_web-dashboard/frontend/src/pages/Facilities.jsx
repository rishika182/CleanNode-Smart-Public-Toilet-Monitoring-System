import { Link } from "react-router-dom";
import { Building2, MapPin, AlertTriangle, DoorClosed } from "lucide-react";
import { PageHeader, PciBadge, PciBar, EmptyState } from "@/components/cleannode/UiBits";
import { useCleanNode } from "@/context/CleanNodeContext";
import { facilityAvgPci, facilityAlerts, facilityOccupancy, cubicleList } from "@/lib/cleannode";

export default function Facilities() {
  const { facilities, search } = useCleanNode();
  const q = search.trim().toLowerCase();
  let list = Object.values(facilities);
  if (q) {
    list = list.filter((f) =>
      f.name.toLowerCase().includes(q) ||
      f.area_name.toLowerCase().includes(q) ||
      Object.values(f.cubicles).some((c) => c.id.toLowerCase().includes(q)),
    );
  }
  list.sort((a, b) => facilityAvgPci(a) - facilityAvgPci(b));

  return (
    <div className="space-y-6">
      <PageHeader title="Facilities" subtitle={`${Object.keys(facilities).length} monitored BBMP facilities across Bengaluru`} />
      {list.length === 0 ? (
        <EmptyState message="No facilities match your search." />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3" data-testid="facility-grid">
          {list.map((f) => {
            const pci = facilityAvgPci(f);
            const cubs = cubicleList(f);
            return (
              <Link key={f.id} to={`/facilities/${f.id}`}
                className="panel group p-5 transition-transform hover:-translate-y-1"
                data-testid={`facility-card-${f.id}`}>
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <span className="grid size-10 place-items-center rounded-lg bg-primary/10 text-primary">
                      <Building2 className="size-5" />
                    </span>
                    <div className="min-w-0">
                      <p className="truncate font-semibold text-foreground group-hover:text-primary">{f.name}</p>
                      <p className="flex items-center gap-1 text-xs text-muted-foreground">
                        <MapPin className="size-3" /> {f.area_name} · {f.blocks.join(", ")}
                      </p>
                    </div>
                  </div>
                  <PciBadge value={pci} showLabel={false} />
                </div>
                <div className="mt-4"><PciBar value={pci} /></div>
                <div className="mt-4 grid grid-cols-3 gap-2 text-center">
                  <Stat icon={DoorClosed} label="Cubicles" value={cubs.length} />
                  <Stat icon={AlertTriangle} label="Alerts" value={facilityAlerts(f)} danger={facilityAlerts(f) > 0} />
                  <Stat label="Occupied" value={`${facilityOccupancy(f)}/${cubs.length}`} />
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}

function Stat({ icon: Icon, label, value, danger }) {
  return (
    <div className="rounded-lg bg-muted/60 py-2">
      <p className={`font-mono text-lg font-bold ${danger ? "text-danger" : "text-foreground"}`}>{value}</p>
      <p className="text-[11px] uppercase tracking-wide text-muted-foreground">{label}</p>
    </div>
  );
}
