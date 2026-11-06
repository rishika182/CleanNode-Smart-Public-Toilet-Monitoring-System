import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { Users, User, Plus, Trash2, Check, X, AlertTriangle } from "lucide-react";
import { PageHeader, StatusBadge, PriorityBadge, EmptyState, PciBadge } from "@/components/cleannode/UiBits";
import { api, formatTime, blockLabel, cubicleList } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter, DialogClose,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";

const STATUSES = ["Pending", "Dispatched", "Accepted", "In Progress", "Completed"];
const PRIORITIES = ["Danger Zone", "Critical", "High", "Medium", "Low"];

function VerifyBadge({ v }) {
  if (!v) return null;
  const cls = { Approved: "bg-good/15 text-good", Rejected: "bg-danger/15 text-danger", Pending: "bg-warn/20 text-warn" }[v];
  return <span className={cn("inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium", cls)}>{v}</span>;
}

export default function StaffDuties() {
  const { facilities, refresh } = useCleanNode();
  const [duties, setDuties] = useState([]);
  const [summary, setSummary] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      const [a, s] = await Promise.all([api.get("/assignments"), api.get("/staff")]);
      setDuties(a.data.assignments || []);
      setAccounts(a.data.staff || []);
      setSummary(s.data.staff_duties || []);
    } catch { /* ignore */ } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const patchDuty = async (id, body, msg) => {
    try {
      await api.patch(`/assignments/${id}`, body);
      toast.success(msg);
      load(); refresh();
    } catch { toast.error("Update failed"); }
  };

  const escalated = duties.filter((d) => d.escalated);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Staff & Duties"
        subtitle="Duties auto-dispatch on danger-zone. If staff don't accept within the SLA they escalate here for manual reassignment (FR7)."
        actions={<CreateDutyDialog facilities={facilities} staff={accounts} onCreated={() => { load(); refresh(); }} />}
      />

      {escalated.length > 0 && (
        <div className="panel border-l-4 border-l-danger p-5" data-testid="escalations">
          <div className="flex items-center gap-2 pb-3">
            <AlertTriangle className="size-4 text-danger" />
            <h2 className="text-sm font-semibold text-foreground">Needs your action — {escalated.length} auto-dispatched {escalated.length === 1 ? "duty" : "duties"} not accepted in time</h2>
          </div>
          <div className="space-y-2">
            {escalated.map((d) => (
              <div key={d.id} className="flex flex-wrap items-center gap-3 rounded-lg bg-danger/5 px-3 py-2.5" data-testid={`escalation-${d.id}`}>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-foreground">
                    {d.facility_id.replace("_BBMP_", " BBMP-").replace(/_/g, " ")} · {blockLabel(d.block_id)} · {d.cubicle_id.replace("_", " ")}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Auto-assigned to <b>{d.staff_name}</b> · not accepted for <b className="text-danger">{d.minutes_waiting} min</b>
                    {d.reassigned_by ? ` · last reassigned by ${d.reassigned_by}` : ""}
                  </p>
                </div>
                <PriorityBadge priority={d.priority} />
                <Select onValueChange={(v) => patchDuty(d.id, { staff_name: v }, `Reassigned to ${v}`)}>
                  <SelectTrigger className="h-8 w-[170px]" data-testid={`reassign-${d.id}`}>
                    <SelectValue placeholder="Reassign to…" />
                  </SelectTrigger>
                  <SelectContent>{accounts.map((s) => <SelectItem key={s.id} value={s.name}>{s.name}</SelectItem>)}</SelectContent>
                </Select>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="panel p-5 lg:col-span-2">
          <div className="flex items-center gap-2 pb-3"><Users className="size-4 text-muted-foreground" /><h2 className="text-sm font-semibold">Field staff workload</h2></div>
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {summary.length === 0 && <p className="text-sm text-muted-foreground">No duties assigned yet.</p>}
            {summary.map((s) => (
              <div key={s.name} className="flex items-center gap-3 rounded-lg bg-muted/50 p-3">
                <span className="grid size-9 place-items-center rounded-full bg-primary/10 text-primary"><User className="size-4" /></span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-foreground">{s.name}</p>
                  <p className="text-xs text-muted-foreground">{s.active} active · {s.completed} done</p>
                </div>
              </div>
            ))}
          </div>
        </div>
        <StaffAccountsCard accounts={accounts} onChange={load} />
      </div>

      {loading ? <EmptyState message="Loading duties…" /> :
        duties.length === 0 ? <EmptyState message="No duty assignments recorded." /> : (
          <div className="panel overflow-x-auto" data-testid="duties-table">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Duty</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead>Staff</TableHead>
                  <TableHead>Priority</TableHead>
                  <TableHead>Assigned</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Proof</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {duties.map((d) => (
                  <TableRow key={d.id} className="hover:bg-muted/50">
                    <TableCell className="font-mono text-xs">{d.id.slice(-6)}{d.manual && <span className="ml-1 rounded bg-info/15 px-1 text-[10px] text-info">manual</span>}{d.escalated && <span className="ml-1 rounded bg-danger/15 px-1 text-[10px] text-danger">overdue</span>}</TableCell>
                    <TableCell>
                      <p className="text-sm font-medium text-foreground">{d.facility_id.replace("_BBMP_", " BBMP-").replace(/_/g, " ")}</p>
                      <p className="text-xs text-muted-foreground">{blockLabel(d.block_id)} · {d.cubicle_id.replace("_", " ")}</p>
                    </TableCell>
                    <TableCell className="text-sm">{d.staff_name}</TableCell>
                    <TableCell><PriorityBadge priority={d.priority} /></TableCell>
                    <TableCell className="text-xs text-muted-foreground">{formatTime(d.assigned_time)}</TableCell>
                    <TableCell>
                      <Select value={d.status} onValueChange={(v) => patchDuty(d.id, { status: v }, `Duty → ${v}`)}>
                        <SelectTrigger className="h-8 w-[130px]" data-testid={`duty-status-${d.id}`}><SelectValue /></SelectTrigger>
                        <SelectContent>{STATUSES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent>
                      </Select>
                    </TableCell>
                    <TableCell><ProofCell duty={d} onVerify={(v) => patchDuty(d.id, { verification: v }, `Proof ${v.toLowerCase()}`)} /></TableCell>
                    <TableCell className="text-right">
                      {d.proof_photo && d.verification === "Pending" ? (
                        <div className="flex justify-end gap-1">
                          <Button size="sm" variant="outline" className="h-7 px-2 text-good" onClick={() => patchDuty(d.id, { verification: "Approved" }, "Proof approved")} data-testid={`approve-${d.id}`}><Check className="size-3.5" /></Button>
                          <Button size="sm" variant="outline" className="h-7 px-2 text-danger" onClick={() => patchDuty(d.id, { verification: "Rejected" }, "Proof rejected")} data-testid={`reject-${d.id}`}><X className="size-3.5" /></Button>
                        </div>
                      ) : <span className="text-xs text-muted-foreground">—</span>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
    </div>
  );
}

function ProofCell({ duty }) {
  if (!duty.proof_photo) return <span className="text-xs text-muted-foreground">No proof</span>;
  return (
    <Dialog>
      <DialogTrigger asChild>
        <button className="flex items-center gap-2" data-testid={`proof-${duty.id}`}>
          <img src={duty.proof_photo} alt="proof" className="size-9 rounded-md object-cover ring-1 ring-border" />
          <VerifyBadge v={duty.verification} />
        </button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Proof of cleaning · {duty.cubicle_id.replace("_", " ")}</DialogTitle></DialogHeader>
        <img src={duty.proof_photo} alt="proof" className="max-h-[420px] w-full rounded-lg object-cover" />
        <p className="text-sm text-muted-foreground">Submitted from the field for {duty.facility_id.replace(/_/g, " ")} · {blockLabel(duty.block_id)}. Status: <VerifyBadge v={duty.verification} /></p>
      </DialogContent>
    </Dialog>
  );
}

function StaffAccountsCard({ accounts, onChange }) {
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");

  const add = async () => {
    if (!name.trim()) return;
    try { await api.post("/staff", { name, phone }); toast.success("Staff added"); setName(""); setPhone(""); onChange(); }
    catch { toast.error("Could not add staff"); }
  };
  const remove = async (id) => {
    try { await api.delete(`/staff/${id}`); toast.success("Staff removed"); onChange(); }
    catch { toast.error("Could not remove"); }
  };

  return (
    <div className="panel p-5" data-testid="staff-accounts">
      <div className="flex items-center gap-2 pb-3"><User className="size-4 text-muted-foreground" /><h2 className="text-sm font-semibold">Manage field staff</h2></div>
      <div className="space-y-2">
        {accounts.map((s) => (
          <div key={s.id} className="flex items-center gap-2 rounded-lg bg-muted/50 px-3 py-2">
            <span className="flex-1 text-sm text-foreground">{s.name}{s.phone ? <span className="ml-1 text-xs text-muted-foreground">· {s.phone}</span> : null}</span>
            <button onClick={() => remove(s.id)} className="text-muted-foreground hover:text-danger" data-testid={`remove-staff-${s.id}`}><Trash2 className="size-4" /></button>
          </div>
        ))}
      </div>
      <div className="mt-3 flex gap-2">
        <Input placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} className="h-9" data-testid="new-staff-name" />
        <Input placeholder="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} className="h-9 w-24" />
        <Button size="sm" onClick={add} className="h-9" data-testid="add-staff-btn"><Plus className="size-4" /></Button>
      </div>
    </div>
  );
}

function CreateDutyDialog({ facilities, staff, onCreated }) {
  const [open, setOpen] = useState(false);
  const [facilityId, setFacilityId] = useState("");
  const [cubKey, setCubKey] = useState("");
  const [staffName, setStaffName] = useState("");
  const [priority, setPriority] = useState("High");
  const [remarks, setRemarks] = useState("");
  const facility = facilities[facilityId];
  const cubs = facility ? cubicleList(facility) : [];

  const submit = async () => {
    if (!facilityId || !cubKey || !staffName) { toast.error("Fill facility, cubicle and staff"); return; }
    const cub = facility.cubicles[cubKey];
    try {
      await api.post("/assignments", {
        facility_id: facilityId, block_id: cub.block, cubicle_id: cub.cubicle,
        staff_name: staffName, priority, remarks,
      });
      toast.success("Duty dispatched");
      setOpen(false); setFacilityId(""); setCubKey(""); setStaffName(""); setRemarks("");
      onCreated();
    } catch { toast.error("Could not create duty"); }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button data-testid="create-duty-btn"><Plus className="mr-2 size-4" /> Manual assign</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Dispatch cleaning duty</DialogTitle></DialogHeader>
        <div className="space-y-3">
          <div className="space-y-1.5">
            <Label>Facility</Label>
            <Select value={facilityId} onValueChange={(v) => { setFacilityId(v); setCubKey(""); }}>
              <SelectTrigger data-testid="duty-facility"><SelectValue placeholder="Select facility" /></SelectTrigger>
              <SelectContent>{Object.values(facilities).map((f) => <SelectItem key={f.id} value={f.id}>{f.name}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label>Cubicle</Label>
            <Select value={cubKey} onValueChange={setCubKey} disabled={!facility}>
              <SelectTrigger data-testid="duty-cubicle"><SelectValue placeholder="Select cubicle" /></SelectTrigger>
              <SelectContent>{cubs.map((c) => <SelectItem key={c.id} value={c.id}>{blockLabel(c.block)} · {c.cubicle.replace("_", " ")} (PCI {c.PCI})</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>Staff</Label>
              <Select value={staffName} onValueChange={setStaffName}>
                <SelectTrigger data-testid="duty-staff"><SelectValue placeholder="Assign to" /></SelectTrigger>
                <SelectContent>{staff.map((s) => <SelectItem key={s.id} value={s.name}>{s.name}</SelectItem>)}</SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Priority</Label>
              <Select value={priority} onValueChange={setPriority}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>{PRIORITIES.map((p) => <SelectItem key={p} value={p}>{p}</SelectItem>)}</SelectContent>
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label>Remarks</Label>
            <Textarea value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Optional instructions" />
          </div>
        </div>
        <DialogFooter>
          <DialogClose asChild><Button variant="outline">Cancel</Button></DialogClose>
          <Button onClick={submit} data-testid="duty-submit">Dispatch</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
