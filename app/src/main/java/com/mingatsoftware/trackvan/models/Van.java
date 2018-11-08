package com.mingatsoftware.trackvan.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

public class Van  {
    String vanId;
    String model;
    String year;
    String name;
    InUse inUse;

    public Van() { }  // Needed for Firebase

    public Van(String model, String year, String name, InUse inUse){
        this.model = model;
        this.year = year;
        this.name = name;
        this.inUse = inUse;
    }

    // the getters and setters: JavaBean naming patter
    public String getVanId() { return  vanId;}
    public void setVanId(String vanId) { this.vanId = vanId;}

    public String getModel() { return  model;}
    public void setModel(String model) { this.model = model;}

    public String getYear() { return  year;}
    public void setYear(String year) { this.year = year;}

    public String getName() { return  name;}
    public void setName(String name) { this.name = name;}

    public InUse getInUse() { return  inUse; }
    public void setInUse(InUse inUse) { this.inUse = inUse; }

    public static class InUse {
        String user;
        int startMileage;
        Long startTime;
        //Map<String, String> startTime;
        String destination;
        String department;
        Double willReturn;
        int returnMileage;
        Long returnTime;
        //Map<String, String> returnTime;

        public InUse() { }  // Needed for Firebase

        /*
        public InUse(String user, int startMileage, Map<String, String> startTime, String destination, String department, Double willReturn ){
            this.user = user;
            this.startMileage = startMileage;
            this.startTime = startTime;
            this.destination = destination;
            this.department = department;
            this.willReturn = willReturn;
        }
        */


        public InUse(String user, int startMileage, String destination, String department, Double willReturn, int returnMileage ){
            this.user = user;
            this.startMileage = startMileage;
            this.destination = destination;
            this.department = department;
            this.willReturn = willReturn;
            this.returnMileage = returnMileage;
        }

        // the getters and setters: JavaBean naming patter
        public String getUser() { return  user;}
        public void setUser(String user) { this.user = user;}

        public int getStartMileage() { return  startMileage;}
        public void setStartMileage(int startMileage) { this.startMileage = startMileage;}

        public Map<String, String> getStartTime() { return ServerValue.TIMESTAMP;}
        //public void setStartTime(Map<String, String> startTime) { this.startTime = startTime;}

        public String getDestination() { return  destination;}
        public void setDestination(String destination) { this.destination = destination;}

        public String getDepartment() { return  department;}
        public void setDepartment(String department) { this.department = department;}

        public Double getWillReturn() { return  willReturn;}
        public void setWillReturn(Double willReturn) { this.willReturn = willReturn;}

        public int getReturnMileage() { return  returnMileage;}
        public void setReturnMileage(int returnMileage) { this.returnMileage = returnMileage;}

        public Map<String, String> getReturnTime() { return  ServerValue.TIMESTAMP;}
        //public void setReturnTime(Map<String, String> returnTime) { this.returnTime = returnTime;}

        @Exclude
        public Long getStartTimeLong() {
            return startTime;
        }

        @Exclude
        public Long getReturnTimeLong() {
            return returnTime;
        }
    }


}