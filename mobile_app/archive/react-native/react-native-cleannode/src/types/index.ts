export type UserRole = 'CITIZEN' | 'STAFF' | 'SUPERVISOR' | 'ADMIN';
export type AppLanguage = 'en' | 'kn' | 'hi';
export type CleanlinessBand = 'CLEAN' | 'MODERATE' | 'NEEDS_ATTENTION' | 'DANGER_ZONE';

export interface UserProfile {
  phoneNumber: string;
  name: string;
  role: UserRole;
  language: AppLanguage;
}

export interface Facility {
  id: string;
  name: string;
  zone: string;
  address: string;
  latitude: number;
  longitude: number;
  isMonitoredPublic: boolean;
  pciScore: number; // 0..100
  cleanlinessBand: CleanlinessBand;
  ammoniaPpm: number;
  temperatureC: number;
  humidityPct: number;
  occupancyCount: number;
  flushFlowRate: number;
  flushHealthStatus: 'NORMAL' | 'LOW_FLOW' | 'BLOCKAGE_SUSPECTED';
  supervisorName: string;
  supervisorPhone: string;
  assignedStaffName: string;
  assignedStaffPhone: string;
  amenities: string;
  distanceMeters: number;
}

export interface CleaningDuty {
  id: string;
  facilityId: string;
  facilityName: string;
  assignedStaffPhone: string;
  assignedStaffName: string;
  status: 'DISPATCHED' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED' | 'ESCALATED';
  triggerReason: string;
  dispatchTime: number;
  acceptDeadlineTime: number;
}

export interface EscalationRecord {
  id: string;
  facilityId: string;
  facilityName: string;
  stage: 'STAFF_DISPATCHED' | 'SUPERVISOR_NOTIFIED' | 'ADMIN_ESCALATED' | 'RESOLVED';
  triggerCause: string;
  auditLog: string;
}
