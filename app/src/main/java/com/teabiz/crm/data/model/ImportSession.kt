package com.teabiz.crm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "import_sessions")
data class ImportSession(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileType: String,
    val totalRows: Int = 0,
    val importedRows: Int = 0,
    val failedRows: Int = 0,
    val duplicateRows: Int = 0,
    val status: String = "PENDING",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

@Entity(tableName = "import_errors")
data class ImportError(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: String,
    val rowNumber: Int,
    val columnName: String? = null,
    val rawValue: String? = null,
    val errorReason: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ImportErrorReason(val displayName: String) {
    DUPLICATE_EMAIL("Duplicate Email"),
    DUPLICATE_PHONE("Duplicate Phone"),
    INVALID_FORMAT("Invalid Format"),
    MISSING_REQUIRED_FIELD("Missing Required Field"),
    INVALID_PRODUCT("Invalid Product Category"),
    DATA_TOO_LONG("Data Too Long")
}

enum class ImportStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}
