package com.example.shiftix.controller.api;

import com.example.shiftix.dto.AttendanceRequest;
import com.example.shiftix.dto.AttendanceResponse;
import com.example.shiftix.dto.LocationResponse;
import com.example.shiftix.entity.AttendanceRecord;
import com.example.shiftix.entity.AttendanceType;
import com.example.shiftix.entity.Location;
import com.example.shiftix.entity.User;
import com.example.shiftix.service.AttendanceService;
import com.example.shiftix.service.LocationService;
import com.example.shiftix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceApiController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@Valid @RequestBody AttendanceRequest request,
                                   Authentication authentication,
                                   HttpServletRequest httpRequest) {
        try {
            User user = (User) authentication.getPrincipal();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            AttendanceRecord record = attendanceService.processAttendance(
                user, request.getLatitude(), request.getLongitude(),
                AttendanceType.CHECK_IN, request.getNotes(),
                deviceInfo, ipAddress
            );

            AttendanceResponse response = new AttendanceResponse(record);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@Valid @RequestBody AttendanceRequest request,
                                    Authentication authentication,
                                    HttpServletRequest httpRequest) {
        try {
            User user = (User) authentication.getPrincipal();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            AttendanceRecord record = attendanceService.processAttendance(
                user, request.getLatitude(), request.getLongitude(),
                AttendanceType.CHECK_OUT, request.getNotes(),
                deviceInfo, ipAddress
            );

            AttendanceResponse response = new AttendanceResponse(record);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/break/start")
    public ResponseEntity<?> startBreak(@Valid @RequestBody AttendanceRequest request,
                                      Authentication authentication,
                                      HttpServletRequest httpRequest) {
        try {
            User user = (User) authentication.getPrincipal();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            AttendanceRecord record = attendanceService.processAttendance(
                user, request.getLatitude(), request.getLongitude(),
                AttendanceType.BREAK_START, request.getNotes(),
                deviceInfo, ipAddress
            );

            AttendanceResponse response = new AttendanceResponse(record);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/break/end")
    public ResponseEntity<?> endBreak(@Valid @RequestBody AttendanceRequest request,
                                    Authentication authentication,
                                    HttpServletRequest httpRequest) {
        try {
            User user = (User) authentication.getPrincipal();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            AttendanceRecord record = attendanceService.processAttendance(
                user, request.getLatitude(), request.getLongitude(),
                AttendanceType.BREAK_END, request.getNotes(),
                deviceInfo, ipAddress
            );

            AttendanceResponse response = new AttendanceResponse(record);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String currentStatus = attendanceService.getCurrentStatus(user);

        StatusResponse response = new StatusResponse();
        response.setStatus(currentStatus);
        response.setCanCheckIn(attendanceService.canPerformAction(user, AttendanceType.CHECK_IN));
        response.setCanCheckOut(attendanceService.canPerformAction(user, AttendanceType.CHECK_OUT));
        response.setCanStartBreak(attendanceService.canPerformAction(user, AttendanceType.BREAK_START));
        response.setCanEndBreak(attendanceService.canPerformAction(user, AttendanceType.BREAK_END));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceResponse>> getHistory(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<AttendanceRecord> records = attendanceService.getUserAttendanceHistory(user);

        List<AttendanceResponse> responses = records.stream()
            .map(AttendanceResponse::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponse>> getActiveLocations() {
        List<Location> locations = locationService.findAllActiveLocations();

        List<LocationResponse> responses = locations.stream()
            .map(LocationResponse::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/validate-location")
    public ResponseEntity<LocationValidationResponse> validateLocation(
            @RequestBody LocationValidationRequest request) {

        List<Location> nearbyLocations = locationService.findAllActiveLocations().stream()
            .filter(location -> locationService.isWithinAllowedRadius(
                request.getLatitude(), request.getLongitude(), location))
            .collect(Collectors.toList());

        LocationValidationResponse response = new LocationValidationResponse();
        response.setValid(!nearbyLocations.isEmpty());
        response.setNearbyLocations(nearbyLocations.stream()
            .map(LocationResponse::new)
            .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    // Inner classes for API responses
    public static class StatusResponse {
        private String status;
        private boolean canCheckIn;
        private boolean canCheckOut;
        private boolean canStartBreak;
        private boolean canEndBreak;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isCanCheckIn() { return canCheckIn; }
        public void setCanCheckIn(boolean canCheckIn) { this.canCheckIn = canCheckIn; }
        public boolean isCanCheckOut() { return canCheckOut; }
        public void setCanCheckOut(boolean canCheckOut) { this.canCheckOut = canCheckOut; }
        public boolean isCanStartBreak() { return canStartBreak; }
        public void setCanStartBreak(boolean canStartBreak) { this.canStartBreak = canStartBreak; }
        public boolean isCanEndBreak() { return canEndBreak; }
        public void setCanEndBreak(boolean canEndBreak) { this.canEndBreak = canEndBreak; }
    }

    public static class ErrorResponse {
        private String message;
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class LocationValidationRequest {
        private double latitude;
        private double longitude;

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }

    public static class LocationValidationResponse {
        private boolean valid;
        private List<LocationResponse> nearbyLocations;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<LocationResponse> getNearbyLocations() { return nearbyLocations; }
        public void setNearbyLocations(List<LocationResponse> nearbyLocations) { this.nearbyLocations = nearbyLocations; }
    }
}
