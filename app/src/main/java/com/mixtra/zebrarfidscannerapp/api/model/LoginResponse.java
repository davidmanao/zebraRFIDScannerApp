package com.mixtra.zebrarfidscannerapp.api.model;

import java.util.List;

public class LoginResponse {
    private String token;
    private UserData data;
    private String message;
    
    public static class UserData {
        private int id;
        private int roleID;
        private String roleName;
        private String fullName;
        private String username;
        private String email;
        private String password;
        private int organizationID;
        private String connString;
        private boolean isAdmin;
        private Boolean isDeleted;
        private boolean isActive;
        private String firebaseToken;
        private String expiredSessionTime;
        private List<OrganizationMember> organizationMembers;
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getRoleID() { return roleID; }
        public void setRoleID(int roleID) { this.roleID = roleID; }
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public int getOrganizationID() { return organizationID; }
        public void setOrganizationID(int organizationID) { this.organizationID = organizationID; }
        
        public String getConnString() { return connString; }
        public void setConnString(String connString) { this.connString = connString; }
        
        public boolean isAdmin() { return isAdmin; }
        public void setAdmin(boolean admin) { isAdmin = admin; }
        
        public Boolean getDeleted() { return isDeleted; }
        public void setDeleted(Boolean deleted) { isDeleted = deleted; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public String getFirebaseToken() { return firebaseToken; }
        public void setFirebaseToken(String firebaseToken) { this.firebaseToken = firebaseToken; }
        
        public String getExpiredSessionTime() { return expiredSessionTime; }
        public void setExpiredSessionTime(String expiredSessionTime) { this.expiredSessionTime = expiredSessionTime; }
        
        public List<OrganizationMember> getOrganizationMembers() { return organizationMembers; }
        public void setOrganizationMembers(List<OrganizationMember> organizationMembers) { this.organizationMembers = organizationMembers; }
    }
    
    public static class OrganizationMember {
        private int id;
        private String name;
        private String status;
        private boolean isAdmin;
        private int organizationID;
        private String dbServer;
        private String userName;
        private Integer userID;
        private Integer roleID;
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public boolean isAdmin() { return isAdmin; }
        public void setAdmin(boolean admin) { isAdmin = admin; }
        
        public int getOrganizationID() { return organizationID; }
        public void setOrganizationID(int organizationID) { this.organizationID = organizationID; }
        
        public String getDbServer() { return dbServer; }
        public void setDbServer(String dbServer) { this.dbServer = dbServer; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public Integer getUserID() { return userID; }
        public void setUserID(Integer userID) { this.userID = userID; }
        
        public Integer getRoleID() { return roleID; }
        public void setRoleID(Integer roleID) { this.roleID = roleID; }
    }
    
    // Main class getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public UserData getData() { return data; }
    public void setData(UserData data) { this.data = data; }
    
    public String getResultMsg() { return message; }
    public void setResultMsg(String resultMsg) { this.message = resultMsg; }
}