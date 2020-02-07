package com.example.faceregistration;

import java.io.Serializable;

public class Privacy implements Serializable {
    String name;
    String birthdate;
    String email;
    String gender;

    public Privacy(){

    }
    public Privacy(String name, String birthdate, String email, String gender){
        this.name = name;
        this.birthdate = birthdate;
        this.email = email;
        this.gender = gender;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getBirthdate(){
        return birthdate;
    }
    public void setBirthdate(String birthdate){
        this.birthdate = birthdate;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getGender(){
        return gender;
    }
    public void setGender(String gender){
        this.gender = gender;
    }


}
