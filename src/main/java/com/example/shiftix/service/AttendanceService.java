package com.example.shiftix.service;

import com.example.shiftix.entity.AttendanceRecord;
import com.example.shiftix.entity.AttendanceType;
import com.example.shiftix.entity.Location;
import com.example.shiftix.entity.User;
import com.example.shiftix.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private LocationService locationService;

    /**
     * Process check-in or check-out with GPS validation
     * @param user The user checking in/out
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @param type Check-in or check-out
     * @param notes Optional notes
     * @param deviceInfo Device information
     * @param ipAddress User's IP address
     * @return AttendanceRecord if successful
     * @throws RuntimeException if validation fails
     */
    public AttendanceRecord processAttendance(User user, double latitude, double longitude,
                                            AttendanceType type, String notes,
                                            String deviceInfo, String ipAddress) {

        // Find nearest valid location
        Optional<Location> nearestLocation = locationService.findNearestLocation(latitude, longitude);

        if (nearestLocation.isEmpty()) {
            throw new RuntimeException("You are not within range of any registered location");
        }

        Location location = nearestLocation.get();
        double distance = locationService.calculateDistance(latitude, longitude,
                                                          location.getLatitude(), location.getLongitude());

        // Validate business rules for check-in/out
        validateAttendanceRules(user, type);

        // Create attendance record
        AttendanceRecord record = new AttendanceRecord(user, location, type, latitude, longitude, distance);
        record.setNotes(notes);
        record.setDeviceInfo(deviceInfo);
        record.setIpAddress(ipAddress);
        record.setValidLocation(distance <= location.getRadiusInMeters());

        return attendanceRecordRepository.save(record);
    }

    /**
     * Validate attendance business rules
     * @param user The user
     * @param type Attendance type
     * @throws RuntimeException if rules are violated
     */
    private void validateAttendanceRules(User user, AttendanceType type) {
        Optional<AttendanceRecord> lastRecord = getLastAttendanceRecord(user);
        LocalDateTime today = LocalDateTime.now();

        switch (type) {
            case CHECK_IN:
                // Check if already checked in today
                if (lastRecord.isPresent() &&
                    lastRecord.get().getType() == AttendanceType.CHECK_IN &&
                    lastRecord.get().getTimestamp().toLocalDate().equals(today.toLocalDate())) {
                    throw new RuntimeException("You have already checked in today");
                }
                break;

            case CHECK_OUT:
                // Must be checked in first
                if (lastRecord.isEmpty() || lastRecord.get().getType() != AttendanceType.CHECK_IN ||
                    !lastRecord.get().getTimestamp().toLocalDate().equals(today.toLocalDate())) {
                    throw new RuntimeException("You must check in first before checking out");
                }
                break;

            case BREAK_START:
                // Must be checked in and not on break
                if (lastRecord.isEmpty() ||
                    (lastRecord.get().getType() != AttendanceType.CHECK_IN &&
                     lastRecord.get().getType() != AttendanceType.BREAK_END) ||
                    !lastRecord.get().getTimestamp().toLocalDate().equals(today.toLocalDate())) {
                    throw new RuntimeException("You must be checked in to start a break");
                }
                break;

            case BREAK_END:
                // Must be on break
                if (lastRecord.isEmpty() || lastRecord.get().getType() != AttendanceType.BREAK_START ||
                    !lastRecord.get().getTimestamp().toLocalDate().equals(today.toLocalDate())) {
                    throw new RuntimeException("You must be on break to end a break");
                }
                break;
        }
    }

    public Optional<AttendanceRecord> getLastAttendanceRecord(User user) {
        return attendanceRecordRepository.findTopByUserOrderByTimestampDesc(user);
    }

    public List<AttendanceRecord> getUserAttendanceHistory(User user) {
        return attendanceRecordRepository.findByUserOrderByTimestampDesc(user);
    }

    public List<AttendanceRecord> getUserAttendanceByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return attendanceRecordRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user, start, end);
    }

    public List<AttendanceRecord> getAllAttendanceByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        return attendanceRecordRepository.findByDate(startOfDay);
    }

    public List<AttendanceRecord> getAllAttendanceByDateRange(LocalDateTime start, LocalDateTime end) {
        return attendanceRecordRepository.findByDateRange(start, end);
    }

    /**
     * Get user's current status based on last attendance record
     * @param user The user
     * @return Current status as string
     */
    public String getCurrentStatus(User user) {
        Optional<AttendanceRecord> lastRecord = getLastAttendanceRecord(user);

        if (lastRecord.isEmpty()) {
            return "Not checked in";
        }

        AttendanceRecord record = lastRecord.get();
        LocalDate today = LocalDate.now();

        if (!record.getTimestamp().toLocalDate().equals(today)) {
            return "Not checked in";
        }

        switch (record.getType()) {
            case CHECK_IN:
                return "Checked in";
            case CHECK_OUT:
                return "Checked out";
            case BREAK_START:
                return "On break";
            case BREAK_END:
                return "Checked in";
            default:
                return "Unknown";
        }
    }

    /**
     * Check if user can perform a specific attendance action
     * @param user The user
     * @param type The attendance type to check
     * @return true if action is allowed
     */
    public boolean canPerformAction(User user, AttendanceType type) {
        try {
            validateAttendanceRules(user, type);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
