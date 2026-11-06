import { Link } from "react-router-dom";
import { Activity } from "lucide-react";
import { PageHeader, PciBadge, AlertBadge, OccupancyBadge, EmptyState } from "@/components/cleannode/UiBits";
import { useCleanNode } from "@/context/CleanNodeContext";
import { allCubicles, relativeTime, blockLabel } from "@/lib/cleannode";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";

export default function LiveMonitor() {
  const { facilities, search, source } = useCleanNode();
  const q = search.trim().toLowerCase();
  let rows = allCubicles(facilities);
  if (q) {
    rows = rows.filter(({ facility, cubicle }) =>
      facility.name.toLowerCase().includes(q) ||
      facility.area_name.toLowerCase().includes(q) ||
      cubicle.id.toLowerCase().includes(q));
  }
  rows.sort((a, b) => a.cubicle.PCI - b.cubicle.PCI);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Live Monitor"
        subtitle="Real-time sensor readings from every cubicle (auto-refresh every 8s)"
        actions={
          <span className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
            <Activity className="size-3.5 animate-pulse" /> {source === "firebase" ? "Firebase live feed" : "Simulated feed"}
          </span>
        }
      />
      {rows.length === 0 ? (
        <EmptyState message="No cubicles match your search." />
      ) : (
        <div className="panel overflow-x-auto" data-testid="live-table">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Facility</TableHead>
                <TableHead>Block</TableHead>
                <TableHead>Cubicle ID</TableHead>
                <TableHead>PCI</TableHead>
                <TableHead className="text-right">Temp</TableHead>
                <TableHead className="text-right">Humidity</TableHead>
                <TableHead className="text-right">Gas</TableHead>
                <TableHead>Water</TableHead>
                <TableHead>Occupancy</TableHead>
                <TableHead>Alert</TableHead>
                <TableHead className="text-right">Updated</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map(({ facility, cubicle }) => (
                <TableRow key={`${facility.id}-${cubicle.id}`} className="hover:bg-muted/50">
                  <TableCell>
                    <Link to={`/facilities/${facility.id}`} className="font-medium text-foreground hover:text-primary">
                      {facility.name}
                    </Link>
                    <p className="text-xs text-muted-foreground">{facility.area_name}</p>
                  </TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${cubicle.block === "Womens" ? "bg-chart-5/15 text-chart-5" : "bg-info/15 text-info"}`}>
                      {blockLabel(cubicle.block)}
                    </span>
                  </TableCell>
                  <TableCell className="font-mono text-sm text-foreground">{cubicle.cubicle.replace("_", " ")}</TableCell>
                  <TableCell><PciBadge value={cubicle.PCI} showLabel={false} /></TableCell>
                  <TableCell className="text-right font-mono">{cubicle.temperature}°C</TableCell>
                  <TableCell className="text-right font-mono">{cubicle.humidity}%</TableCell>
                  <TableCell className="text-right font-mono">{cubicle.gas}</TableCell>
                  <TableCell className="text-sm">{cubicle.water_status}</TableCell>
                  <TableCell><OccupancyBadge occupied={cubicle.occupancy} /></TableCell>
                  <TableCell><AlertBadge alert={cubicle.alert} /></TableCell>
                  <TableCell className="text-right text-xs text-muted-foreground">{relativeTime(cubicle.last_updated)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}
