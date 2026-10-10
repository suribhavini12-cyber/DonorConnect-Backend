package com.donorconnect.controller;

import com.donorconnect.dto.AdminDashboardResponse;
import com.donorconnect.dto.ReportResponse;
import com.donorconnect.model.ReportStatus;
import com.donorconnect.security.JwtAuthenticationFilter;
import com.donorconnect.security.JwtService;
import com.donorconnect.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/admin/dashboard returns admin dashboard metrics")
    void getDashboardStats_Success() throws Exception {
        AdminDashboardResponse resp = new AdminDashboardResponse(10L, 9L, 5L, 4L, 1L, 15L, 8L, 4L, 3L, 2L, 1L, 1L);

        when(adminService.getDashboardStats()).thenReturn(resp);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalRequests").value(15));
    }

    @Test
    @DisplayName("GET /api/admin/reports returns reports list")
    void getAllReports_Success() throws Exception {
        ReportResponse r = new ReportResponse(1L, 10L, "Reporter", 20L, "TargetUser", 30L, "SPAM", "Details", ReportStatus.OPEN, LocalDateTime.now(), null, null, null);

        when(adminService.getAllReports()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reason").value("SPAM"));
    }
}
