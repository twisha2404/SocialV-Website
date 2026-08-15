package com.socialmedia.bean;
import com.socialmedia.entity.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import java.util.List;

@Stateless
public class GroupDaoBean {
    @PersistenceContext(unitName = "socialMediaPU")
    private EntityManager em;

    @Inject
    private MessageDaoBean messageDao;

    public List<Groupmaster> getAllGroups() {
        return em.createQuery("SELECT DISTINCT g FROM Groupmaster g LEFT JOIN FETCH g.members ORDER BY g.createdAt DESC", Groupmaster.class).getResultList();
    }
    
    public Groupmaster getGroupById(Integer groupId) {
        try {
            return em.createQuery("SELECT g FROM Groupmaster g LEFT JOIN FETCH g.members WHERE g.groupmasterId = :id", Groupmaster.class)
                     .setParameter("id", groupId)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Groupmaster createGroup(String name, String mediaUrl, Integer creatorId) {
        Groupmaster g = new Groupmaster();
        g.setGroupName(name);
        g.setMediaUrl(mediaUrl != null && !mediaUrl.isEmpty() ? mediaUrl : "https://ui-avatars.com/api/?name=" + name);
        g.setCreatedBy(em.find(User.class, creatorId));
        em.persist(g);
        em.flush();
        // Add creator as member automatically
        addMember(g.getGroupmasterId(), creatorId);
        
        // Auto-create group conversation
        try {
            messageDao.getOrCreateGroupConversation(g.getGroupmasterId());
        } catch (Exception e) {}
        
        return g;
    }

    public void addMember(Integer groupmasterId, Integer userId) {
        try {
            Groupmaster g = em.find(Groupmaster.class, groupmasterId);
            User u = em.find(User.class, userId);
            if (g != null && u != null) {
                if (g.getMembers() == null) {
                    g.setMembers(new java.util.ArrayList<>());
                }
                if (!g.getMembers().contains(u)) {
                    g.getMembers().add(u);
                    em.merge(g);
                }
                
                // Sync with conversation if it exists
                try {
                    Conversation c = em.createQuery("SELECT c FROM Conversation c WHERE c.group.groupmasterId = :gId", Conversation.class)
                                       .setParameter("gId", groupmasterId).getSingleResult();
                    if (c.getParticipants() == null) {
                        c.setParticipants(new java.util.ArrayList<>());
                    }
                    if (!c.getParticipants().contains(u)) {
                        c.getParticipants().add(u);
                        em.merge(c);
                    }
                } catch (NoResultException e) {}
                em.flush();
            }
        } catch (Exception e) {}
    }
}
