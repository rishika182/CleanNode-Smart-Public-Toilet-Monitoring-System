import { useEffect, useRef } from "react";
import { APIProvider, Map, useMap, useMapsLibrary } from "@vis.gl/react-google-maps";
import { MarkerClusterer } from "@googlemaps/markerclusterer";
import { pciBand, pciLabel } from "@/lib/cleannode";
const BAND_HEX = { good: "#2f9e6f", moderate: "#e0952a", poor: "#d94b34" };

function Markers({ facilities, settings }) {
  const map = useMap();
  const markerLib = useMapsLibrary("marker");
  const clusterer = useRef(null);

  useEffect(() => {
    if (!map || !markerLib) return;
    const infoWindow = new window.google.maps.InfoWindow();
    const markers = facilities.map((f) => {
      const band = pciBand(f.avg_pci, settings);
      const color = BAND_HEX[band];
      const el = document.createElement("div");
      el.style.cssText = `display:grid;place-items:center;width:38px;height:38px;border-radius:9999px;background:${color};color:#fff;font-weight:700;font-size:12px;border:2px solid #fff;box-shadow:0 2px 8px rgba(0,0,0,.35);font-family:'IBM Plex Mono',monospace;`;
      el.textContent = Math.round(f.avg_pci);
      const marker = new markerLib.AdvancedMarkerElement({
        position: { lat: f.lat, lng: f.lng },
        content: el,
        title: f.name,
        gmpClickable: true,
      });
      marker.addListener("click", () => {
        infoWindow.setContent(
          `<div style="font-family:'Plus Jakarta Sans',sans-serif;min-width:180px">
             <div style="font-weight:700;color:#1b2b36">${f.name}</div>
             <div style="font-size:12px;color:#5b6b76;margin:2px 0">${f.area}</div>
             <div style="font-size:13px">PCI <b>${f.avg_pci}</b> · ${pciLabel[band]}</div>
             <div style="font-size:12px;color:#5b6b76">${f.cubicles} cubicles · ${f.alerts} alerts</div>
           </div>`,
        );
        infoWindow.open(map, marker);
      });
      return marker;
    });
    clusterer.current = new MarkerClusterer({ map, markers });
    return () => {
      clusterer.current?.clearMarkers();
      markers.forEach((m) => { m.map = null; });
      infoWindow.close();
    };
  }, [map, markerLib, facilities, settings]);

  return null;
}

export default function GoogleFacilityMap({ areas, settings, onError }) {
  const key = process.env.REACT_APP_GOOGLE_MAPS_API_KEY;
  useEffect(() => {
    // Google calls this global on auth errors (InvalidKeyMapError, billing, referrer)
    window.gm_authFailure = () => { if (onError) onError(); };
    return () => { window.gm_authFailure = undefined; };
  }, [onError]);
  const facilities = areas.flatMap((a) => a.facilities.map((f) => ({ ...f, area: a.name })));
  if (!key || facilities.length === 0) return null;
  const center = {
    lat: facilities.reduce((s, f) => s + f.lat, 0) / facilities.length,
    lng: facilities.reduce((s, f) => s + f.lng, 0) / facilities.length,
  };
  return (
    <APIProvider apiKey={key} onError={onError}>
      <Map
        defaultCenter={center}
        defaultZoom={13}
        mapId="DEMO_MAP_ID"
        style={{ width: "100%", height: "440px" }}
        gestureHandling="greedy"
        mapTypeControl={false}
        streetViewControl={false}
        fullscreenControl={false}
        data-testid="google-map"
      >
        <Markers facilities={facilities} settings={settings} />
      </Map>
    </APIProvider>
  );
}
