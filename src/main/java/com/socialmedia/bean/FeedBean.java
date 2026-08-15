package com.socialmedia.bean;

import com.socialmedia.entity.Post;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class FeedBean {
    
    @Inject
    private PostDaoBean postDao;
    @Inject
    private LikeDaoBean likeDao;
    @Inject
    private CommentDaoBean commentDao;
    @Inject
    private LoginBean loginBean;
    private List<PostViewModel> postViewModels;
    private List<Post> activeStories;
    private String newCaption;
    private String newMediaUrl;
    private String newType = "post"; // 'post', 'reel', 'story'
    private jakarta.servlet.http.Part uploadedFile;
    
    private boolean myStoryActive;
    private Post myActiveStory;

    // Post edit properties
    private Integer editPostId;
    private String editCaption;
    private String editMediaUrl;
    
    @PostConstruct
    public void init() {
        if (!loginBean.isLoggedIn()) return;
        List<Post> rawPosts = postDao.getAllPosts();
        postViewModels = new ArrayList<>();
        Integer currentUserId = loginBean.getCurrentUser().getUserId();
        
        for (Post p : rawPosts) {
            long lCount = likeDao.getLikeCount(p.getPostId());
            boolean lByMe = likeDao.isLikedBy(currentUserId, p.getPostId());
            postViewModels.add(new PostViewModel(p, lCount, lByMe, commentDao.getComments(p.getPostId())));
        }
        
        activeStories = postDao.getActiveStories();
        
        // Check if I have an active story
        myStoryActive = false;
        myActiveStory = null;
        for (Post s : activeStories) {
            if (s.getUser().getUserId().equals(currentUserId)) {
                myStoryActive = true;
                myActiveStory = s;
                break;
            }
        }
    }

    public String createPost() {
        boolean hasUpload = (uploadedFile != null && uploadedFile.getSize() > 0);
        boolean hasContent = (newCaption != null && !newCaption.trim().isEmpty()) || 
                             (newMediaUrl != null && !newMediaUrl.trim().isEmpty()) ||
                             hasUpload;
        if (loginBean.isLoggedIn() && hasContent) {
            Post p = new Post();
            p.setCaption(newCaption != null ? newCaption : "");
            p.setUser(loginBean.getCurrentUser());
            p.setBlocked(false);
            p.setType(newType != null ? newType : "post");

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
                    
                    String originalName = getFileName(uploadedFile);
                    String fileName = System.currentTimeMillis() + "_" + originalName;
                    
                    // Write to target deployment path using InputStream copy (failsafe)
                    java.nio.file.Path targetPath = java.nio.file.Paths.get(uploadDir, fileName);
                    try (java.io.InputStream input = uploadedFile.getInputStream()) {
                        java.nio.file.Files.copy(input, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    // Try writing to source path to persist on clean rebuilds
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
                    } catch (Exception ex) {
                        // ignore source copy errors
                    }
                    
                    p.setMediaUrl("uploads/" + fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    p.setMediaUrl(newMediaUrl != null ? newMediaUrl : "");
                }
            } else {
                p.setMediaUrl(newMediaUrl != null ? newMediaUrl : "");
            }

            postDao.create(p);
            newCaption = "";
            newMediaUrl = "";
            newType = "post";
            uploadedFile = null;
            init();
        }
        return "feed?faces-redirect=true";
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

    public void toggleLike(Integer postId) {
        if (loginBean.isLoggedIn()) {
            likeDao.toggleLike(loginBean.getCurrentUser().getUserId(), postId);
            init(); // refresh
        }
    }

    public void addComment(PostViewModel vm) {
        if (loginBean.isLoggedIn() && vm.getNewComment() != null && !vm.getNewComment().isEmpty()) {
            commentDao.addComment(loginBean.getCurrentUser().getUserId(), vm.getPost().getPostId(), vm.getNewComment());
            vm.setNewComment("");
            init(); // refresh
        }
    }

    public List<PostViewModel> getPostViewModels() { return postViewModels; }
    public List<Post> getActiveStories() { return activeStories; }
    public String getNewCaption() { return newCaption; }
    public void setNewCaption(String newCaption) { this.newCaption = newCaption; }
    public String getNewMediaUrl() { return newMediaUrl; }
    public void setNewMediaUrl(String newMediaUrl) { this.newMediaUrl = newMediaUrl; }
    public String getNewType() { return newType; }
    public void setNewType(String newType) { this.newType = newType; }
    public jakarta.servlet.http.Part getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(jakarta.servlet.http.Part uploadedFile) { this.uploadedFile = uploadedFile; }

    public String createStory() {
        this.newType = "story";
        return createPost();
    }

    public boolean isMyStoryActive() { return myStoryActive; }
    public Post getMyActiveStory() { return myActiveStory; }

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
        return "feed?faces-redirect=true";
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
        return "feed?faces-redirect=true";
    }

    public Integer getEditPostId() { return editPostId; }
    public void setEditPostId(Integer editPostId) { this.editPostId = editPostId; }
    public String getEditCaption() { return editCaption; }
    public void setEditCaption(String editCaption) { this.editCaption = editCaption; }
    public String getEditMediaUrl() { return editMediaUrl; }
    public void setEditMediaUrl(String editMediaUrl) { this.editMediaUrl = editMediaUrl; }
}
