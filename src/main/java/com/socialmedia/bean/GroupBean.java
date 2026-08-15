package com.socialmedia.bean;

import com.socialmedia.entity.Groupmaster;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class GroupBean {
    
    @Inject
    private GroupDaoBean groupDao;
    @Inject
    private LoginBean loginBean;
    @Inject
    private UserDaoBean userDao;
    
    private List<Groupmaster> allGroups;
    private List<com.socialmedia.entity.User> allUsers;
    private String groupName;
    private String groupMediaurl;
    private Integer selectedUserId;

    @PostConstruct
    public void init() {
        if (!loginBean.isLoggedIn()) return;
        allGroups = groupDao.getAllGroups();
        allUsers = userDao.getAllUsersExcept(loginBean.getCurrentUser().getUserId());
    }

    public String createGroup() {
        if (loginBean.isLoggedIn() && groupName != null && !groupName.trim().isEmpty()) {
            groupDao.createGroup(groupName, groupMediaurl, loginBean.getCurrentUser().getUserId());
            groupName = "";
            groupMediaurl = "";
            init();
        }
        return "groups?faces-redirect=true";
    }

    public void addMember(Integer groupId) {
        if (selectedUserId != null) {
            groupDao.addMember(groupId, selectedUserId);
            selectedUserId = null;
        }
    }

    public boolean isMember(Groupmaster group) {
        if (loginBean.isLoggedIn() && group != null && group.getMembers() != null) {
            Integer currentUserId = loginBean.getCurrentUser().getUserId();
            for (com.socialmedia.entity.User u : group.getMembers()) {
                if (u.getUserId().equals(currentUserId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void joinGroup(Integer groupId) {
        if (loginBean.isLoggedIn()) {
            groupDao.addMember(groupId, loginBean.getCurrentUser().getUserId());
            init(); // refresh membership lists
        }
    }

    public List<Groupmaster> getAllGroups() { return allGroups; }
    public List<com.socialmedia.entity.User> getAllUsers() { return allUsers; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getGroupMediaurl() { return groupMediaurl; }
    public void setGroupMediaurl(String groupMediaurl) { this.groupMediaurl = groupMediaurl; }
    public Integer getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Integer selectedUserId) { this.selectedUserId = selectedUserId; }
}
