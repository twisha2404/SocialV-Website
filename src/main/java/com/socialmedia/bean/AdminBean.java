package com.socialmedia.bean;

import com.socialmedia.entity.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AdminDaoBean adminDao;

    @Inject
    private LoginBean loginBean;

    private List<User> users;
    private List<Post> posts;
    private List<Comment> comments;
    private List<Like> likes;

    private String activeTab = "users"; // Default tab: users, posts, comments, likes

    // Backing fields for user editing modal
    private User editUser = new User();

    @PostConstruct
    public void init() {
        if (!isAdmin()) return;
        refreshData();
    }

    public void checkAdmin() throws java.io.IOException {
        if (!isAdmin()) {
            jakarta.faces.context.FacesContext.getCurrentInstance()
                .getExternalContext().redirect("index.xhtml");
        }
    }

    public boolean isAdmin() {
        if (!loginBean.isLoggedIn()) return false;
        User current = loginBean.getCurrentUser();
        return current.getGroupmaster() != null && current.getGroupmaster().getGroupmasterId() == 1;
    }

    public void refreshData() {
        users = adminDao.getAllUsers();
        posts = adminDao.getAllPosts();
        comments = adminDao.getAllComments();
        likes = adminDao.getAllLikes();
    }

    public void switchTab(String tabName) {
        this.activeTab = tabName;
    }

    public void prepareEditUser(User u) {
        this.editUser = new User();
        this.editUser.setUserId(u.getUserId());
        this.editUser.setUsername(u.getUsername());
        this.editUser.setFullName(u.getFullName());
        this.editUser.setProfilePic(u.getProfilePic());
    }

    public void saveUserEdit() {
        if (editUser != null) {
            adminDao.updateUser(editUser);
            refreshData();
        }
    }

    public void toggleBlockUser(Integer userId) {
        adminDao.toggleBlockUser(userId);
        refreshData();
    }

    public void toggleBlockPost(Integer postId) {
        adminDao.toggleBlockPost(postId);
        refreshData();
    }

    public void deleteUser(Integer userId) {
        adminDao.deleteUser(userId);
        refreshData();
    }

    public void deletePost(Integer postId) {
        adminDao.deletePost(postId);
        refreshData();
    }

    public void deleteComment(Integer commentId) {
        adminDao.deleteComment(commentId);
        refreshData();
    }

    public void deleteLike(Integer likeId) {
        adminDao.deleteLike(likeId);
        refreshData();
    }

    // Getters and Setters
    public List<User> getUsers() { return users; }
    public List<Post> getPosts() { return posts; }
    public List<Comment> getComments() { return comments; }
    public List<Like> getLikes() { return likes; }
    public String getActiveTab() { return activeTab; }
    public void setActiveTab(String activeTab) { this.activeTab = activeTab; }
    public User getEditUser() { return editUser; }
    public void setEditUser(User editUser) { this.editUser = editUser; }
}
