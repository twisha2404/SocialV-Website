package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.List;

@Stateless
public class FollowerDaoBean {
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public void requestFollow(Integer followerId, Integer followedId) {
        if (followerId.equals(followedId)) return;
        try {
            Follower existing = em.createQuery("SELECT f FROM Follower f WHERE f.followerUser.userId = :fId AND f.followedUser.userId = :ftId", Follower.class)
                                  .setParameter("fId", followerId)
                                  .setParameter("ftId", followedId)
                                  .getSingleResult();
            // If exists, clicking again unfollows or cancels request
            em.remove(existing);
        } catch (NoResultException e) {
            Follower f = new Follower();
            f.setFollowerUser(em.find(User.class, followerId));
            f.setFollowedUser(em.find(User.class, followedId));
            f.setStatus("pending");
            em.persist(f);
            em.flush();
        }
    }

    public void acceptRequest(Integer followerId, Integer followedId) {
        try {
            Follower existing = em.createQuery("SELECT f FROM Follower f WHERE f.followerUser.userId = :fId AND f.followedUser.userId = :ftId", Follower.class)
                                  .setParameter("fId", followerId)
                                  .setParameter("ftId", followedId)
                                  .getSingleResult();
            existing.setStatus("accepted");
            em.merge(existing);
            em.flush();
        } catch (NoResultException e) {}
    }

    public void declineRequest(Integer followerId, Integer followedId) {
        try {
            Follower existing = em.createQuery("SELECT f FROM Follower f WHERE f.followerUser.userId = :fId AND f.followedUser.userId = :ftId", Follower.class)
                                  .setParameter("fId", followerId)
                                  .setParameter("ftId", followedId)
                                  .getSingleResult();
            em.remove(existing);
            em.flush();
        } catch (NoResultException e) {}
    }

    public List<Follower> getPendingRequests(Integer userId) {
        return em.createQuery("SELECT f FROM Follower f WHERE f.followedUser.userId = :userId AND f.status = 'pending'", Follower.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public long getFollowersCount(Integer userId) {
        try {
            return em.createQuery("SELECT COUNT(f) FROM Follower f WHERE f.followedUser.userId = :userId AND f.status = 'accepted'", Long.class)
                     .setParameter("userId", userId).getSingleResult();
        } catch(Exception e) { return 0; }
    }

    public long getFollowingCount(Integer userId) {
        try {
            return em.createQuery("SELECT COUNT(f) FROM Follower f WHERE f.followerUser.userId = :userId AND f.status = 'accepted'", Long.class)
                     .setParameter("userId", userId).getSingleResult();
        } catch(Exception e) { return 0; }
    }
    
    public String getFollowStatus(Integer followerId, Integer followedId) {
        try {
            String stat = em.createQuery("SELECT f.status FROM Follower f WHERE f.followerUser.userId = :fId AND f.followedUser.userId = :ftId", String.class)
                     .setParameter("fId", followerId).setParameter("ftId", followedId).getSingleResult();
            return stat; // returns "pending" or "accepted"
        } catch(NoResultException e) { return "none"; }
    }

    public List<User> getFollowers(Integer userId) {
        return em.createQuery("SELECT f.followerUser FROM Follower f WHERE f.followedUser.userId = :userId AND f.status = 'accepted' ORDER BY f.followerUser.fullName ASC", User.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public List<User> getFollowing(Integer userId) {
        return em.createQuery("SELECT f.followedUser FROM Follower f WHERE f.followerUser.userId = :userId AND f.status = 'accepted' ORDER BY f.followedUser.fullName ASC", User.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
}
