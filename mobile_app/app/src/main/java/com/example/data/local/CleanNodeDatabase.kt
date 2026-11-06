package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AppLanguage
import com.example.data.model.CitizenReportEntity
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.CleanlinessBand
import com.example.data.model.DutyStatus
import com.example.data.model.EscalationEntity
import com.example.data.model.EscalationStage
import com.example.data.model.FacilityEntity
import com.example.data.model.FlushHealthStatus
import com.example.data.model.ReportCategory
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        FacilityEntity::class,
        CleaningDutyEntity::class,
        EscalationEntity::class,
        CitizenReportEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CleanNodeDatabase : RoomDatabase() {
    abstract fun cleanNodeDao(): CleanNodeDao

    companion object {
        @Volatile
        private var INSTANCE: CleanNodeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CleanNodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CleanNodeDatabase::class.java,
                    "cleannode_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance

                // Seed initial data if DB is empty, or ensure reports and escalations are populated
                scope.launch(Dispatchers.IO) {
                    val dao = instance.cleanNodeDao()
                    if (dao.getFacilityCount() == 0) {
                        seedDatabase(dao)
                    } else {
                        if (dao.getReportCount() == 0) {
                            seedReports(dao)
                        }
                        if (dao.getEscalationCount() == 0) {
                            seedEscalations(dao)
                        }
                    }
                }

                instance
            }
        }

        private suspend fun seedDatabase(dao: CleanNodeDao) {
            val now = System.currentTimeMillis()

            val initialFacilities = listOf(
                FacilityEntity(
                    id = "CN-BLR-01",
                    name = "Jayanagar BBMP-01",
                    zone = "South Zone - Jayanagar",
                    address = "Jayanagar · Mens, Womens",
                    latitude = 12.9298,
                    longitude = 77.5838,
                    isMonitoredPublic = true,
                    pciScore = 48,
                    cleanlinessBand = CleanlinessBand.NEEDS_ATTENTION,
                    ammoniaPpm = 4.2f,
                    temperatureC = 25.4f,
                    humidityPct = 58.0f,
                    occupancyCount = 142,
                    isOccupied = true,
                    flushFlowRate = 4.8f,
                    flushHealthStatus = FlushHealthStatus.NORMAL,
                    waterTankLevelPct = 88,
                    soapDispenserPct = 75,
                    supervisorName = "Suresh Kumar",
                    supervisorPhone = "+91 98450 12345",
                    assignedStaffName = "Ramesh Babu",
                    assignedStaffPhone = "+91 98860 11223",
                    amenities = "Wheelchair Ramp, Automated Self-Clean, Water ATM",
                    openingHours = "24x7 Open",
                    distanceMeters = 220,
                    cubiclesTotal = 6,
                    cubiclesOccupied = 4,
                    alertsCount = 4,
                    lastCleanedTimestamp = now - (45 * 60 * 1000L)
                ),
                FacilityEntity(
                    id = "CN-BLR-03",
                    name = "Jayanagar BBMP-03",
                    zone = "South Zone - Jayanagar",
                    address = "Jayanagar · Mens, Womens",
                    latitude = 12.9340,
                    longitude = 77.5890,
                    isMonitoredPublic = true,
                    pciScore = 54,
                    cleanlinessBand = CleanlinessBand.MODERATE,
                    ammoniaPpm = 3.6f,
                    temperatureC = 26.8f,
                    humidityPct = 64.0f,
                    occupancyCount = 289,
                    isOccupied = true,
                    flushFlowRate = 5.2f,
                    flushHealthStatus = FlushHealthStatus.NORMAL,
                    waterTankLevelPct = 65,
                    soapDispenserPct = 40,
                    supervisorName = "Anand Vardhan",
                    supervisorPhone = "+91 98451 23456",
                    assignedStaffName = "Fatima",
                    assignedStaffPhone = "+91 98862 33003",
                    amenities = "Sanitary Pad Dispenser, Western & Indian Stalls",
                    openingHours = "24x7 Open",
                    distanceMeters = 290,
                    cubiclesTotal = 6,
                    cubiclesOccupied = 6,
                    alertsCount = 4,
                    lastCleanedTimestamp = now - (150 * 60 * 1000L)
                ),
                FacilityEntity(
                    id = "CN-BLR-04",
                    name = "Cubbon Park BBMP-01",
                    zone = "Central Zone - Cubbon Park",
                    address = "Cubbon Park · Mens",
                    latitude = 12.9738,
                    longitude = 77.5906,
                    isMonitoredPublic = true,
                    pciScore = 61,
                    cleanlinessBand = CleanlinessBand.MODERATE,
                    ammoniaPpm = 2.4f,
                    temperatureC = 25.1f,
                    humidityPct = 60.0f,
                    occupancyCount = 195,
                    isOccupied = true,
                    flushFlowRate = 6.0f,
                    flushHealthStatus = FlushHealthStatus.NORMAL,
                    waterTankLevelPct = 90,
                    soapDispenserPct = 82,
                    supervisorName = "Dr. Prakash Gowda (BBMP)",
                    supervisorPhone = "+91 98452 34567",
                    assignedStaffName = "Ramesh Babu",
                    assignedStaffPhone = "+91 98860 11223",
                    amenities = "High-Capacity Urinals, Disinfectant Station",
                    openingHours = "05:00 AM - 09:00 PM",
                    distanceMeters = 420,
                    cubiclesTotal = 3,
                    cubiclesOccupied = 1,
                    alertsCount = 1,
                    lastCleanedTimestamp = now - (60 * 60 * 1000L)
                ),
                FacilityEntity(
                    id = "CN-BLR-02",
                    name = "Jayanagar BBMP-02",
                    zone = "South Zone - Jayanagar",
                    address = "Jayanagar · Mens, Womens",
                    latitude = 12.9250,
                    longitude = 77.5810,
                    isMonitoredPublic = true,
                    pciScore = 63,
                    cleanlinessBand = CleanlinessBand.MODERATE,
                    ammoniaPpm = 2.1f,
                    temperatureC = 25.8f,
                    humidityPct = 56.0f,
                    occupancyCount = 210,
                    isOccupied = false,
                    flushFlowRate = 5.8f,
                    flushHealthStatus = FlushHealthStatus.NORMAL,
                    waterTankLevelPct = 75,
                    soapDispenserPct = 60,
                    supervisorName = "Suresh Kumar",
                    supervisorPhone = "+91 98450 12345",
                    assignedStaffName = "Girish",
                    assignedStaffPhone = "+91 98453 44004",
                    amenities = "Wheelchair Access, Contactless Flush",
                    openingHours = "24x7 Open",
                    distanceMeters = 310,
                    cubiclesTotal = 6,
                    cubiclesOccupied = 2,
                    alertsCount = 2,
                    lastCleanedTimestamp = now - (90 * 60 * 1000L)
                ),
                FacilityEntity(
                    id = "CN-BLR-05",
                    name = "Majestic KSRTC Terminal BBMP",
                    zone = "Central Zone - Gandhinagar",
                    address = "Platform 1 Exit, Kempegowda Bus Station, Majestic",
                    latitude = 12.9774,
                    longitude = 77.5713,
                    isMonitoredPublic = true,
                    pciScore = 24, // DANGER ZONE!
                    cleanlinessBand = CleanlinessBand.DANGER_ZONE,
                    ammoniaPpm = 9.8f,
                    temperatureC = 28.5f,
                    humidityPct = 76.0f,
                    occupancyCount = 840,
                    isOccupied = true,
                    flushFlowRate = 1.1f,
                    flushHealthStatus = FlushHealthStatus.BLOCKAGE_SUSPECTED,
                    waterTankLevelPct = 25,
                    soapDispenserPct = 10,
                    supervisorName = "Dr. Prakash Gowda (BBMP)",
                    supervisorPhone = "+91 98452 34567",
                    assignedStaffName = "Ramesh Babu",
                    assignedStaffPhone = "+91 98860 11223",
                    amenities = "Differently-abled Access, High Capacity Urinals",
                    openingHours = "24x7 Open",
                    distanceMeters = 650,
                    cubiclesTotal = 6,
                    cubiclesOccupied = 6,
                    alertsCount = 5,
                    lastCleanedTimestamp = now - (360 * 60 * 1000L)
                ),
                // PRIVATE / UNMONITORED RESTROOMS (SRS v2.0 FR14.3)
                FacilityEntity(
                    id = "PRV-BLR-01",
                    name = "Nexus Shantiniketan Restroom (Private)",
                    zone = "East Zone - Whitefield",
                    address = "Food Court Level 3, Whitefield Main Road, Bengaluru",
                    latitude = 12.9890,
                    longitude = 77.7280,
                    isMonitoredPublic = false,
                    pciScore = 0,
                    cleanlinessBand = CleanlinessBand.MODERATE,
                    ammoniaPpm = 0f,
                    temperatureC = 0f,
                    humidityPct = 0f,
                    occupancyCount = 0,
                    isOccupied = false,
                    flushFlowRate = 0f,
                    flushHealthStatus = FlushHealthStatus.NORMAL,
                    waterTankLevelPct = 0,
                    soapDispenserPct = 0,
                    supervisorName = "Mall Management Desk",
                    supervisorPhone = "+91 80 4912 3456",
                    assignedStaffName = "Mall Facilities Staff",
                    assignedStaffPhone = "+91 80 4912 3457",
                    amenities = "Mall Customers Only, Accessible, Baby Care Lounge",
                    openingHours = "10:00 AM - 10:00 PM",
                    distanceMeters = 500,
                    cubiclesTotal = 4,
                    cubiclesOccupied = 1,
                    alertsCount = 0,
                    lastCleanedTimestamp = 0L
                )
            )

            dao.insertFacilities(initialFacilities)

            // Seed the 5 auto-dispatched unaccepted duties from Screenshot 8 (Needs your action)
            val initialDuties = listOf(
                CleaningDutyEntity(
                    id = "DUTY-01",
                    facilityId = "CN-BLR-04",
                    facilityName = "Cubbon Park BBMP-01",
                    cubicle = "Men's · Cubicle 1",
                    assignedStaffPhone = "+91 98860 11223",
                    assignedStaffName = "Ramesh",
                    status = DutyStatus.DISPATCHED,
                    triggerReason = "Auto-assigned to Ramesh · not accepted for 120 min",
                    priority = "High",
                    overdueMinutes = 120,
                    dispatchTime = now - (120 * 60 * 1000L),
                    acceptDeadlineTime = now - (110 * 60 * 1000L)
                ),
                CleaningDutyEntity(
                    id = "DUTY-02",
                    facilityId = "CN-BLR-02",
                    facilityName = "Jayanagar BBMP-02",
                    cubicle = "Men's · Cubicle 3",
                    assignedStaffPhone = "+91 98453 44004",
                    assignedStaffName = "Girish",
                    status = DutyStatus.DISPATCHED,
                    triggerReason = "Auto-assigned to Girish · not accepted for 180 min",
                    priority = "High",
                    overdueMinutes = 180,
                    dispatchTime = now - (180 * 60 * 1000L),
                    acceptDeadlineTime = now - (170 * 60 * 1000L)
                ),
                CleaningDutyEntity(
                    id = "DUTY-03",
                    facilityId = "CN-BLR-03",
                    facilityName = "Jayanagar BBMP-03",
                    cubicle = "Men's · Cubicle 2",
                    assignedStaffPhone = "+91 98862 33003",
                    assignedStaffName = "Fatima",
                    status = DutyStatus.DISPATCHED,
                    triggerReason = "Auto-assigned to Fatima · not accepted for 240 min",
                    priority = "High",
                    overdueMinutes = 240,
                    dispatchTime = now - (240 * 60 * 1000L),
                    acceptDeadlineTime = now - (230 * 60 * 1000L)
                ),
                CleaningDutyEntity(
                    id = "DUTY-04",
                    facilityId = "CN-BLR-01",
                    facilityName = "Jayanagar BBMP-01",
                    cubicle = "Women's · Cubicle 1",
                    assignedStaffPhone = "+91 98451 22002",
                    assignedStaffName = "Lakshmi",
                    status = DutyStatus.DISPATCHED,
                    triggerReason = "Auto-assigned to Lakshmi · not accepted for 300 min",
                    priority = "High",
                    overdueMinutes = 300,
                    dispatchTime = now - (300 * 60 * 1000L),
                    acceptDeadlineTime = now - (290 * 60 * 1000L)
                ),
                CleaningDutyEntity(
                    id = "DUTY-05",
                    facilityId = "CN-BLR-02",
                    facilityName = "Jayanagar BBMP-02",
                    cubicle = "Women's · Cubicle 2",
                    assignedStaffPhone = "+91 98450 11001",
                    assignedStaffName = "Suresh",
                    status = DutyStatus.DISPATCHED,
                    triggerReason = "Auto-assigned to Suresh · not accepted for 300 min",
                    priority = "Medium",
                    overdueMinutes = 300,
                    dispatchTime = now - (300 * 60 * 1000L),
                    acceptDeadlineTime = now - (290 * 60 * 1000L)
                )
            )

            for (duty in initialDuties) {
                dao.insertDuty(duty)
            }

            seedEscalations(dao)
            seedReports(dao)

            // Seed default citizen
            val defaultUser = UserEntity(
                phoneNumber = "+91 98450 99887",
                name = "BBMP Official",
                role = UserRole.ADMIN,
                preferredLanguage = AppLanguage.ENGLISH,
                assignedFacilityId = "CN-BLR-01",
                isLoggedIn = true,
                lastLoginTime = now
            )
            dao.insertUser(defaultUser)
        }

        suspend fun seedEscalations(dao: CleanNodeDao) {
            val now = System.currentTimeMillis()
            val initialEscalations = listOf(
                EscalationEntity(
                    id = "ESC-BLR-109",
                    alertId = "ALT-DZ-401",
                    facilityId = "CN-BLR-05",
                    facilityName = "Majestic KSRTC Terminal BBMP",
                    initialPci = 24,
                    triggerCause = "Critical Odor Spike & Low Flush Flow Rate",
                    stage = EscalationStage.STAFF_DISPATCHED,
                    staffNotifiedTime = now - (3 * 60 * 1000L),
                    auditLog = "10:15 AM - Node detected PCI dropped to 24 (Ammonia: 9.8 ppm, Flow: 1.1 L/min)\n10:16 AM - Automated Danger Zone alert generated\n10:16 AM - Nearest on-duty staff Ramesh Babu dispatched (10-minute acceptance timer running)"
                ),
                EscalationEntity(
                    id = "ESC-BLR-108",
                    alertId = "ALT-DZ-402",
                    facilityId = "CN-BLR-01",
                    facilityName = "Jayanagar BBMP-01",
                    initialPci = 28,
                    triggerCause = "Ammonia 4.2 ppm exceeding safety threshold",
                    stage = EscalationStage.SUPERVISOR_NOTIFIED,
                    staffNotifiedTime = now - (15 * 60 * 1000L),
                    supervisorNotifiedTime = now - (5 * 60 * 1000L),
                    auditLog = "09:45 AM - Sensor threshold exceeded (Ammonia 4.2 ppm)\n09:46 AM - Dispatched to Ramesh\n09:56 AM - Acceptance timeout -> Escalated to Supervisor Suresh Kumar"
                ),
                EscalationEntity(
                    id = "ESC-BLR-107",
                    alertId = "ALT-DZ-403",
                    facilityId = "CN-BLR-04",
                    facilityName = "Cubbon Park BBMP-01",
                    initialPci = 32,
                    triggerCause = "Water tank depleted below 15%",
                    stage = EscalationStage.ADMIN_ESCALATED,
                    staffNotifiedTime = now - (35 * 60 * 1000L),
                    supervisorNotifiedTime = now - (25 * 60 * 1000L),
                    adminEscalatedTime = now - (10 * 60 * 1000L),
                    auditLog = "09:20 AM - Water tank low sensor triggered\n09:30 AM - Escalated to Supervisor\n09:45 AM - Supervisor unaddressed -> BBMP Central Command Escalated"
                )
            )
            for (esc in initialEscalations) {
                dao.insertEscalation(esc)
            }
        }

        suspend fun seedReports(dao: CleanNodeDao) {
            val now = System.currentTimeMillis()
            val initialReports = listOf(
                CitizenReportEntity(
                    id = "REP-BLR-201",
                    facilityId = "CN-BLR-01",
                    facilityName = "Jayanagar BBMP-01",
                    citizenPhone = "+91 98450 11223",
                    rating = 2,
                    category = ReportCategory.ODOR,
                    comment = "Severe ammonia smell inside Cubicle 2. Exhaust fan is not spinning and washbasin tap is leaking.",
                    aiTriagePriority = "CRITICAL",
                    photoUri = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=400&q=80",
                    status = "PENDING_TRIAGE",
                    timestamp = now - (18 * 60 * 1000L)
                ),
                CitizenReportEntity(
                    id = "REP-BLR-202",
                    facilityId = "CN-BLR-04",
                    facilityName = "Cubbon Park BBMP-01",
                    citizenPhone = "+91 98860 33445",
                    rating = 1,
                    category = ReportCategory.CLOG,
                    comment = "Stall 1 flush handle broken and bowl clogged. Immediate cleaning needed before morning peak rush.",
                    aiTriagePriority = "HIGH",
                    photoUri = "https://images.unsplash.com/photo-1542013936693-884638332954?w=400&q=80",
                    status = "PENDING_TRIAGE",
                    timestamp = now - (42 * 60 * 1000L)
                ),
                CitizenReportEntity(
                    id = "REP-BLR-203",
                    facilityId = "CN-BLR-03",
                    facilityName = "Jayanagar BBMP-03",
                    citizenPhone = "+91 99001 22334",
                    rating = 3,
                    category = ReportCategory.CLEANLINESS,
                    comment = "Soap dispenser empty in women's section and floor is slippery with water.",
                    aiTriagePriority = "MEDIUM",
                    photoUri = "https://images.unsplash.com/photo-1584622781564-1d987f7333c1?w=400&q=80",
                    status = "RESOLVED",
                    timestamp = now - (120 * 60 * 1000L)
                ),
                CitizenReportEntity(
                    id = "REP-BLR-204",
                    facilityId = "CN-BLR-05",
                    facilityName = "Majestic KSRTC Terminal BBMP",
                    citizenPhone = "+91 97410 55667",
                    rating = 1,
                    category = ReportCategory.WATER_SHORTAGE,
                    comment = "Zero running water in taps. Handwash basin completely dry, commuters stranded.",
                    aiTriagePriority = "CRITICAL",
                    photoUri = "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=400&q=80",
                    status = "PENDING_TRIAGE",
                    timestamp = now - (10 * 60 * 1000L)
                )
            )
            for (rep in initialReports) {
                dao.insertReport(rep)
            }
        }
    }
}
