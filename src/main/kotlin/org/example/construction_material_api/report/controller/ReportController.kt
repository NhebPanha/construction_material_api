package org.example.construction_material_api.report.controller

import org.example.construction_material_api.report.dto.*
import org.example.construction_material_api.report.service.*

import org.example.construction_material_api.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(private val reportService: ReportService) {

    @GetMapping
    fun report(@RequestParam(required = false) range: String?): ApiResponse<RangeReportResponse> =
        ApiResponse.ok(reportService.rangeReport(range), "Report generated")

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<ReportDashboardResponse> =
        ApiResponse.ok(reportService.dashboard(), "Dashboard summary")
}
