import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { api, formatApiError } from "@/lib/cleannode";
import { firebaseAuth } from "@/lib/firebase";
import { signInWithEmailAndPassword, signOut } from "firebase/auth";
import { onAuthStateChanged } from "@/lib/firebase";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // null = checking, false = logged out
  const [loading, setLoading] = useState(true);

  const bootstrap = useCallback(() => {
    const unsubscribe = onAuthStateChanged(firebaseAuth, async (firebaseUser) => {
      if (!firebaseUser) { localStorage.removeItem("cn_token"); setUser(false); setLoading(false); return; }
      try {
        const token = await firebaseUser.getIdToken();
        localStorage.setItem("cn_token", token);
        const { data } = await api.get("/auth/me");
        setUser(data);
      } catch {
        localStorage.removeItem("cn_token"); setUser(false);
      } finally { setLoading(false); }
    });
    return unsubscribe;
  }, []);

  useEffect(() => bootstrap(), [bootstrap]);

  const login = async (email, password) => {
    try {
      const credential = await signInWithEmailAndPassword(firebaseAuth, email, password);
      const token = await credential.user.getIdToken();
      localStorage.setItem("cn_token", token);
      const { data } = await api.get("/auth/me");
      setUser(data);
      return { ok: true };
    } catch (e) {
      localStorage.removeItem("cn_token");
      const firebaseMessages = {
        "auth/invalid-api-key": "Firebase configuration is invalid. Check the frontend Firebase API key.",
        "auth/invalid-credential": "Invalid email or password",
        "auth/wrong-password": "Invalid email or password",
        "auth/user-not-found": "Invalid email or password",
        "auth/invalid-email": "Please enter a valid email address",
        "auth/network-request-failed": "Unable to reach Firebase. Check your internet connection.",
        "auth/too-many-requests": "Too many sign-in attempts. Please try again later.",
      };
      return {
        ok: false,
        error: firebaseMessages[e?.code] || formatApiError(e.response?.data?.detail) || e?.message || "Unable to sign in. Please try again.",
      };
    }
  };

  const logout = async () => {
    localStorage.removeItem("cn_token");
    await signOut(firebaseAuth);
    setUser(false);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
