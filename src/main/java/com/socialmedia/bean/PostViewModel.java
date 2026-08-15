package com.socialmedia.bean;

import com.socialmedia.entity.Comment;
import com.socialmedia.entity.Post;
import java.util.List;

public class PostViewModel {
    private Post post;
    private long likesCount;
    private boolean likedByMe;
    private List<Comment> comments;
    private String newComment;

    public PostViewModel(Post post, long likesCount, boolean likedByMe, List<Comment> comments) {
        this.post = post;
        this.likesCount = likesCount;
        this.likedByMe = likedByMe;
        this.comments = comments;
    }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }
    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public String getNewComment() { return newComment; }
    public void setNewComment(String newComment) { this.newComment = newComment; }
}
