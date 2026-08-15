package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.List;

@Stateless
public class PostDaoBean {
    
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public List<Post> getAllPosts() {
        return em.createQuery("SELECT p FROM Post p WHERE p.type != 'story' AND p.blocked = false ORDER BY p.createdAt DESC", Post.class)
                 .getResultList();
    }
    
    public List<Post> getPostsByUser(Integer userId) {
        return em.createQuery("SELECT p FROM Post p WHERE p.user.userId = :userId AND p.type != 'story' AND p.blocked = false ORDER BY p.createdAt DESC", Post.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public List<Post> getActiveStories() {
        java.sql.Timestamp cutoff = new java.sql.Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        return em.createQuery("SELECT p FROM Post p WHERE p.type = 'story' AND p.createdAt >= :cutoff ORDER BY p.createdAt DESC", Post.class)
                 .setParameter("cutoff", cutoff)
                 .getResultList();
    }
    
    public void create(Post post) {
        em.persist(post);
        em.flush();
    }
    
    public Post find(Integer postId) {
        return em.find(Post.class, postId);
    }
    
    public void update(Post post) {
        em.merge(post);
        em.flush();
    }
    
    public void delete(Integer postId) {
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
            
            // Delete the post itself
            em.remove(post);
            em.flush();
        }
    }
}
