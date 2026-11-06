import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { Toaster } from "@/components/ui/sonner";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import { CleanNodeProvider } from "@/context/CleanNodeContext";
import Layout from "@/components/Layout";
import Login from "@/pages/Login";
import Overview from "@/pages/Overview";
import Facilities from "@/pages/Facilities";
import FacilityDetail from "@/pages/FacilityDetail";
import LiveMonitor from "@/pages/LiveMonitor";
import Alerts from "@/pages/Alerts";
import Heatmap from "@/pages/Heatmap";
import Maintenance from "@/pages/Maintenance";
import Analytics from "@/pages/Analytics";
import Anomalies from "@/pages/Anomalies";
import StaffDuties from "@/pages/StaffDuties";
import History from "@/pages/History";
import Settings from "@/pages/Settings";

function Loader() {
  return (
    <div className="grid min-h-screen place-items-center bg-background">
      <div className="size-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
    </div>
  );
}

function Protected({ children }) {
  const { user, loading } = useAuth();
  if (loading || user === null) return <Loader />;
  if (!user) return <Navigate to="/login" replace />;
  return (
    <CleanNodeProvider>
      <Layout>{children}</Layout>
    </CleanNodeProvider>
  );
}

function LoginRoute() {
  const { user, loading } = useAuth();
  if (loading) return <Loader />;
  if (user) return <Navigate to="/" replace />;
  return <Login />;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginRoute />} />
          <Route path="/" element={<Protected><Overview /></Protected>} />
          <Route path="/facilities" element={<Protected><Facilities /></Protected>} />
          <Route path="/facilities/:facilityId" element={<Protected><FacilityDetail /></Protected>} />
          <Route path="/live" element={<Protected><LiveMonitor /></Protected>} />
          <Route path="/alerts" element={<Protected><Alerts /></Protected>} />
          <Route path="/heatmap" element={<Protected><Heatmap /></Protected>} />
          <Route path="/maintenance" element={<Protected><Maintenance /></Protected>} />
          <Route path="/analytics" element={<Protected><Analytics /></Protected>} />
          <Route path="/anomalies" element={<Protected><Anomalies /></Protected>} />
          <Route path="/staff" element={<Protected><StaffDuties /></Protected>} />
          <Route path="/history" element={<Protected><History /></Protected>} />
          <Route path="/settings" element={<Protected><Settings /></Protected>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <Toaster position="top-right" richColors />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
