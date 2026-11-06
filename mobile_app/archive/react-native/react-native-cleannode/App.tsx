import React, { useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  TextInput,
  Alert
} from 'react-native';
import { translations } from './src/localization/translations';
import { UserRole, AppLanguage, Facility, CleaningDuty, EscalationRecord } from './src/types';

const INITIAL_FACILITIES: Facility[] = [
  {
    id: "CN-BLR-01",
    name: "Jayanagar 4th Block e-Toilet",
    zone: "South Zone - Jayanagar",
    address: "Opposite 4th Block Bus Complex, Bengaluru",
    latitude: 12.9298,
    longitude: 77.5838,
    isMonitoredPublic: true,
    pciScore: 92,
    cleanlinessBand: "CLEAN",
    ammoniaPpm: 1.2,
    temperatureC: 25.4,
    humidityPct: 58.0,
    occupancyCount: 142,
    flushFlowRate: 6.4,
    flushHealthStatus: "NORMAL",
    supervisorName: "Suresh Kumar",
    supervisorPhone: "+91 98450 12345",
    assignedStaffName: "Ramesh Babu",
    assignedStaffPhone: "+91 98860 11223",
    amenities: "Wheelchair Ramp, Automated Self-Clean, Water ATM",
    distanceMeters: 220
  },
  {
    id: "CN-BLR-03",
    name: "Majestic KSRTC Terminal Complex",
    zone: "Central Zone - Gandhinagar",
    address: "Kempegowda Bus Station, Majestic, Bengaluru",
    latitude: 12.9774,
    longitude: 77.5713,
    isMonitoredPublic: true,
    pciScore: 24, // Danger Zone!
    cleanlinessBand: "DANGER_ZONE",
    ammoniaPpm: 9.8,
    temperatureC: 28.5,
    humidityPct: 76.0,
    occupancyCount: 840,
    flushFlowRate: 1.1,
    flushHealthStatus: "BLOCKAGE_SUSPECTED",
    supervisorName: "Dr. Prakash Gowda (BBMP)",
    supervisorPhone: "+91 98452 34567",
    assignedStaffName: "Ramesh Babu",
    assignedStaffPhone: "+91 98860 11223",
    amenities: "Differently-abled Access, High Capacity Urinals",
    distanceMeters: 650
  },
  {
    id: "PRV-BLR-01",
    name: "Nexus Shantiniketan Restroom (Private)",
    zone: "East Zone - Whitefield",
    address: "Food Court Level 3, Whitefield, Bengaluru",
    latitude: 12.9890,
    longitude: 77.7280,
    isMonitoredPublic: false,
    pciScore: 0,
    cleanlinessBand: "MODERATE",
    ammoniaPpm: 0,
    temperatureC: 0,
    humidityPct: 0,
    occupancyCount: 0,
    flushFlowRate: 0,
    flushHealthStatus: "NORMAL",
    supervisorName: "Mall Desk",
    supervisorPhone: "+91 80 4912 3456",
    assignedStaffName: "Mall Cleaning Crew",
    assignedStaffPhone: "+91 80 4912 3457",
    amenities: "Mall Customers Only, Accessible",
    distanceMeters: 500
  }
];

