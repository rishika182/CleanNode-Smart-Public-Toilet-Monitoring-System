import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Save, SlidersHorizontal, ShieldAlert } from "lucide-react";
import { PageHeader, EmptyState } from "@/components/cleannode/UiBits";
import { api } from "@/lib/cleannode";
import { useAuth } from "@/context/AuthContext";
import { useCleanNode } from "@/context/CleanNodeContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const FIELDS = [
  ["pci_clean_min", "PCI · Clean threshold", "Score ≥ this is Clean (SRS: 75)"],
  ["pci_moderate_min", "PCI · Moderate threshold", "Score ≥ this is Moderate; below is Dirty (SRS: 45)"],
  ["gas_threshold", "Gas / ammonia alert (ppm)", "Bad Smell alert when gas exceeds this"],
  ["humidity_threshold", "Humidity alert (%)", "Anomaly when humidity exceeds this"],
  ["overcrowd_threshold", "Overcrowd occupancy", "Overcrowded alert above this count"],
  ["danger_zone_pci", "Danger-zone PCI", "Auto-dispatch staff below this PCI"],
  ["hours_since_cleaned_alert", "Hours-since-cleaned alert", "Flag cubicle when overdue for cleaning"],
  ["acceptance_sla_minutes", "Acceptance SLA (minutes)", "If staff don't accept an auto-dispatched duty within this window, it escalates to admin"],
];

export default function Settings() {
  const { user } = useAuth();
  const { refresh } = useCleanNode();
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const isAdmin = user?.role === "admin";

  useEffect(() => {
    api.get("/settings").then(({ data }) => setForm(data)).catch(() => setForm(false));
  }, []);

  if (form === null) return <EmptyState message="Loading settings…" />;
  if (form === false) return <EmptyState message="Settings unavailable." />;

  const save = async () => {
    setSaving(true);
    try {
      const payload = Object.fromEntries(Object.entries(form).map(([k, v]) => [k, Number(v)]));
      await api.put("/settings", payload);
      toast.success("Thresholds updated");
      refresh();
    } catch (e) {
      toast.error(e.response?.status === 403 ? "Admin access required" : "Could not save settings");
    } finally { setSaving(false); }
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Settings" subtitle="Cleanliness scoring bands & alert thresholds (FR1, FR3)" />

      {!isAdmin && (
        <div className="panel flex items-center gap-3 p-4 text-sm text-warn">
          <ShieldAlert className="size-5" /> You are signed in as staff. Thresholds are read-only.
        </div>
      )}

      <div className="panel max-w-3xl p-6">
        <div className="flex items-center gap-2 pb-4">
          <SlidersHorizontal className="size-4 text-muted-foreground" />
          <h2 className="text-sm font-semibold">Alert & scoring thresholds</h2>
        </div>
        <div className="grid gap-5 sm:grid-cols-2">
          {FIELDS.map(([key, label, hint]) => (
            <div key={key} className="space-y-1.5">
              <Label htmlFor={key}>{label}</Label>
              <Input id={key} type="number" value={form[key]} disabled={!isAdmin}
                onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                data-testid={`setting-${key}`} />
              <p className="text-xs text-muted-foreground">{hint}</p>
            </div>
          ))}
        </div>
        {isAdmin && (
          <div className="mt-6 flex justify-end">
            <Button onClick={save} disabled={saving} data-testid="save-settings">
              <Save className="mr-2 size-4" /> {saving ? "Saving…" : "Save thresholds"}
            </Button>
          </div>
        )}
      </div>

      <div className="panel max-w-3xl p-6">
        <h2 className="pb-3 text-sm font-semibold">Signed-in account</h2>
        <div className="grid gap-3 sm:grid-cols-2 text-sm">
          <div><span className="text-muted-foreground">Name: </span>{user?.name}</div>
          <div><span className="text-muted-foreground">Role: </span><span className="capitalize">{user?.role}</span></div>
          <div className="sm:col-span-2"><span className="text-muted-foreground">Email: </span>{user?.email}</div>
        </div>
      </div>
    </div>
  );
}
