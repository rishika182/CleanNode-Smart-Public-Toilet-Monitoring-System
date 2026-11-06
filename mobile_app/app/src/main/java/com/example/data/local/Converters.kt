package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AppLanguage
import com.example.data.model.CleanlinessBand
import com.example.data.model.DutyStatus
import com.example.data.model.EscalationStage
import com.example.data.model.FlushHealthStatus
import com.example.data.model.ReportCategory
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (_: Exception) {
        UserRole.CITIZEN
    }

    @TypeConverter
    fun fromAppLanguage(value: AppLanguage): String = value.name

    @TypeConverter
    fun toAppLanguage(value: String): AppLanguage = try {
        AppLanguage.valueOf(value)
    } catch (_: Exception) {
        AppLanguage.ENGLISH
    }

    @TypeConverter
    fun fromCleanlinessBand(value: CleanlinessBand): String = value.name

    @TypeConverter
    fun toCleanlinessBand(value: String): CleanlinessBand = try {
        CleanlinessBand.valueOf(value)
    } catch (_: Exception) {
        CleanlinessBand.MODERATE
    }

    @TypeConverter
    fun fromFlushHealthStatus(value: FlushHealthStatus): String = value.name

    @TypeConverter
    fun toFlushHealthStatus(value: String): FlushHealthStatus = try {
        FlushHealthStatus.valueOf(value)
    } catch (_: Exception) {
        FlushHealthStatus.NORMAL
    }

    @TypeConverter
    fun fromDutyStatus(value: DutyStatus): String = value.name

    @TypeConverter
    fun toDutyStatus(value: String): DutyStatus = try {
        DutyStatus.valueOf(value)
    } catch (_: Exception) {
        DutyStatus.DISPATCHED
    }

    @TypeConverter
    fun fromEscalationStage(value: EscalationStage): String = value.name

    @TypeConverter
    fun toEscalationStage(value: String): EscalationStage = try {
        EscalationStage.valueOf(value)
    } catch (_: Exception) {
        EscalationStage.STAFF_DISPATCHED
    }

    @TypeConverter
    fun fromReportCategory(value: ReportCategory): String = value.name

    @TypeConverter
    fun toReportCategory(value: String): ReportCategory = try {
        ReportCategory.valueOf(value)
    } catch (_: Exception) {
        ReportCategory.CLEANLINESS
    }
}