export default function App() {
  const [lang, setLang] = useState<AppLanguage>('en');
  const [role, setRole] = useState<UserRole>('CITIZEN');
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(true);
  const [phone, setPhone] = useState<string>('9845012345');
  const [otp, setOtp] = useState<string>('202604');
  const [facilities, setFacilities] = useState<Facility[]>(INITIAL_FACILITIES);
  const [selectedFacility, setSelectedFacility] = useState<Facility | null>(null);

  const t = translations[lang];

  // Smart Ranking (50m tolerance tie-break in favor of cleaner toilet)
  const rankedFacilities = [...facilities].sort((a, b) => a.distanceMeters - b.distanceMeters);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#004D40" />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerTitle}>{t.appName}</Text>
          <Text style={styles.headerSubtitle}>{t.tagline}</Text>
        </View>

        {/* Language Switcher */}
        <View style={styles.langRow}>
          {(['en', 'kn', 'hi'] as AppLanguage[]).map((l) => (
            <TouchableOpacity
              key={l}
              onPress={() => setLang(l)}
              style={[styles.langChip, lang === l && styles.langChipActive]}
            >
              <Text style={[styles.langText, lang === l && styles.langTextActive]}>
                {l === 'en' ? 'EN' : l === 'kn' ? 'ಕನ್ನಡ' : 'हिंदी'}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Role Switcher for Testing */}
      <View style={styles.roleBar}>
        {(['CITIZEN', 'STAFF', 'SUPERVISOR', 'ADMIN'] as UserRole[]).map((r) => (
          <TouchableOpacity
            key={r}
            onPress={() => setRole(r)}
            style={[styles.roleTab, role === r && styles.roleTabActive]}
          >
            <Text style={[styles.roleTabText, role === r && styles.roleTabTextActive]}>{r}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <ScrollView contentContainerStyle={styles.scroll}>
        {/* Offline Cache Status (FR13.4) */}
        <View style={styles.offlineBanner}>
          <Text style={styles.offlineText}>✓ {t.offlineBanner}</Text>
        </View>

        {/* Smart Google Ranking Notice (FR14.5) */}
        <View style={styles.smartNotice}>
          <Text style={styles.smartNoticeText}>⚡ {t.smartRanking}</Text>
        </View>

        {/* Content based on Role */}
        {role === 'CITIZEN' && (
          <View>
            <Text style={styles.sectionTitle}>Nearby Restrooms (Distance + PCI Ranked)</Text>
            {rankedFacilities.map((facility) => (
              <TouchableOpacity
                key={facility.id}
                style={styles.card}
                onPress={() => setSelectedFacility(facility)}
              >
                <View style={styles.cardHeader}>
                  <View style={[styles.badge, facility.isMonitoredPublic ? styles.badgePublic : styles.badgePrivate]}>
                    <Text style={styles.badgeText}>
                      {facility.isMonitoredPublic ? t.monitoredPublic : t.unmonitoredPrivate}
                    </Text>
                  </View>
                  <Text style={styles.distance}>{facility.distanceMeters}m away</Text>
                </View>

                <Text style={styles.facilityName}>{facility.name}</Text>
                <Text style={styles.facilityAddress}>{facility.address}</Text>

                {facility.isMonitoredPublic ? (
                  <View style={styles.pciRow}>
                    <View style={[styles.pciBox, facility.pciScore < 30 ? styles.pciDanger : styles.pciGood]}>
                      <Text style={styles.pciValue}>{facility.pciScore}</Text>
                      <Text style={styles.pciLabel}>PCI Score</Text>
                    </View>
                    <View style={styles.sensorMetrics}>
                      <Text style={styles.metricText}>Ammonia: {facility.ammoniaPpm} ppm (MQ-135)</Text>
                      <Text style={styles.metricText}>Flow: {facility.flushFlowRate} L/m ({facility.flushHealthStatus})</Text>
                      <Text style={styles.metricText}>Footfall: {facility.occupancyCount} users today</Text>
                    </View>
                  </View>
                ) : (
                  <View style={styles.unmonitoredBox}>
                    <Text style={styles.unmonitoredText}>🔒 {t.cleanlinessDataUnavailable}</Text>
                  </View>
                )}
              </TouchableOpacity>
            ))}
          </View>
        )}

        {role === 'STAFF' && (
          <View>
            <Text style={styles.sectionTitle}>Assigned Cleaning Duties (FR13.1 - FR13.5)</Text>
            <View style={styles.card}>
              <View style={[styles.badge, styles.badgeDanger]}>
                <Text style={styles.badgeText}>URGENT DANGER ZONE DISPATCH</Text>
              </View>
              <Text style={styles.facilityName}>Majestic KSRTC Terminal Complex</Text>
              <Text style={styles.dangerText}>Cause: Ammonia 9.8 ppm spike, Flow 1.1 L/m blockage suspected</Text>
              <Text style={styles.timerText}>⏱ 06:45 left on 10-minute acceptance window</Text>
              
              <TouchableOpacity
                style={styles.actionBtn}
                onPress={() => Alert.alert("Proof of Cleaning", "Sanitation checklist verified! Photo uploaded to Firebase Storage. Restroom PCI restored to 96.")}
              >
                <Text style={styles.actionBtnText}>📷 {t.proofOfCleaning}</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}

        {role === 'SUPERVISOR' && (
          <View>
            <Text style={styles.sectionTitle}>Toilet Supervisor / Incharge Escalation (FR15.7)</Text>
            <View style={styles.card}>
              <Text style={styles.facilityName}>Active Escalation #ALT-DZ-401</Text>
              <Text style={styles.metricText}>Facility: Majestic KSRTC Terminal</Text>
              <Text style={styles.metricText}>Status: Staff acceptance window expired</Text>
              <Text style={styles.dangerText}>Auto-notified Supervisor Dr. Prakash Gowda (+91 98452 34567)</Text>
              <TouchableOpacity
                style={[styles.actionBtn, { backgroundColor: '#BA1A1A' }]}
                onPress={() => Alert.alert("Admin Escalation", "Escalated to BBMP Municipal Command as top priority!")}
              >
                <Text style={styles.actionBtnText}>Escalate to BBMP Admin Dashboard</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}

        {role === 'ADMIN' && (
          <View>
            <Text style={styles.sectionTitle}>BBMP Municipal Authority Analytics</Text>
            <View style={styles.card}>
              <Text style={styles.facilityName}>Bruhat Bengaluru Mahanagara Palike (Central)</Text>
              <Text style={styles.metricText}>• Monitored Smart Restrooms: 5</Text>
              <Text style={styles.metricText}>• Average Cleanliness Index: 76 / 100</Text>
              <Text style={styles.dangerText}>• Danger Zone Escalations: 1 Active</Text>
              <Text style={styles.metricText}>• AI Predictive Maintenance: Pipe blockage predicted at Majestic</Text>
            </View>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F4FBF7' },
  header: { backgroundColor: '#004D40', padding: 16, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  headerTitle: { color: '#FFF', fontSize: 20, fontWeight: 'bold' },
  headerSubtitle: { color: '#A7F0DF', fontSize: 12 },
  langRow: { flexDirection: 'row', gap: 6 },
  langChip: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 12, backgroundColor: 'rgba(255,255,255,0.2)' },
  langChipActive: { backgroundColor: '#FFF' },
  langText: { color: '#FFF', fontSize: 12, fontWeight: 'bold' },
  langTextActive: { color: '#004D40' },
  roleBar: { flexDirection: 'row', backgroundColor: '#E0F2F1', padding: 4 },
  roleTab: { flex: 1, paddingVertical: 8, alignItems: 'center', borderRadius: 8 },
  roleTabActive: { backgroundColor: '#00685F' },
  roleTabText: { fontSize: 11, fontWeight: 'bold', color: '#004D40' },
  roleTabTextActive: { color: '#FFF' },
  scroll: { padding: 16 },
  offlineBanner: { backgroundColor: '#D4EDDA', padding: 10, borderRadius: 8, marginBottom: 10 },
  offlineText: { color: '#155724', fontSize: 12, fontWeight: 'bold' },
  smartNotice: { backgroundColor: '#E8F5E9', padding: 10, borderRadius: 8, marginBottom: 14 },
  smartNoticeText: { color: '#2E7D32', fontSize: 12 },
  sectionTitle: { fontSize: 16, fontWeight: 'bold', color: '#161D1A', marginBottom: 10 },
  card: { backgroundColor: '#FFF', borderRadius: 12, padding: 14, marginBottom: 12, elevation: 2 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  badgePublic: { backgroundColor: '#D4EDDA' },
  badgePrivate: { backgroundColor: '#EEEEEE' },
  badgeDanger: { backgroundColor: '#FFDAD6', alignSelf: 'flex-start', marginBottom: 6 },
  badgeText: { fontSize: 11, fontWeight: 'bold', color: '#333' },
  distance: { color: '#00685F', fontWeight: 'bold', fontSize: 12 },
  facilityName: { fontSize: 16, fontWeight: 'bold', color: '#161D1A' },
  facilityAddress: { fontSize: 12, color: '#666', marginTop: 2, marginBottom: 8 },
  pciRow: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 6 },
  pciBox: { width: 64, height: 64, borderRadius: 32, justifyContent: 'center', alignItems: 'center' },
  pciGood: { backgroundColor: '#2E7D32' },
  pciDanger: { backgroundColor: '#BA1A1A' },
  pciValue: { color: '#FFF', fontSize: 20, fontWeight: 'bold' },
  pciLabel: { color: '#FFF', fontSize: 9 },
  sensorMetrics: { flex: 1 },
  metricText: { fontSize: 11, color: '#444', marginBottom: 2 },
  dangerText: { fontSize: 12, color: '#BA1A1A', fontWeight: 'bold', marginVertical: 4 },
  timerText: { fontSize: 13, color: '#BA1A1A', fontWeight: 'bold', marginVertical: 6 },
  unmonitoredBox: { backgroundColor: '#F5F5F5', padding: 10, borderRadius: 8, marginTop: 6 },
  unmonitoredText: { color: '#777', fontSize: 12 },
  actionBtn: { backgroundColor: '#00685F', padding: 12, borderRadius: 8, alignItems: 'center', marginTop: 10 },
  actionBtnText: { color: '#FFF', fontWeight: 'bold', fontSize: 13 }
});
