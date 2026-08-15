package com.socialmedia.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    @Column(nullable = false, length = 200)
    private String username;
    
    @Column(nullable = false, length = 200)
    private String fullName;
    
    @Column(nullable = false, length = 200)
    private String password;
    
    @Column(nullable = false, length = 300)
    private String profilePic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupmasterId", nullable = false)
    private Groupmaster groupmaster;
    
    @Column(nullable = false)
    private Boolean blocked = false;

    @Column(length = 500)
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private java.util.Date dob;

    @Column(length = 10)
    private String gender;
    
    @Column(name = "createdAt", insertable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updatedAt", insertable = false, updatable = false)
    private Timestamp updatedAt;

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public Groupmaster getGroupmaster() { return groupmaster; }
    public void setGroupmaster(Groupmaster groupmaster) { this.groupmaster = groupmaster; }
    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public java.util.Date getDob() { return dob; }
    public void setDob(java.util.Date dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
