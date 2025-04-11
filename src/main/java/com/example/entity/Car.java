package com.example.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "cars")
public class Car {
    
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long Id;
    
    private String manufactor;
    
    private String model;
    
    private int year;

    private String status;

    public String getStatus(String done) {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Car(String manufactor, String model, int year, String status) {
        this.manufactor = manufactor;
        this.model = model;
        this.year = year;
        this.status = status;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufactor() {
        return manufactor;
    }

    public void setManufactor(String manufactor) {
        this.manufactor = manufactor;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Car(Long id, String manufactor, String model, int year) {
        Id = id;
        this.manufactor = manufactor;
        this.model = model;
        this.year = year;
    }

    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" +
                "Id=" + Id +
                ", manufactor='" + manufactor + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                '}';
    }
}
