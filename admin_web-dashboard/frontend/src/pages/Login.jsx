import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Droplets, Loader2, ShieldCheck } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("admin@cleannode.com");
  const [password, setPassword] = useState("Admin@1234");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    const res = await login(email, password);
    setLoading(false);
    if (res.ok) navigate("/");
    else setError(res.error);
  };

  return (
    <div className="grid min-h-screen w-full lg:grid-cols-2">
      {/* Brand panel */}
      <div className="relative hidden flex-col justify-between overflow-hidden bg-sidebar p-10 text-sidebar-foreground lg:flex">
        <div className="pointer-events-none absolute -right-24 -top-24 size-96 rounded-full bg-sidebar-primary/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-24 -left-16 size-80 rounded-full bg-primary/20 blur-3xl" />
        <div className="relative flex items-center gap-3">
          <span className="grid size-11 place-items-center rounded-xl bg-sidebar-primary text-sidebar-primary-foreground">
            <Droplets className="size-6" />
          </span>
          <div>
            <p className="text-lg font-bold text-white">CleanNode</p>
            <p className="text-[11px] font-semibold tracking-widest text-sidebar-foreground/60">
              BBMP SMART SANITATION
            </p>
          </div>
        </div>
        <div className="relative max-w-md">
          <h1 className="text-4xl font-bold leading-tight text-white">
            IoT-driven public toilet monitoring for Bengaluru.
          </h1>
          <p className="mt-4 text-sm text-sidebar-foreground/70">
            Live cleanliness scoring, threshold alerts, predictive maintenance and
            danger-zone dispatch across every monitored BBMP facility.
          </p>
        </div>
        <div className="relative flex items-center gap-2 text-xs text-sidebar-foreground/60">
          <ShieldCheck className="size-4" /> Sanitation Control Centre · Secure access
        </div>
      </div>

      {/* Form panel */}
      <div className="flex items-center justify-center bg-background p-6">
        <form onSubmit={submit} className="w-full max-w-sm space-y-5" data-testid="login-form">
          <div className="lg:hidden flex items-center gap-3">
            <span className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground">
              <Droplets className="size-5" />
            </span>
            <p className="text-lg font-bold">CleanNode</p>
          </div>
          <div>
            <h2 className="text-2xl font-bold text-foreground">Sign in</h2>
            <p className="mt-1 text-sm text-muted-foreground">Municipal Authority access to the sanitation control centre.</p>
          </div>

          {error ? (
            <div className="rounded-lg bg-danger/10 px-3 py-2 text-sm text-danger" data-testid="login-error">
              {error}
            </div>
          ) : null}

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)}
              required data-testid="login-email" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)}
              required data-testid="login-password" />
          </div>

          <Button type="submit" className="w-full" disabled={loading} data-testid="login-submit">
            {loading ? <Loader2 className="size-4 animate-spin" /> : "Sign in"}
          </Button>

          <div className="rounded-lg bg-muted/60 p-3 text-xs text-muted-foreground">
            <p className="font-semibold text-foreground">Demo account</p>
            <p>Municipal Authority — admin@cleannode.com / Admin@1234</p>
          </div>
        </form>
      </div>
    </div>
  );
}
