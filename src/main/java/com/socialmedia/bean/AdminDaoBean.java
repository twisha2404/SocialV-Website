package com.socialmedia.bean;

import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class AdminDaoBean {

    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u WHERE u.username != 'admin2112' ORDER BY u.userId DESC", User.class)
                 .getResultList();
    }

    public List<Post> getAllPosts() {
        return em.createQuery("SELECT p FROM Post p ORDER BY p.createdAt DESC", Post.class)
                 .getResultList();
    }

    public List<Comment> getAllComments() {
        return em.createQuery("SELECT c FROM Comment c ORDER BY c.createdAt DESC", Comment.class)
                 .getResultList();
    }

    public List<Like> getAllLikes() {
        return em.createQuery("SELECT l FROM Like l ORDER BY l.likeId DESC", Like.class)
                 .getResultList();
    }

    public void updateUser(User user) {
        User u = em.find(User.class, user.getUserId());
        if (u != null) {
            u.setFullName(user.getFullName());
            u.setUsername(user.getUsername());
            u.setProfilePic(user.getProfilePic());
            em.merge(u);
            em.flush();
        }
    }

    public void toggleBlockUser(Integer userId) {
        User u = em.find(User.class, userId);
        if (u != null) {
            u.setBlocked(!u.getBlocked());
            em.merge(u);
            em.flush();
        }
    }

    public void toggleBlockPost(Integer postId) {
        Post p = em.find(Post.class, postId);
        if (p != null) {
            p.setBlocked(!p.getBlocked());
            em.merge(p);
            em.flush();
        }
    }

    public void deleteComment(Integer commentId) {
        Comment c = em.find(Comment.class, commentId);
        if (c != null) {
            em.remove(c);
            em.flush();
        }
    }

    public void deleteLike(Integer likeId) {
        Like l = em.find(Like.class, likeId);
        if (l != null) {
            em.remove(l);
            em.flush();
        }
    }

    public void deletePost(Integer postId) {
        Post post = em.find(Post.class, postId);
        if (post != null) {
            // Delete associated likes
            em.createQuery("DELETE FROM Like l WHERE l.post.postId = :postId")
              .setParameter("postId", postId)
              .executeUpdate();
            
            // Delete associated comments
            em.createQuery("DELETE FROM Comment c WHERE c.post.postId = :postId")
              .setParameter("postId", postId)
              .executeUpdate();
            
            em.remove(post);
            em.flush();
        }
    }

    public void deleteUser(Integer userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            // 1. Delete all likes by the user
            em.createQuery("DELETE FROM Like l WHERE l.user.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 2. Delete all likes on posts owned by this user
            em.createQuery("DELETE FROM Like l WHERE l.post.user.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 3. Delete all comments by the user
            em.createQuery("DELETE FROM Comment c WHERE c.user.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 4. Delete all comments on posts owned by this user
            em.createQuery("DELETE FROM Comment c WHERE c.post.user.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 5. Delete all followers links
            em.createQuery("DELETE FROM Follower f WHERE f.followerUser.userId = :userId OR f.followedUser.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 6. Delete all messages sent by this user
            em.createQuery("DELETE FROM Message m WHERE m.sender.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 7. Remove from Many-to-Many groupmembers join table
            em.createNativeQuery("DELETE FROM groupmembers WHERE userId = ?1")
              .setParameter(1, userId)
              .executeUpdate();

            // 8. Remove from Many-to-Many conversation_participants join table
            em.createNativeQuery("DELETE FROM conversation_participants WHERE userId = ?1")
              .setParameter(1, userId)
              .executeUpdate();

            // 9. Delete all posts by this user
            em.createQuery("DELETE FROM Post p WHERE p.user.userId = :userId")
              .setParameter("userId", userId)
              .executeUpdate();

            // 10. Delete the user
            em.remove(user);
            em.flush();
        }
    }
}
