package com.socialmedia.bean;

import com.socialmedia.entity.User;

public class UserViewModel {
    private User user;
    private String followStatus; // "none", "pending", "accepted"
    private boolean followsMe; // true if they follow us
    private String incomingStatus; // "none", "pending", "accepted"

    public UserViewModel(User user, String followStatus, boolean followsMe) {
        this.user = user;
        this.followStatus = followStatus;
        this.followsMe = followsMe;
        this.incomingStatus = followsMe ? "accepted" : "none";
    }

    public UserViewModel(User user, String followStatus, boolean followsMe, String incomingStatus) {
        this.user = user;
        this.followStatus = followStatus;
        this.followsMe = followsMe;
        this.incomingStatus = incomingStatus;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFollowStatus() { return followStatus; }
    public void setFollowStatus(String followStatus) { this.followStatus = followStatus; }
    public boolean isFollowsMe() { return followsMe; }
    public void setFollowsMe(boolean followsMe) { this.followsMe = followsMe; }
    public String getIncomingStatus() { return incomingStatus; }
    public void setIncomingStatus(String incomingStatus) { this.incomingStatus = incomingStatus; }
}
