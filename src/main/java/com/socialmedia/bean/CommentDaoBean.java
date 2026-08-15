package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.List;

@Stateless
public class CommentDaoBean {
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public void addComment(Integer userId, Integer postId, String content) {
        Comment c = new Comment();
        c.setUser(em.find(User.class, userId));
        c.setPost(em.find(Post.class, postId));
        c.setContent(content);
        em.persist(c);
        em.flush();
    }

    public List<Comment> getComments(Integer postId) {
        return em.createQuery("SELECT c FROM Comment c WHERE c.post.postId = :postId ORDER BY c.createdAt ASC", Comment.class)
                 .setParameter("postId", postId).getResultList();
    }
}
