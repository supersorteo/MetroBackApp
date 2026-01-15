package com.example.bdMetro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String description;
    @Column(length = 500)
    private String logoUrl;
    private String userCode;

    private String website;

    private String tiktok;

    private String instagram;

    private String facebook;

    private String cuilCuit;

    public Empresa() {
    }

    public Empresa(Long id, String name, String phone, String email, String description, String logoUrl, String userCode, String website, String tiktok, String instagram, String facebook, String cuilCuit) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.description = description;
        this.logoUrl = logoUrl;
        this.userCode = userCode;
        this.website = website;
        this.tiktok = tiktok;
        this.instagram = instagram;
        this.facebook = facebook;
        this.cuilCuit = cuilCuit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTiktok() {
        return tiktok;
    }

    public void setTiktok(String tiktok) {
        this.tiktok = tiktok;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getCuilCuit() {
        return cuilCuit;
    }

    public void setCuilCuit(String cuilCuit) {
        this.cuilCuit = cuilCuit;
    }
}
