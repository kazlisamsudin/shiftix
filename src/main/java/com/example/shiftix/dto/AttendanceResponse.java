package com.example.shiftix.dto;

import com.example.shiftix.entity.AttendanceRecord;

import java.time.LocalDateTime;

public class AttendanceResponse {
    private Long id;
    private String userName;
    private String locationName;
    private String type;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;
    private Double distanceFromLocation;
    private String notes;
    private boolean validLocation;

    public AttendanceResponse() {}

    public AttendanceResponse(AttendanceRecord record) {
        this.id = record.getId();
        this.userName = record.getUser().getFullName();
        this.locationName = record.getLocation().getName();
        this.type = record.getType().name();
        this.timestamp = record.getTimestamp();
        this.latitude = record.getLatitude();
        this.longitude = record.getLongitude();
        this.distanceFromLocation = record.getDistanceFromLocation();
        this.notes = record.getNotes();
        this.validLocation = record.isValidLocation();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistanceFromLocation() { return distanceFromLocation; }
    public void setDistanceFromLocation(Double distanceFromLocation) { this.distanceFromLocation = distanceFromLocation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isValidLocation() { return validLocation; }
    public void setValidLocation(boolean validLocation) { this.validLocation = validLocation; }
}
