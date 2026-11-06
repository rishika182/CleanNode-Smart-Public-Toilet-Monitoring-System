package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.CleanlinessBand
import com.example.data.model.CitizenReportEntity
import com.example.data.model.EscalationEntity
import com.example.data.model.FacilityEntity
import com.example.data.model.FlushHealthStatus
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanNodeDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    // --- Facility Queries ---
    @Query("SELECT * FROM facilities ORDER BY isMonitoredPublic DESC, distanceMeters ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE id = :id LIMIT 1")
    fun getFacilityById(id: String): Flow<FacilityEntity?>

    @Query("SELECT * FROM facilities WHERE id = :id LIMIT 1")
    suspend fun getFacilityByIdDirect(id: String): FacilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<FacilityEntity>)

    @Update
    suspend fun updateFacility(facility: FacilityEntity)

    @Query("""
        UPDATE facilities 
        SET pciScore = :pci, cleanlinessBand = :band, ammoniaPpm = :ammonia, 
            flushFlowRate = :flowRate, flushHealthStatus = :flushStatus, lastUpdatedTimestamp = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateSensorReadings(
        id: String,
        pci: Int,
        band: CleanlinessBand,
        ammonia: Float,
        flowRate: Float,
        flushStatus: FlushHealthStatus,
        timestamp: Long
    )

    // --- Cleaning Duties ---
    @Query("SELECT * FROM cleaning_duties ORDER BY dispatchTime DESC")
    fun getAllDuties(): Flow<List<CleaningDutyEntity>>

    @Query("SELECT * FROM cleaning_duties WHERE assignedStaffPhone = :staffPhone ORDER BY dispatchTime DESC")
    fun getDutiesForStaff(staffPhone: String): Flow<List<CleaningDutyEntity>>

    @Query("SELECT * FROM cleaning_duties WHERE id = :id LIMIT 1")
    suspend fun getDutyById(id: String): CleaningDutyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuty(duty: CleaningDutyEntity)

    @Update
    suspend fun updateDuty(duty: CleaningDutyEntity)

    // --- Escalations ---
    @Query("SELECT * FROM escalations ORDER BY staffNotifiedTime DESC")
    fun getAllEscalations(): Flow<List<EscalationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscalation(escalation: EscalationEntity)

    @Update
    suspend fun updateEscalation(escalation: EscalationEntity)

    // --- Citizen Reports ---
    @Query("SELECT * FROM citizen_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<CitizenReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CitizenReportEntity)

    @Query("UPDATE citizen_reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Query("SELECT * FROM facilities")
    suspend fun getAllFacilitiesList(): List<FacilityEntity>

    @Query("SELECT * FROM cleaning_duties ORDER BY dispatchTime DESC")
    suspend fun getAllDutiesList(): List<CleaningDutyEntity>

    @Query("SELECT COUNT(*) FROM facilities")
    suspend fun getFacilityCount(): Int

    @Query("SELECT COUNT(*) FROM citizen_reports")
    suspend fun getReportCount(): Int

    @Query("SELECT COUNT(*) FROM escalations")
    suspend fun getEscalationCount(): Int
}
