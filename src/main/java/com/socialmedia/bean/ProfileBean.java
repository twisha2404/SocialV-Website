package com.socialmedia.bean;

import com.socialmedia.entity.Follower;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class ProfileBean implements Serializable {

    @Inject
    private UserDaoBean userDao;
    @Inject
    private PostDaoBean postDao;
    @Inject
    private FollowerDaoBean followerDao;
    @Inject
    private LoginBean loginBean;

    private User profileUser;
    private long followersCount;
    private long followingCount;
    private String followStatus; // "none", "pending", "accepted"
    private List<PostViewModel> userPosts;
    private List<Follower> pendingRequests;
    private List<User> followersList;
    private List<User> followingList;

    // Post edit properties
    private Integer editPostId;
    private String editCaption;
    private String editMediaUrl;

    // Profile edit properties
    private String editFullName;
    private String editProfilePic;
    private String editBio;
    private String editPhone;
    private java.util.Date editDob;
    private String editGender;
    private jakarta.servlet.http.Part profilePicFile;

    @PostConstruct
    public void init() {
        if (!loginBean.isLoggedIn()) return;
        
        String userIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("userId");
        Integer uId = loginBean.getCurrentUser().getUserId();
        if (userIdParam != null && !userIdParam.isEmpty()) {
            uId = Integer.parseInt(userIdParam);
        }
        
        profileUser = userDao.findById(uId);
        followersCount = followerDao.getFollowersCount(uId);
        followingCount = followerDao.getFollowingCount(uId);
        followersList = followerDao.getFollowers(uId);
        followingList = followerDao.getFollowing(uId);
        
        if (!isMyProfile()) {
            followStatus = followerDao.getFollowStatus(loginBean.getCurrentUser().getUserId(), uId);
        } else {
            pendingRequests = followerDao.getPendingRequests(uId);
            editFullName = profileUser.getFullName();
            editProfilePic = profileUser.getProfilePic();
            editBio = profileUser.getBio();
            editPhone = profileUser.getPhone();
            editDob = profileUser.getDob();
            editGender = profileUser.getGender();
        }
        
        // Load posts
        List<Post> rawPosts = postDao.getPostsByUser(uId);
        userPosts = new ArrayList<>();
        for (Post p : rawPosts) {
            userPosts.add(new PostViewModel(p, 0, false, null)); 
        }
    }

    public void requestFollow() {
        if (loginBean.isLoggedIn() && profileUser != null) {
            followerDao.requestFollow(loginBean.getCurrentUser().getUserId(), profileUser.getUserId());
            init(); // refresh counts & status
        }
    }

    public void acceptRequest(Integer followerId) {
        followerDao.acceptRequest(followerId, loginBean.getCurrentUser().getUserId());
        init();
    }

    public void declineRequest(Integer followerId) {
        followerDao.declineRequest(followerId, loginBean.getCurrentUser().getUserId());
        init();
    }

    // Getters
    public User getProfileUser() { return profileUser; }
    public long getFollowersCount() { return followersCount; }
    public long getFollowingCount() { return followingCount; }
    public String getFollowStatus() { return followStatus; }
    public List<PostViewModel> getUserPosts() { return userPosts; }
    public List<Follower> getPendingRequests() { return pendingRequests; }
    public List<User> getFollowersList() { return followersList; }
    public List<User> getFollowingList() { return followingList; }
    public boolean isMyProfile() { return loginBean.isLoggedIn() && loginBean.getCurrentUser().getUserId().equals(profileUser.getUserId()); }

    // Actions for updating and deleting posts
    public String updatePost() {
        if (loginBean.isLoggedIn() && editPostId != null) {
            Post p = postDao.find(editPostId);
            if (p != null && p.getUser().getUserId().equals(loginBean.getCurrentUser().getUserId())) {
                p.setCaption(editCaption != null ? editCaption : "");
                p.setMediaUrl(editMediaUrl != null ? editMediaUrl : "");
                postDao.update(p);
            }
        }
        editPostId = null;
        editCaption = null;
        editMediaUrl = null;
        init();
        return "profile?faces-redirect=true" + (isMyProfile() ? "" : "&userId=" + profileUser.getUserId());
    }

    public String deletePost() {
        if (loginBean.isLoggedIn() && editPostId != null) {
            Post p = postDao.find(editPostId);
            if (p != null && p.getUser().getUserId().equals(loginBean.getCurrentUser().getUserId())) {
                postDao.delete(editPostId);
            }
        }
        editPostId = null;
        editCaption = null;
        editMediaUrl = null;
        init();
        return "profile?faces-redirect=true" + (isMyProfile() ? "" : "&userId=" + profileUser.getUserId());
    }

    public String updateProfile() {
        if (loginBean.isLoggedIn() && isMyProfile()) {
            User currentUser = loginBean.getCurrentUser();
            currentUser.setFullName(editFullName);
            
            boolean hasUpload = (profilePicFile != null && profilePicFile.getSize() > 0);
            if (hasUpload) {
                try {
                    String absolutePath = jakarta.faces.context.FacesContext.getCurrentInstance()
                        .getExternalContext().getRealPath("/");
                    if (absolutePath == null) {
                        absolutePath = "";
                    }
                    String targetDirToken = "target" + java.io.File.separator + "social-media-app";
                    String srcDirToken = "src" + java.io.File.separator + "main" + java.io.File.separator + "webapp";
                    String srcPath = absolutePath.replace(targetDirToken, srcDirToken);

                    if (!absolutePath.endsWith(java.io.File.separator)) {
                        absolutePath += java.io.File.separator;
                    }
                    if (!srcPath.endsWith(java.io.File.separator)) {
                        srcPath += java.io.File.separator;
                    }
                    
                    String uploadDir = absolutePath + "uploads";
                    java.io.File dir = new java.io.File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    String originalName = getFileName(profilePicFile);
                    String fileName = System.currentTimeMillis() + "_" + originalName;
                    
                    java.nio.file.Path targetPath = java.nio.file.Paths.get(uploadDir, fileName);
                    try (java.io.InputStream input = profilePicFile.getInputStream()) {
                        java.nio.file.Files.copy(input, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    try {
                        if (!srcPath.equals(absolutePath)) {
                            java.io.File srcDir = new java.io.File(srcPath + "uploads");
                            if (!srcDir.exists()) {
                                srcDir.mkdirs();
                            }
                            java.nio.file.Files.copy(
                                java.nio.file.Paths.get(uploadDir + java.io.File.separator + fileName),
                                java.nio.file.Paths.get(srcPath + "uploads" + java.io.File.separator + fileName),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );
                        }
                    } catch (Exception ex) {}
                    
                    currentUser.setProfilePic("uploads/" + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    currentUser.setProfilePic(editProfilePic);
                }
            } else {
                currentUser.setProfilePic(editProfilePic);
            }
            
            currentUser.setBio(editBio);
            currentUser.setPhone(editPhone);
            currentUser.setDob(editDob);
            currentUser.setGender(editGender);
            userDao.update(currentUser);
            profileUser = currentUser;
        }
        profilePicFile = null;
        return "profile?faces-redirect=true";
    }

    private String getFileName(jakarta.servlet.http.Part part) {
        try {
            return part.getSubmittedFileName();
        } catch (Exception e) {
            String contentDisp = part.getHeader("content-disposition");
            String[] tokens = contentDisp.split(";");
            for (String token : tokens) {
                if (token.trim().startsWith("filename")) {
                    String fn = token.substring(token.indexOf("=") + 1).trim();
                    if (fn.startsWith("\"") && fn.endsWith("\"")) {
                        return fn.substring(1, fn.length() - 1);
                    }
                    return fn;
                }
            }
        }
        return "file";
    }

    public Integer getEditPostId() { return editPostId; }
    public void setEditPostId(Integer editPostId) { this.editPostId = editPostId; }
    public String getEditCaption() { return editCaption; }
    public void setEditCaption(String editCaption) { this.editCaption = editCaption; }
    public String getEditMediaUrl() { return editMediaUrl; }
    public void setEditMediaUrl(String editMediaUrl) { this.editMediaUrl = editMediaUrl; }
    public String getEditFullName() { return editFullName; }
    public void setEditFullName(String editFullName) { this.editFullName = editFullName; }
    public String getEditProfilePic() { return editProfilePic; }
    public void setEditProfilePic(String editProfilePic) { this.editProfilePic = editProfilePic; }
    public String getEditBio() { return editBio; }
    public void setEditBio(String editBio) { this.editBio = editBio; }
    public String getEditPhone() { return editPhone; }
    public void setEditPhone(String editPhone) { this.editPhone = editPhone; }
    public java.util.Date getEditDob() { return editDob; }
    public void setEditDob(java.util.Date editDob) { this.editDob = editDob; }
    public String getEditGender() { return editGender; }
    public void setEditGender(String editGender) { this.editGender = editGender; }
    public jakarta.servlet.http.Part getProfilePicFile() { return profilePicFile; }
    public void setProfilePicFile(jakarta.servlet.http.Part profilePicFile) { this.profilePicFile = profilePicFile; }
}
