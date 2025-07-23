package com.example.shiftix.controller;

import com.example.shiftix.entity.AttendanceRecord;
import com.example.shiftix.entity.AttendanceType;
import com.example.shiftix.entity.User;
import com.example.shiftix.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        
        // Get current status
        String currentStatus = attendanceService.getCurrentStatus(user);
        model.addAttribute("currentStatus", currentStatus);
        
        // Check which actions are available
        model.addAttribute("canCheckIn", attendanceService.canPerformAction(user, AttendanceType.CHECK_IN));
        model.addAttribute("canCheckOut", attendanceService.canPerformAction(user, AttendanceType.CHECK_OUT));
        model.addAttribute("canStartBreak", attendanceService.canPerformAction(user, AttendanceType.BREAK_START));
        model.addAttribute("canEndBreak", attendanceService.canPerformAction(user, AttendanceType.BREAK_END));
        
        // Get recent attendance records
        List<AttendanceRecord> recentRecords = attendanceService.getUserAttendanceHistory(user);
        if (recentRecords.size() > 10) {
            recentRecords = recentRecords.subList(0, 10);
        }
        model.addAttribute("recentRecords", recentRecords);
        
        return "employee/dashboard";
    }

    @GetMapping("/attendance-history")
    public String attendanceHistory(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        
        // Get all attendance records for current user
        List<AttendanceRecord> records = attendanceService.getUserAttendanceHistory(user);
        model.addAttribute("records", records);
        
        return "employee/attendance-history";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        return "employee/profile";
    }
}
