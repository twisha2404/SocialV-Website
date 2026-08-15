package com.socialmedia.bean;

import com.socialmedia.entity.User;
import com.socialmedia.entity.Groupmaster;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserDaoBean {
    
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return password; // Fallback
        }
    }

    public User findByUsernameAndPassword(String username, String password) {
        User user = findByUsername(username);
        if (user != null) {
            String dbPassword = user.getPassword();
            if (dbPassword != null) {
                String hashedPassword = hashPassword(password);
                if (dbPassword.equals(hashedPassword) || dbPassword.equals(password)) {
                    return user;
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public java.util.List<User> getAllUsersExcept(Integer currentUserId) {
        return em.createQuery("SELECT u FROM User u WHERE u.userId != :currentId ORDER BY u.fullName ASC", User.class)
                 .setParameter("currentId", currentUserId)
                 .getResultList();
    }
    
    public User findById(Integer id) {
        return em.find(User.class, id);
    }
    
    public void create(User user) {
        em.persist(user);
    }
    
    public void update(User user) {
        em.merge(user);
        em.flush();
    }
    
    public Groupmaster findGroupmasterById(Integer id) {
        return em.find(Groupmaster.class, id);
    }
}
