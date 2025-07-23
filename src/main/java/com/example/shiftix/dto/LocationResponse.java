package com.example.shiftix.dto;

import com.example.shiftix.entity.Location;

public class LocationResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double radiusInMeters;
    private String description;

    public LocationResponse() {}

    public LocationResponse(Location location) {
        this.id = location.getId();
        this.name = location.getName();
        this.address = location.getAddress();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.radiusInMeters = location.getRadiusInMeters();
        this.description = location.getDescription();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getRadiusInMeters() { return radiusInMeters; }
    public void setRadiusInMeters(Double radiusInMeters) { this.radiusInMeters = radiusInMeters; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
