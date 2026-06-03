package com.teabiz.crm.util

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReaderBuilder
import com.teabiz.crm.data.model.Lead
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object ExcelParser {

    data class ParseResult(
        val leads: List<Lead>,
        val errors: List<ParseError>,
        val totalRows: Int
    )

    data class ParseError(
        val rowNumber: Int,
        val reason: String
    )

    data class ColumnMapping(
        val nameColumn: Int = -1,
        val emailColumn: Int = -1,
        val phoneColumn: Int = -1,
        val productColumn: Int = -1,
        val messageColumn: Int = -1,
        val companyColumn: Int = -1,
        val cityColumn: Int = -1
    )

    private val columnAliases = mapOf(
        "name" to listOf("name", "lead name", "contact name", "company name", "firm", "contact", "customer"),
        "email" to listOf("email", "email id", "email address", "mail", "e-mail"),
        "phone" to listOf("phone", "mobile", "contact number", "telephone", "cell", "mobile number", "number", "whatsapp", "phone number"),
        "product" to listOf("product", "product interest", "requirement", "service", "item", "product type", "interest"),
        "message" to listOf("message", "inquiry", "details", "description", "notes", "query", "remarks", "comment"),
        "company" to listOf("company", "firm", "organization", "business name", "business"),
        "city" to listOf("city", "location", "area", "region", "place", "address", "state")
    )

    private val phoneRegex = Regex("(?:\\+91|0)?[\\s-]?[6-9]\\d{9}|\\+\\d{1,3}[\\s-]?\\d{4,14}|\\d{10,13}")

    private val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    fun parseExcel(context: Context, uri: Uri): ParseResult {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ParseResult(
            emptyList(), listOf(ParseError(0, "Cannot open file")), 0
        )

        val fileName = getFileName(context, uri)

        try {
            when {
                fileName.endsWith(".csv", true) -> {
                    val result = parseCSV(context, uri)
                    return result
                }
                fileName.endsWith(".xls", true) -> {
                    val workbook = HSSFWorkbook(inputStream)
                    val result = parseWorkbook(workbook)
                    workbook.close()
                    return result
                }
                fileName.endsWith(".xlsx", true) -> {
                    val workbook = XSSFWorkbook(inputStream)
                    val result = parseWorkbook(workbook)
                    workbook.close()
                    return result
                }
                else -> {
                    return ParseResult(emptyList(), listOf(ParseError(0, "Unsupported file format")), 0)
                }
            }
        } catch (e: Exception) {
            return ParseResult(emptyList(), listOf(ParseError(0, "Error parsing file: ${e.message}")), 0)
        } finally {
            inputStream.close()
        }
    }

    private fun parseCSV(context: Context, uri: Uri): ParseResult {
        val leads = mutableListOf<Lead>()
        val errors = mutableListOf<ParseError>()
        var totalRows = 0

        val inputStream = context.contentResolver.openInputStream(uri) ?: return ParseResult(
            emptyList(), listOf(ParseError(0, "Cannot open CSV file")), 0
        )

        try {
            val reader = CSVReaderBuilder(BufferedReader(InputStreamReader(inputStream))).build()
            val headerRow = reader.readNext() ?: return ParseResult(emptyList(), listOf(ParseError(0, "Empty CSV file")), 0)

            val mapping = detectColumnMapping(headerRow.toList())
            var rowNumber = 1

            var nextLine = reader.readNext()
            while (nextLine != null) {
                totalRows++
                rowNumber++

                try {
                    val lead = mapRowToLead(nextLine.toList(), mapping, rowNumber)
                    if (lead != null) {
                        leads.add(lead)
                    } else {
                        errors.add(ParseError(rowNumber, "Invalid or empty row"))
                    }
                } catch (e: Exception) {
                    errors.add(ParseError(rowNumber, e.message ?: "Unknown error"))
                }

                nextLine = reader.readNext()
            }

            reader.close()
        } catch (e: Exception) {
            errors.add(ParseError(0, "Error reading CSV: ${e.message}"))
        } finally {
            inputStream.close()
        }

        return ParseResult(leads, errors, totalRows)
    }

    private fun parseWorkbook(workbook: Workbook): ParseResult {
        val leads = mutableListOf<Lead>()
        val errors = mutableListOf<ParseError>()
        var totalRows = 0

        val sheet = workbook.getSheetAt(0)
        val headerRow = sheet.getRow(0) ?: return ParseResult(emptyList(), listOf(ParseError(0, "Empty sheet")), 0)

        val headerValues = (0 until headerRow.lastCellNum).map { idx ->
            getCellStringValue(headerRow.getCell(idx))
        }
        val mapping = detectColumnMapping(headerValues)

        for (i in 1..sheet.lastRowNum) {
            totalRows++
            val row = sheet.getRow(i)

            if (row == null) {
                continue
            }

            try {
                val cellValues = (0 until headerRow.lastCellNum).map { idx ->
                    getCellStringValue(row.getCell(idx))
                }
                val lead = mapRowToLead(cellValues, mapping, i + 1)
                if (lead != null) {
                    leads.add(lead)
                } else {
                    errors.add(ParseError(i + 1, "Invalid or empty row"))
                }
            } catch (e: Exception) {
                errors.add(ParseError(i + 1, e.message ?: "Unknown error"))
            }
        }

        return ParseResult(leads, errors, totalRows)
    }

    fun detectColumnMapping(headers: List<String>): ColumnMapping {
        var nameCol = -1
        var emailCol = -1
        var phoneCol = -1
        var productCol = -1
        var messageCol = -1
        var companyCol = -1
        var cityCol = -1

        headers.forEachIndexed { index, header ->
            val normalizedHeader = header.lowercase().trim()
            when {
                columnAliases["name"]?.any { normalizedHeader.contains(it) } == true && nameCol == -1 -> nameCol = index
                columnAliases["email"]?.any { normalizedHeader.contains(it) } == true && emailCol == -1 -> emailCol = index
                columnAliases["phone"]?.any { normalizedHeader.contains(it) } == true && phoneCol == -1 -> phoneCol = index
                columnAliases["product"]?.any { normalizedHeader.contains(it) } == true && productCol == -1 -> productCol = index
                columnAliases["message"]?.any { normalizedHeader.contains(it) } == true && messageCol == -1 -> messageCol = index
                columnAliases["company"]?.any { normalizedHeader.contains(it) } == true && companyCol == -1 -> companyCol = index
                columnAliases["city"]?.any { normalizedHeader.contains(it) } == true && cityCol == -1 -> cityCol = index
            }
        }

        return ColumnMapping(
            nameColumn = nameCol,
            emailColumn = emailCol,
            phoneColumn = phoneCol,
            productColumn = productCol,
            messageColumn = messageCol,
            companyColumn = companyCol,
            cityColumn = cityCol
        )
    }

    private fun mapRowToLead(
        cells: List<String>,
        mapping: ColumnMapping,
        rowNumber: Int
    ): Lead? {
        var name = cells.getOrNull(mapping.nameColumn)?.trim() ?: ""
        var email = cells.getOrNull(mapping.emailColumn)?.trim() ?: ""
        var phone = cells.getOrNull(mapping.phoneColumn)?.trim() ?: ""
        var city = cells.getOrNull(mapping.cityColumn)?.trim() ?: ""
        var company = cells.getOrNull(mapping.companyColumn)?.trim() ?: ""
        var message = cells.getOrNull(mapping.messageColumn)?.trim() ?: ""
        var productRaw = cells.getOrNull(mapping.productColumn)?.trim() ?: ""

        // Smart extraction: scan ALL fields for phone numbers and emails
        val allCells = cells.toMutableList()
        val extractedPhones = mutableListOf<String>()
        val extractedEmails = mutableListOf<String>()

        for (cellValue in allCells) {
            if (cellValue.isBlank()) continue

            // Extract phone numbers from any field
            phoneRegex.findAll(cellValue).forEach { match ->
                val found = match.value.trim()
                if (found.length >= 10 && extractedPhones.none { it == found }) {
                    extractedPhones.add(found)
                }
            }

            // Extract emails from any field
            emailRegex.findAll(cellValue).forEach { match ->
                val found = match.value.trim()
                if (extractedEmails.none { it == found }) {
                    extractedEmails.add(found)
                }
            }
        }

        // If phone field is empty but we found phones elsewhere, use the first one
        if (phone.isBlank() && extractedPhones.isNotEmpty()) {
            phone = extractedPhones.first()
        }

        // If email field is empty but we found emails elsewhere, use the first one
        if (email.isBlank() && extractedEmails.isNotEmpty()) {
            email = extractedEmails.first()
        }

        // Clean city field: remove phone numbers and emails that leaked in
        if (city.isNotBlank()) {
            city = phoneRegex.replace(city, "").trim()
            city = emailRegex.replace(city, "").trim()
            city = city.replace(Regex("[,;]\\s*$"), "").trim()
            city = city.replace(Regex("^\\s*[,;]\\s*"), "").trim()
            if (city.isBlank() || city.length < 2) city = ""
        }

        // Clean name field: remove phone numbers that leaked in
        if (name.isNotBlank()) {
            val cleanedName = phoneRegex.replace(name, "").trim()
            if (cleanedName.isNotBlank() && cleanedName.length >= 2) {
                name = cleanedName
            }
        }

        // If still no phone and there are extra extracted phones, assign remaining ones
        if (phone.isBlank() && extractedPhones.size > 1) {
            phone = extractedPhones[1]
        }

        if (name.isBlank() && email.isBlank() && phone.isBlank()) {
            return null
        }

        val productInterest = if (productRaw.isNotBlank()) {
            productRaw.split(",", "/", "&").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        return Lead(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Unknown" },
            email = email,
            phone = phone,
            company = company,
            productInterest = productInterest,
            message = message,
            city = city,
            source = "EXCEL"
        )
    }

    private fun getCellStringValue(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                    val num = cell.numericCellValue
                    if (num == num.toLong().toDouble()) {
                        num.toLong().toString()
                    } else {
                        num.toString()
                    }
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try {
                cell.stringCellValue
            } catch (e: Exception) {
                try {
                    val num = cell.numericCellValue
                    if (num == num.toLong().toDouble()) {
                        num.toLong().toString()
                    } else {
                        num.toString()
                    }
                } catch (e2: Exception) {
                    ""
                }
            }
            else -> ""
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex) ?: "unknown"
                }
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }
}
