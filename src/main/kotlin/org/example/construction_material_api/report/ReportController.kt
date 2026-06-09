package org.example.construction_material_api.report

import org.example.construction_material_api.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(private val reportService: ReportService) {

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<DashboardResponse> =
        ApiResponse.ok(reportService.dashboard(), "Dashboard summary")
}
