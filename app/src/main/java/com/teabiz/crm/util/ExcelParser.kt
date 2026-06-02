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
        "name" to listOf("name", "lead name", "contact name", "company name", "firm", "contact"),
        "email" to listOf("email", "email id", "email address", "mail", "e-mail"),
        "phone" to listOf("phone", "mobile", "contact number", "telephone", "cell", "mobile number"),
        "product" to listOf("product", "product interest", "requirement", "service", "item", "product type"),
        "message" to listOf("message", "inquiry", "details", "description", "notes", "query", "remarks"),
        "company" to listOf("company", "firm", "organization", "business name", "business"),
        "city" to listOf("city", "location", "area", "region", "place")
    )

    fun parseExcel(context: Context, uri: Uri): ParseResult {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ParseResult(
            emptyList(), listOf(ParseError(0, "Cannot open file")), 0
        )

        val fileName = getFileName(context, uri)
        val leads = mutableListOf<Lead>()
        val errors = mutableListOf<ParseError>()
        var totalRows = 0

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
                columnAliases["name"]?.any { normalizedHeader.contains(it) } == true -> nameCol = index
                columnAliases["email"]?.any { normalizedHeader.contains(it) } == true -> emailCol = index
                columnAliases["phone"]?.any { normalizedHeader.contains(it) } == true -> phoneCol = index
                columnAliases["product"]?.any { normalizedHeader.contains(it) } == true -> productCol = index
                columnAliases["message"]?.any { normalizedHeader.contains(it) } == true -> messageCol = index
                columnAliases["company"]?.any { normalizedHeader.contains(it) } == true -> companyCol = index
                columnAliases["city"]?.any { normalizedHeader.contains(it) } == true -> cityCol = index
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
        val name = cells.getOrNull(mapping.nameColumn)?.trim() ?: ""
        val email = cells.getOrNull(mapping.emailColumn)?.trim() ?: ""
        val phone = cells.getOrNull(mapping.phoneColumn)?.trim() ?: ""

        if (name.isBlank() && email.isBlank() && phone.isBlank()) {
            return null
        }

        val productInterest = cells.getOrNull(mapping.productColumn)?.trim()?.let { product ->
            if (product.isNotBlank()) listOf(product) else emptyList()
        } ?: emptyList()

        return Lead(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Unknown" },
            email = email,
            phone = phone,
            company = cells.getOrNull(mapping.companyColumn)?.trim() ?: "",
            productInterest = productInterest,
            message = cells.getOrNull(mapping.messageColumn)?.trim() ?: "",
            city = cells.getOrNull(mapping.cityColumn)?.trim() ?: "",
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
                    cell.numericCellValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try {
                cell.stringCellValue
            } catch (e: Exception) {
                try {
                    cell.numericCellValue.toString()
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
