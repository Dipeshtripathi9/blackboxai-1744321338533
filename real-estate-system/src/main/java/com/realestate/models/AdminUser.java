package com.realestate.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Concrete implementation of User for administrative users.
 * Includes specific attributes and behaviors for system administrators.
 */
public class AdminUser extends User {
    // Admin-specific constants
    private static final int ADMIN_ACCESS_LEVEL = 100;
    
    // Admin-specific attributes
    private String department;
    private String role;
    private Set<String> permissions;
    private boolean isSuperAdmin;

    public AdminUser(String firstName, String lastName, String email, String phoneNumber,
                    String passwordHash, String department, String role, boolean isSuperAdmin) {
        super(firstName, lastName, email, phoneNumber, passwordHash, ADMIN_ACCESS_LEVEL);
        this.department = department;
        this.role = role;
        this.isSuperAdmin = isSuperAdmin;
        this.permissions = new HashSet<>();
        initializePermissions();
    }

    private void initializePermissions() {
        // Basic admin permissions
        permissions.add("CREATE_PROPERTY");
        permissions.add("EDIT_PROPERTY");
        permissions.add("DELETE_PROPERTY");
        permissions.add("VIEW_ANALYTICS");
        permissions.add("MANAGE_USERS");
        
        // Super admin gets additional permissions
        if (isSuperAdmin) {
            permissions.add("MANAGE_ADMINS");
            permissions.add("SYSTEM_CONFIGURATION");
            permissions.add("VIEW_AUDIT_LOGS");
            permissions.add("MANAGE_PERMISSIONS");
        }
    }

    @Override
    public String getUserType() {
        return isSuperAdmin ? "Super Admin" : "Admin";
    }

    @Override
    public boolean hasPermission(String operation) {
        // Super admins have all permissions
        if (isSuperAdmin) {
            return true;
        }
        // Check specific permission
        return permissions.contains(operation);
    }

    @Override
    public boolean validateUser() {
        return getEmail() != null &&
               !getEmail().trim().isEmpty() &&
               department != null &&
               !department.trim().isEmpty() &&
               role != null &&
               !role.trim().isEmpty();
    }

    // Admin-specific methods
    public boolean addPermission(String permission) {
        if (!isSuperAdmin) {
            return permissions.add(permission);
        }
        return false; // Super admins already have all permissions
    }

    public boolean removePermission(String permission) {
        if (!isSuperAdmin) {
            return permissions.remove(permission);
        }
        return false; // Can't remove permissions from super admin
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions); // Return a copy to maintain encapsulation
    }

    // Getters and Setters for admin-specific attributes
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty");
        }
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        this.role = role;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    // Only allow promoting to super admin, never demoting
    public void promoteTosuperAdmin() {
        if (!this.isSuperAdmin) {
            this.isSuperAdmin = true;
            initializePermissions(); // Reinitialize permissions for super admin
        }
    }

    @Override
    public String toString() {
        return String.format("%s, Admin{department='%s', role='%s', isSuperAdmin=%b, permissions=%s}",
            super.toString(), department, role, isSuperAdmin, permissions);
    }
}
