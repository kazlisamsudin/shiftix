package com.example.shiftix.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double distanceFromLocation; // Distance in meters

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean isValidLocation = true;

    // For photo verification (optional)
    private String photoPath;

    // Device information
    private String deviceInfo;

    // IP Address for additional security
    private String ipAddress;

    // Constructors
    public AttendanceRecord() {}

    public AttendanceRecord(User user, Location location, AttendanceType type,
                          Double latitude, Double longitude, Double distanceFromLocation) {
        this.user = user;
        this.location = location;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromLocation = distanceFromLocation;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public AttendanceType getType() {
        return type;
    }

    public void setType(AttendanceType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceFromLocation() {
        return distanceFromLocation;
    }

    public void setDistanceFromLocation(Double distanceFromLocation) {
        this.distanceFromLocation = distanceFromLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isValidLocation() {
        return isValidLocation;
    }

    public void setValidLocation(boolean validLocation) {
        isValidLocation = validLocation;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
