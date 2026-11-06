import { useEffect, useMemo, useState } from "react";
import { History as HistoryIcon } from "lucide-react";
import { PageHeader, PciBadge, AlertBadge, EmptyState } from "@/components/cleannode/UiBits";
import { api, formatTime } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";

export default function History() {
  const { facilities } = useCleanNode();
  const [facilityId, setFacilityId] = useState("all");
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const params = facilityId === "all" ? {} : { facility_id: facilityId };
    api.get("/history", { params }).then(({ data }) => { setRows(data.history || []); }).finally(() => setLoading(false));
  }, [facilityId]);

  const facName = useMemo(() => (id) => facilities[id]?.name || id, [facilities]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Maintenance History Log"
        subtitle="Timestamped sensor, alert & flush records per cubicle — retained for audit (FR9)"
        actions={
          <Select value={facilityId} onValueChange={setFacilityId}>
            <SelectTrigger className="w-[220px]" data-testid="history-facility-filter"><SelectValue placeholder="Filter facility" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All facilities</SelectItem>
              {Object.values(facilities).map((f) => <SelectItem key={f.id} value={f.id}>{f.name}</SelectItem>)}
            </SelectContent>
          </Select>
        }
      />
      {loading ? <EmptyState message="Loading history…" /> :
        rows.length === 0 ? <EmptyState message="No historical records for this selection." /> : (
          <div className="panel overflow-x-auto" data-testid="history-table">
            <div className="flex items-center gap-2 border-b border-border px-5 py-3 text-sm text-muted-foreground">
              <HistoryIcon className="size-4" /> {rows.length} records
            </div>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Timestamp</TableHead>
                  <TableHead>Facility</TableHead>
                  <TableHead>Cubicle</TableHead>
                  <TableHead>PCI</TableHead>
                  <TableHead className="text-right">Gas</TableHead>
                  <TableHead className="text-right">Humidity</TableHead>
                  <TableHead className="text-right">Temp</TableHead>
                  <TableHead>Water</TableHead>
                  <TableHead>Alert</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((r, i) => (
                  <TableRow key={i} className="hover:bg-muted/50">
                    <TableCell className="text-xs text-muted-foreground">{formatTime(r.timestamp)}</TableCell>
                    <TableCell className="text-sm">{facName(r.facility_id)}</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{r.block} · {r.cubicle.replace("_", " ")}</TableCell>
                    <TableCell><PciBadge value={r.PCI} showLabel={false} /></TableCell>
                    <TableCell className="text-right font-mono text-sm">{r.gas}</TableCell>
                    <TableCell className="text-right font-mono text-sm">{r.humidity}%</TableCell>
                    <TableCell className="text-right font-mono text-sm">{r.temperature}°C</TableCell>
                    <TableCell className="text-sm">{r.water_status}</TableCell>
                    <TableCell><AlertBadge alert={r.alert} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
    </div>
  );
}
