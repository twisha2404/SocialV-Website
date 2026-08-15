package com.socialmedia.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "groupmaster")
public class Groupmaster implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupmasterId;
    
    @Column(nullable = false, length = 20)
    private String groupName;
    
    @Column(name = "createdAt", insertable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updatedAt", insertable = false, updatable = false)
    private Timestamp updatedAt;

    @Column(length = 300)
    private String mediaUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "groupmembers",
        joinColumns = @JoinColumn(name = "groupId"),
        inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private List<User> members;

    // Getters and Setters
    public Integer getGroupmasterId() { return groupmasterId; }
    public void setGroupmasterId(Integer groupmasterId) { this.groupmasterId = groupmasterId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}
