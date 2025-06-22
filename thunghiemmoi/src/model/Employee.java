package model;

import java.awt.Image;

public class Employee {
    private String id;
    private String name;
    private String address;
    private String gender;
    private String title;
    private int age;
    private Image photo;

    public Employee() { }

    public Employee(String id, String name, String address, String gender, String title, int age, Image photo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.gender = gender;
        this.title = title;
        this.age = age;
        this.photo = photo;
    }

    // -- ID --
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // -- Name --
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // -- Address --
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    // -- Gender --
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    // -- Title / Position --
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // -- Age --
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    // -- Photo --
    public Image getPhoto() {
        return photo;
    }
    public void setPhoto(Image photo) {
        this.photo = photo;
    }
}