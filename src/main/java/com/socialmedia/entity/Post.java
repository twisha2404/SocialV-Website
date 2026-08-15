package com.socialmedia.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "posts")
public class Post implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 300)
    private String mediaUrl;
    
    @Column(nullable = false, length = 300)
    private String caption;
    
    @Column(nullable = false)
    private Boolean blocked;
    
    @Column(nullable = false, length = 50)
    private String type = "post";
    
    @Column(name = "createdAt", insertable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updatedAt", insertable = false, updatable = false)
    private Timestamp updatedAt;

    // Getters and Setters
    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
