package com.example.shiftix.config;

import com.example.shiftix.entity.Location;
import com.example.shiftix.entity.Role;
import com.example.shiftix.entity.User;
import com.example.shiftix.service.LocationService;
import com.example.shiftix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@shiftix.com");
            admin.setPassword("admin123");
            admin.setFullName("System Administrator");
            admin.setPhoneNumber("+1234567890");
            admin.setRole(Role.ADMIN);
            userService.createUser(admin);
            System.out.println("Default admin user created: admin/admin123");
        }

        // Create sample employee user
        if (!userService.existsByUsername("employee1")) {
            User employee = new User();
            employee.setUsername("employee1");
            employee.setEmail("employee1@shiftix.com");
            employee.setPassword("employee123");
            employee.setFullName("John Doe");
            employee.setPhoneNumber("+1234567891");
            employee.setRole(Role.EMPLOYEE);
            userService.createUser(employee);
            System.out.println("Sample employee user created: employee1/employee123");
        }

        // Create sample manager user
        if (!userService.existsByUsername("manager1")) {
            User manager = new User();
            manager.setUsername("manager1");
            manager.setEmail("manager1@shiftix.com");
            manager.setPassword("manager123");
            manager.setFullName("Jane Smith");
            manager.setPhoneNumber("+1234567892");
            manager.setRole(Role.MANAGER);
            userService.createUser(manager);
            System.out.println("Sample manager user created: manager1/manager123");
        }

        // Create sample locations
        if (locationService.findAllLocations().isEmpty()) {
            // Office Location
            Location office = new Location();
            office.setName("Main Office");
            office.setAddress("123 Business Street, City, State 12345");
            office.setLatitude(40.7128); // Example coordinates (New York)
            office.setLongitude(-74.0060);
            office.setRadiusInMeters(100.0);
            office.setDescription("Main office building with 5 floors");
            locationService.createLocation(office);

            // Warehouse Location
            Location warehouse = new Location();
            warehouse.setName("Warehouse A");
            warehouse.setAddress("456 Industrial Ave, City, State 12345");
            warehouse.setLatitude(40.7589); // Example coordinates
            warehouse.setLongitude(-73.9851);
            warehouse.setRadiusInMeters(150.0);
            warehouse.setDescription("Primary storage and distribution center");
            locationService.createLocation(warehouse);

            // Remote Site
            Location remoteSite = new Location();
            remoteSite.setName("Client Site - ABC Corp");
            remoteSite.setAddress("789 Client Plaza, City, State 12345");
            remoteSite.setLatitude(40.7831); // Example coordinates
            remoteSite.setLongitude(-73.9712);
            remoteSite.setRadiusInMeters(75.0);
            remoteSite.setDescription("Client location for project work");
            locationService.createLocation(remoteSite);

            System.out.println("Sample locations created");
        }
    }
}
