package com.socialmedia.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "followers")
public class Follower implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "followId")
    private Integer followId;

    @ManyToOne
    @JoinColumn(name = "followerUserId", nullable = false)
    private User followerUser;

    @ManyToOne
    @JoinColumn(name = "followedUserId", nullable = false)
    private User followedUser;

    @Column(name = "status", length = 20)
    private String status = "pending";

    public Integer getFollowId() { return followId; }
    public void setFollowId(Integer followId) { this.followId = followId; }
    public User getFollowerUser() { return followerUser; }
    public void setFollowerUser(User followerUser) { this.followerUser = followerUser; }
    public User getFollowedUser() { return followedUser; }
    public void setFollowedUser(User followedUser) { this.followedUser = followedUser; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
