package com.socialmedia.bean;

import com.socialmedia.entity.Follower;
import com.socialmedia.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class PeopleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UserDaoBean userDao;
    @Inject
    private FollowerDaoBean followerDao;
    @Inject
    private LoginBean loginBean;

    private List<UserViewModel> discoverPeople;
    private List<UserViewModel> followRequests;
    private java.util.Set<Integer> recentlyAcceptedUserIds = new java.util.HashSet<>();
    private String searchQuery;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        if (!loginBean.isLoggedIn()) return;
        Integer currentUserId = loginBean.getCurrentUser().getUserId();
        
        // 1. Populate followRequests list
        followRequests = new ArrayList<>();
        
        // Load pending follow requests
        List<Follower> pending = followerDao.getPendingRequests(currentUserId);
        for (Follower f : pending) {
            User sender = f.getFollowerUser();
            String outgoingStatus = followerDao.getFollowStatus(currentUserId, sender.getUserId());
            followRequests.add(new UserViewModel(sender, outgoingStatus, false, "pending"));
        }
        
        // Load recently accepted requests so they can be followed back from the requests list
        for (Integer acceptedId : recentlyAcceptedUserIds) {
            User sender = userDao.findById(acceptedId);
            if (sender != null) {
                String outgoingStatus = followerDao.getFollowStatus(currentUserId, sender.getUserId());
                followRequests.add(new UserViewModel(sender, outgoingStatus, true, "accepted"));
            }
        }
        
        // 2. Load all other users to discover
        List<User> users = userDao.getAllUsersExcept(currentUserId);
        discoverPeople = new ArrayList<>();
        
        for (User u : users) {
            // Apply search query filter if present
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String q = searchQuery.toLowerCase().trim();
                boolean matches = u.getUsername().toLowerCase().contains(q) || 
                                  u.getFullName().toLowerCase().contains(q);
                if (!matches) {
                    continue;
                }
            }
            
            String followStatus = followerDao.getFollowStatus(currentUserId, u.getUserId());
            String incomingStatus = followerDao.getFollowStatus(u.getUserId(), currentUserId);
            boolean followsMe = "accepted".equals(incomingStatus);
            
            discoverPeople.add(new UserViewModel(u, followStatus, followsMe, incomingStatus));
        }
    }

    public void toggleFollow(Integer targetUserId) {
        if (!loginBean.isLoggedIn()) return;
        followerDao.requestFollow(loginBean.getCurrentUser().getUserId(), targetUserId);
        refresh();
    }

    public void acceptRequest(Integer followerUserId) {
        if (!loginBean.isLoggedIn()) return;
        followerDao.acceptRequest(followerUserId, loginBean.getCurrentUser().getUserId());
        recentlyAcceptedUserIds.add(followerUserId);
        refresh();
    }

    public void declineRequest(Integer followerUserId) {
        if (!loginBean.isLoggedIn()) return;
        followerDao.declineRequest(followerUserId, loginBean.getCurrentUser().getUserId());
        recentlyAcceptedUserIds.remove(followerUserId);
        refresh();
    }

    public void search() {
        refresh();
    }

    // Getters and Setters
    public List<UserViewModel> getDiscoverPeople() { return discoverPeople; }
    public List<UserViewModel> getFollowRequests() { return followRequests; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
}
