package com.hidirektor.hydraulic.utils.Model.Hydraulic;

public class Motor {
    private String name;
    private String motorYukseklik;

    public Motor(String name, String motorYukseklik) {
        this.name = name;
        this.motorYukseklik = motorYukseklik;
    }

    public String getName() {
        return name;
    }

    public String getMotorYukseklik() {
        return motorYukseklik;
    }

    @Override
    public String toString() {
        return "MotorInfo{name='" + name + "', motorYukseklik='" + motorYukseklik + "'}";
    }
}
