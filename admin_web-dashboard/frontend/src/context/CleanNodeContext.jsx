import { createContext, useContext, useEffect, useRef, useState, useCallback } from "react";
import { api } from "@/lib/cleannode";

const CleanNodeContext = createContext(null);

const EMPTY = { facilities: {}, assignments: {}, history: [], areas: {}, settings: {}, source: "demo", last_sync: null };

export function CleanNodeProvider({ children }) {
  const [snapshot, setSnapshot] = useState(EMPTY);
  const [connected, setConnected] = useState(false);
  const [search, setSearch] = useState("");
  const [ready, setReady] = useState(false);
  const timer = useRef(null);

  const fetchSnapshot = useCallback(async () => {
    try {
      const { data } = await api.get("/snapshot");
      setSnapshot(data);
      setConnected(true);
    } catch {
      setConnected(false);
    } finally {
      setReady(true);
    }
  }, []);

  useEffect(() => {
    fetchSnapshot();
    timer.current = setInterval(fetchSnapshot, 8000);
    return () => clearInterval(timer.current);
  }, [fetchSnapshot]);

  return (
    <CleanNodeContext.Provider value={{ ...snapshot, connected, ready, search, setSearch, refresh: fetchSnapshot }}>
      {children}
    </CleanNodeContext.Provider>
  );
}

export function useCleanNode() {
  const ctx = useContext(CleanNodeContext);
  if (!ctx) throw new Error("useCleanNode must be used within CleanNodeProvider");
  return ctx;
}
