package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class LikeDaoBean {
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public void toggleLike(Integer userId, Integer postId) {
        try {
            Like existing = em.createQuery("SELECT l FROM Like l WHERE l.user.userId = :userId AND l.post.postId = :postId", Like.class)
                              .setParameter("userId", userId)
                              .setParameter("postId", postId)
                              .getSingleResult();
            em.remove(existing);
        } catch (NoResultException e) {
            Like l = new Like();
            l.setUser(em.find(User.class, userId));
            l.setPost(em.find(Post.class, postId));
            em.persist(l);
        }
    }
    
    public long getLikeCount(Integer postId) {
        try {
            return em.createQuery("SELECT COUNT(l) FROM Like l WHERE l.post.postId = :postId", Long.class)
                     .setParameter("postId", postId).getSingleResult();
        } catch (Exception e) { return 0; }
    }

    public boolean isLikedBy(Integer userId, Integer postId) {
        try {
            return em.createQuery("SELECT COUNT(l) FROM Like l WHERE l.user.userId = :userId AND l.post.postId = :postId", Long.class)
                     .setParameter("userId", userId).setParameter("postId", postId).getSingleResult() > 0;
        } catch (Exception e) { return false; }
    }
}
