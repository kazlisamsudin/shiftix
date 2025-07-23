package com.example.shiftix.controller;

import com.example.shiftix.entity.AttendanceRecord;
import com.example.shiftix.entity.Location;
import com.example.shiftix.entity.Role;
import com.example.shiftix.entity.User;
import com.example.shiftix.service.AttendanceService;
import com.example.shiftix.service.LocationService;
import com.example.shiftix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.findAllUsers();
        List<Location> locations = locationService.findAllLocations();
        List<AttendanceRecord> todayRecords = attendanceService.getAllAttendanceByDate(LocalDate.now());

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalLocations", locations.size());
        model.addAttribute("todayAttendance", todayRecords.size());
        model.addAttribute("recentRecords", todayRecords.size() > 10 ? todayRecords.subList(0, 10) : todayRecords);

        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String addUser(@Valid @ModelAttribute("user") User user,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/user-form";
        }

        try {
            userService.createUser(user);
            return "redirect:/admin/users?success=User created successfully";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "admin/user-form";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                          @Valid @ModelAttribute("user") User user,
                          @RequestParam(value = "newPassword", required = false) String newPassword,
                          BindingResult result,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/user-edit";
        }

        // Get existing user to preserve password if not changed
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setId(id);

        // Only update password if a new one is provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(newPassword);
            userService.updateUserWithNewPassword(user);
        } else {
            // Keep existing password
            user.setPassword(existingUser.getPassword());
            userService.updateUser(user);
        }

        return "redirect:/admin/users?success=User updated successfully";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            // Check if user exists before deletion
            User userToDelete = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Prevent deletion of the last admin user
            if (userToDelete.getRole() == Role.ADMIN) {
                long adminCount = userService.findAllUsers().stream()
                        .filter(u -> u.getRole() == Role.ADMIN)
                        .count();
                if (adminCount <= 1) {
                    return "redirect:/admin/users?error=Cannot delete the last admin user";
                }
            }

            userService.deleteUser(id);
            return "redirect:/admin/users?success=User deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin/users?error=Failed to delete user: " + e.getMessage();
        }
    }

    // Location Management
    @GetMapping("/locations")
    public String manageLocations(Model model) {
        List<Location> locations = locationService.findAllLocations();
        model.addAttribute("locations", locations);
        return "admin/locations";
    }

    @GetMapping("/locations/new")
    public String showAddLocationForm(Model model) {
        model.addAttribute("location", new Location());
        return "admin/location-form";
    }

    @PostMapping("/locations/new")
    public String addLocation(@Valid @ModelAttribute("location") Location location,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            return "admin/location-form";
        }

        locationService.createLocation(location);
        return "redirect:/admin/locations?success=Location created successfully";
    }

    @GetMapping("/locations/edit/{id}")
    public String showEditLocationForm(@PathVariable Long id, Model model) {
        Location location = locationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        model.addAttribute("location", location);
        return "admin/location-edit";
    }

    @PostMapping("/locations/edit/{id}")
    public String editLocation(@PathVariable Long id,
                              @Valid @ModelAttribute("location") Location location,
                              BindingResult result) {
        if (result.hasErrors()) {
            return "admin/location-edit";
        }

        location.setId(id);
        locationService.updateLocation(location);
        return "redirect:/admin/locations?success=Location updated successfully";
    }

    @PostMapping("/locations/delete/{id}")
    public String deleteLocation(@PathVariable Long id) {
        try {
            // Check if location exists before deletion
            Location locationToDelete = locationService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            locationService.deleteLocation(id);
            return "redirect:/admin/locations?success=Location deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin/locations?error=Failed to delete location: " + e.getMessage();
        }
    }

    // View endpoints for better user experience
    @GetMapping("/users/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/user-view";
    }

    @GetMapping("/locations/view/{id}")
    public String viewLocation(@PathVariable Long id, Model model) {
        Location location = locationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        model.addAttribute("location", location);
        return "admin/location-view";
    }

    // Attendance Reports
    @GetMapping("/reports")
    public String reports(Model model) {
        List<AttendanceRecord> todayRecords = attendanceService.getAllAttendanceByDate(LocalDate.now());
        model.addAttribute("todayRecords", todayRecords);
        return "admin/reports";
    }
}
