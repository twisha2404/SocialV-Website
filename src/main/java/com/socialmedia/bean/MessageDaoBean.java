package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.List;

@Stateless
public class MessageDaoBean {
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    public List<Conversation> getUserConversations(Integer userId) {
        // Find conversations where user is a participant
        return em.createQuery(
            "SELECT DISTINCT c FROM Conversation c " +
            "LEFT JOIN FETCH c.group " +
            "LEFT JOIN FETCH c.participants " +
            "JOIN c.participants p " +
            "WHERE p.userId = :userId " +
            "ORDER BY COALESCE(c.updatedAt, c.createdAt) DESC, c.conversationId DESC", Conversation.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public List<Message> getMessages(Integer conversationId) {
        return em.createQuery("SELECT m FROM Message m WHERE m.conversation.conversationId = :cId ORDER BY m.sentAt ASC", Message.class)
                 .setParameter("cId", conversationId)
                 .getResultList();
    }

    public List<User> getConversationParticipants(Integer conversationId) {
        Conversation c = em.find(Conversation.class, conversationId);
        if (c != null) {
            c.getParticipants().size(); // force lazy load initialization
            return c.getParticipants();
        }
        return new java.util.ArrayList<>();
    }

    public void sendMessage(Integer conversationId, Integer senderId, String content) {
        Message m = new Message();
        Conversation c = em.find(Conversation.class, conversationId);
        m.setConversation(c);
        m.setSender(em.find(User.class, senderId));
        m.setContent(content);
        m.setIsSeen(false);
        em.persist(m);
        
        c.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        em.merge(c);
        em.flush();
    }

    public Conversation getOrCreateDirectConversation(Integer user1Id, Integer user2Id) {
        // Simple direct conversation implementation
        try {
            return (Conversation) em.createNativeQuery(
                "SELECT c.* FROM conversations c " +
                "JOIN conversation_participants cp1 ON c.conversationId = cp1.conversationId " +
                "JOIN conversation_participants cp2 ON c.conversationId = cp2.conversationId " +
                "WHERE c.type = 'user' AND cp1.userId = ?1 AND cp2.userId = ?2", Conversation.class)
                .setParameter(1, user1Id)
                .setParameter(2, user2Id)
                .getSingleResult();
        } catch (NoResultException e) {
            Conversation c = new Conversation();
            c.setType("user");
            
            List<User> participants = new java.util.ArrayList<>();
            participants.add(em.find(User.class, user1Id));
            participants.add(em.find(User.class, user2Id));
            c.setParticipants(participants);
            
            em.persist(c);
            em.flush();
            return c;
        }
    }

    public Conversation getOrCreateGroupConversation(Integer groupId) {
        return getOrCreateGroupConversation(groupId, null);
    }

    public Conversation getOrCreateGroupConversation(Integer groupId, List<Integer> memberUserIds) {
        try {
            return em.createQuery("SELECT c FROM Conversation c WHERE c.group.groupmasterId = :gId", Conversation.class)
                     .setParameter("gId", groupId).getSingleResult();
        } catch (NoResultException e) {
            Conversation c = new Conversation();
            Groupmaster g = em.find(Groupmaster.class, groupId);
            c.setGroup(g);
            c.setType("group");
            
            List<User> participants = new java.util.ArrayList<>();
            if (memberUserIds != null && !memberUserIds.isEmpty()) {
                for (Integer uId : memberUserIds) {
                    User u = em.find(User.class, uId);
                    if (u != null) {
                        participants.add(u);
                    }
                }
            } else if (g != null && g.getMembers() != null) {
                participants.addAll(g.getMembers());
            }
            c.setParticipants(participants);
            
            em.persist(c);
            em.flush();
            return c;
        }
    }

    public boolean hasUnseenMessages(Integer userId) {
        try {
            Long count = em.createQuery(
                "SELECT COUNT(m) FROM Message m " +
                "JOIN m.conversation c JOIN c.participants p " +
                "WHERE p.userId = :userId AND m.sender.userId != :userId AND m.isSeen = false", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void markMessagesAsSeen(Integer conversationId, Integer userId) {
        try {
            em.createQuery(
                "UPDATE Message m SET m.isSeen = true " +
                "WHERE m.conversation.conversationId = :cId AND m.sender.userId != :userId AND m.isSeen = false")
                .setParameter("cId", conversationId)
                .setParameter("userId", userId)
                .executeUpdate();
            em.flush();
        } catch (Exception e) {
            // Log or ignore
        }
    }
}
