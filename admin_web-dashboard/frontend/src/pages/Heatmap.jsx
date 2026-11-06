import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Map as MapIcon, MapPin } from "lucide-react";
import { PageHeader, PciBadge, EmptyState } from "@/components/cleannode/UiBits";
import { api, pciBand } from "@/lib/cleannode";
import { useCleanNode } from "@/context/CleanNodeContext";
import GoogleFacilityMap from "@/components/cleannode/GoogleFacilityMap";

const BAND_COLOR = { good: "hsl(var(--good))", moderate: "hsl(var(--warn))", poor: "hsl(var(--danger))" };

export default function Heatmap() {
  const { settings } = useCleanNode();
  const [areas, setAreas] = useState(null);
  const [mapFailed, setMapFailed] = useState(false);
  const [showMap, setShowMap] = useState(false);
  const hasKey = Boolean(process.env.REACT_APP_GOOGLE_MAPS_API_KEY);

  useEffect(() => {
    let active = true;
    const load = () => api.get("/heatmap").then(({ data }) => { if (active) setAreas(data.areas || []); }).catch(() => setAreas([]));
    load();
    const t = setInterval(load, 8000);
    return () => { active = false; clearInterval(t); };
  }, []);

  if (areas === null) return <EmptyState message="Rendering city heatmap…" />;

  const useGoogle = hasKey && showMap && !mapFailed;
  const allFacs = areas.flatMap((a) => a.facilities.map((f) => ({ ...f, area: a.name })));
  const lats = allFacs.map((f) => f.lat), lngs = allFacs.map((f) => f.lng);
  const minLat = Math.min(...lats), maxLat = Math.max(...lats);
  const minLng = Math.min(...lngs), maxLng = Math.max(...lngs);
  const pad = 0.01;
  const project = (lat, lng) => ({
    x: ((lng - (minLng - pad)) / ((maxLng + pad) - (minLng - pad))) * 100,
    y: (1 - (lat - (minLat - pad)) / ((maxLat + pad) - (minLat - pad))) * 100,
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="City Heatmap"
        subtitle="Real-time PCI heatmap of BBMP facilities across Bengaluru (FR11)"
        actions={
          <div className="flex items-center gap-3 text-xs">
            {[["good", "Clean"], ["moderate", "Moderate"], ["poor", "Dirty"]].map(([b, l]) => (
              <span key={b} className="inline-flex items-center gap-1.5">
                <span className="size-3 rounded-full" style={{ background: BAND_COLOR[b] }} /> {l}
              </span>
            ))}
          </div>
        }
      />

      <div className="panel relative overflow-hidden p-0">
        <div className="flex items-center justify-between gap-2 border-b border-border px-5 py-3">
          <div className="flex items-center gap-2">
            <MapIcon className="size-4 text-muted-foreground" />
            <h2 className="text-sm font-semibold">{useGoogle ? "Live Google Map" : "Facility PCI map"}</h2>
          </div>
          {hasKey && !showMap && (
            <button onClick={() => { setMapFailed(false); setShowMap(true); }}
              className="rounded-full bg-primary px-3 py-1 text-xs font-medium text-primary-foreground transition-colors hover:bg-primary/90"
              data-testid="load-google-map">
              Load live Google Map
            </button>
          )}
          {showMap && !mapFailed && (
            <button onClick={() => setShowMap(false)}
              className="rounded-full bg-muted px-3 py-1 text-xs font-medium text-muted-foreground hover:bg-muted/70">
              Show grid view
            </button>
          )}
        </div>
        {mapFailed && (
          <div className="border-b border-danger/30 bg-danger/5 px-5 py-2 text-xs text-danger" data-testid="map-failed-note">
            Google Maps rejected the API key (InvalidKeyMapError). Showing the built-in grid map instead. Enable “Maps JavaScript API” + billing on the key and allow this domain, then reload.
          </div>
        )}
        {useGoogle ? (
          <GoogleFacilityMap areas={areas} settings={settings} onError={() => setMapFailed(true)} />
        ) : (
          <div className="relative h-[420px] w-full bg-[radial-gradient(hsl(var(--border))_1px,transparent_1px)] [background-size:24px_24px]">
            {allFacs.map((f) => {
              const { x, y } = project(f.lat, f.lng);
              const band = pciBand(f.avg_pci, settings);
              return (
                <Link key={f.id} to={`/facilities/${f.id}`}
                  className="group absolute -translate-x-1/2 -translate-y-1/2"
                  style={{ left: `${x}%`, top: `${y}%` }} data-testid={`heatmap-marker-${f.id}`}>
                  <span className="absolute inset-0 -z-10 animate-ping rounded-full opacity-30" style={{ background: BAND_COLOR[band] }} />
                  <span className="grid size-10 place-items-center rounded-full border-2 border-white text-[10px] font-bold text-white shadow-lg transition-transform group-hover:scale-125"
                    style={{ background: BAND_COLOR[band] }}>{f.avg_pci}</span>
                  <div className="pointer-events-none absolute left-1/2 top-11 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded-md bg-foreground px-2 py-1 text-[11px] text-background group-hover:block">
                    {f.name} · {f.area}
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {areas.map((area) => (
          <div key={area.id} className="panel p-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <MapPin className="size-4 text-primary" />
                <h3 className="font-semibold text-foreground">{area.name}</h3>
              </div>
              <PciBadge value={Math.round(area.avg_pci)} />
            </div>
            <div className="mt-4 space-y-2">
              {area.facilities.map((f) => (
                <Link key={f.id} to={`/facilities/${f.id}`}
                  className="flex items-center gap-3 rounded-lg p-2 transition-colors hover:bg-muted/70">
                  <span className="size-3 shrink-0 rounded-full" style={{ background: BAND_COLOR[pciBand(f.avg_pci, settings)] }} />
                  <span className="min-w-0 flex-1 truncate text-sm text-foreground">{f.name}</span>
                  <span className="text-xs text-muted-foreground">{f.alerts} alerts</span>
                  <span className="font-mono text-sm font-semibold text-foreground">{f.avg_pci}</span>
                </Link>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
