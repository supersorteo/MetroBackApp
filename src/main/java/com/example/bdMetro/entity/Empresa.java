package com.example.bdMetro.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

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

    // Colores personalizables del presupuesto
    private String primaryColor;
    private String secondaryColor;
    private String secondaryColor2;
    private String textColor;
    private String tableColor;
    private String tableTextColor;
    private String tableBodyColor;
    private String gradientAngle;
    private String infoBoxColorHex;
    private Double infoBoxOpacity;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Empresa() {
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

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public String getSecondaryColor2() { return secondaryColor2; }
    public void setSecondaryColor2(String secondaryColor2) { this.secondaryColor2 = secondaryColor2; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getTableColor() { return tableColor; }
    public void setTableColor(String tableColor) { this.tableColor = tableColor; }

    public String getTableTextColor() { return tableTextColor; }
    public void setTableTextColor(String tableTextColor) { this.tableTextColor = tableTextColor; }

    public String getTableBodyColor() { return tableBodyColor; }
    public void setTableBodyColor(String tableBodyColor) { this.tableBodyColor = tableBodyColor; }

    public String getGradientAngle() { return gradientAngle; }
    public void setGradientAngle(String gradientAngle) { this.gradientAngle = gradientAngle; }

    public String getInfoBoxColorHex() { return infoBoxColorHex; }
    public void setInfoBoxColorHex(String infoBoxColorHex) { this.infoBoxColorHex = infoBoxColorHex; }

    public Double getInfoBoxOpacity() { return infoBoxOpacity; }
    public void setInfoBoxOpacity(Double infoBoxOpacity) { this.infoBoxOpacity = infoBoxOpacity; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
