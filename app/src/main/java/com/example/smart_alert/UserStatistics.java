package com.example.smart_alert;

public class UserStatistics {

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDangerEvents() {
        return dangerEvents;
    }

    public void setDangerEvents(int dangerEvents) {
        this.dangerEvents = dangerEvents;
    }

    public String email;
    public int dangerEvents, fireEvent, earthquakeEvent, floodEvent, elseEvent, EmployeeDangerEvents, EmployeeFireEvent, EmployeeEarthEvent, EmployeeFloodEvent, EmployeeElseEvent; // This field will track the number of danger events

    public UserStatistics() {
        // Default constructor required for Firebase
    }
    public UserStatistics(String email) {
        this.email = email;
        // Initialize with zero danger events for user and employee
        this.dangerEvents = 0;
        this.fireEvent = 0;
        this.earthquakeEvent = 0;
        this.floodEvent = 0;
        this.elseEvent = 0;
        this.EmployeeDangerEvents = 0;
        this.EmployeeFireEvent = 0;
        this.EmployeeEarthEvent = 0;
        this.EmployeeFloodEvent = 0;
        this.EmployeeElseEvent = 0;

    }


}
