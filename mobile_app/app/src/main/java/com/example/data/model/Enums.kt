package com.example.data.model

enum class UserRole(val displayName: String) {
    CITIZEN("Citizen / Public"),
    STAFF("Maintenance Staff"),
    SUPERVISOR("Toilet Supervisor"),
    ADMIN("Municipal Authority (BBMP)")
}

enum class AppLanguage(val code: String, val nativeName: String) {
    ENGLISH("en", "English"),
    KANNADA("kn", "ಕನ್ನಡ (Kannada)"),
    HINDI("hi", "हिंदी (Hindi)")
}

enum class CleanlinessBand(val label: String, val minPci: Int, val maxPci: Int) {
    CLEAN("Clean / Excellent", 80, 100),
    MODERATE("Moderate", 50, 79),
    NEEDS_ATTENTION("Needs Attention", 30, 49),
    DANGER_ZONE("Danger Zone (Critical)", 0, 29);

    companion object {
        fun fromPci(score: Int): CleanlinessBand {
            return when {
                score >= 80 -> CLEAN
                score >= 50 -> MODERATE
                score >= 30 -> NEEDS_ATTENTION
                else -> DANGER_ZONE
            }
        }
    }
}

enum class FlushHealthStatus(val displayName: String) {
    NORMAL("Normal Flush"),
    LOW_FLOW("Low Water Flow"),
    NO_FLUSH("Flush Not Triggered"),
    BLOCKAGE_SUSPECTED("Potential Blockage Detected")
}

enum class DutyStatus {
    DISPATCHED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    ESCALATED
}

enum class EscalationStage(val displayName: String) {
    STAFF_DISPATCHED("Stage 1: Staff Dispatched (10m window)"),
    SUPERVISOR_NOTIFIED("Stage 2: Supervisor Escalated"),
    ADMIN_ESCALATED("Stage 3: BBMP Admin Dashboard Top Priority"),
    RESOLVED("Resolved & Cleared")
}

enum class ReportCategory(val displayName: String) {
    ODOR("Foul Odor / Poor Air"),
    CLOG("Toilet Clog / Drainage Blockage"),
    WATER_SHORTAGE("Water Shortage / Flush Inactive"),
    CLEANLINESS("Dirty Surface / Litter"),
    SAFETY("Lighting / Lock / Structural Issue")
}
