package com.example.shiftix.service;

import com.example.shiftix.entity.Location;
import com.example.shiftix.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public List<Location> findAllActiveLocations() {
        return locationRepository.findByActiveTrue();
    }

    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> searchLocationsByName(String name) {
        return locationRepository.findByNameContainingIgnoreCase(name);
    }

    public Location updateLocation(Location location) {
        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }

    public void deactivateLocation(Long id) {
        Optional<Location> location = locationRepository.findById(id);
        if (location.isPresent()) {
            location.get().setActive(false);
            locationRepository.save(location.get());
        }
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in meters
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Check if user's current location is within allowed radius of a registered location
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param location Registered location
     * @return true if within allowed radius
     */
    public boolean isWithinAllowedRadius(double userLat, double userLon, Location location) {
        double distance = calculateDistance(userLat, userLon, location.getLatitude(), location.getLongitude());
        return distance <= location.getRadiusInMeters();
    }

    /**
     * Find the nearest location to user's current position
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @return Nearest location if found within reasonable distance
     */
    public Optional<Location> findNearestLocation(double userLat, double userLon) {
        List<Location> activeLocations = findAllActiveLocations();
        Location nearestLocation = null;
        double minDistance = Double.MAX_VALUE;

        for (Location location : activeLocations) {
            double distance = calculateDistance(userLat, userLon, location.getLatitude(), location.getLongitude());
            if (distance < minDistance && distance <= location.getRadiusInMeters()) {
                minDistance = distance;
                nearestLocation = location;
            }
        }

        return Optional.ofNullable(nearestLocation);
    }
}
