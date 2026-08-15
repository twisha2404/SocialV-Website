package com.socialmedia.bean;

import com.socialmedia.entity.Conversation;
import com.socialmedia.entity.Message;
import com.socialmedia.entity.Groupmaster;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class ChatBean {

    @Inject
    private MessageDaoBean messageDao;
    @Inject
    private LoginBean loginBean;
    @Inject
    private UserDaoBean userDao;
    @Inject
    private GroupDaoBean groupDao;

    private List<Conversation> myConversations;
    private Conversation activeConversation;
    private List<Message> messages;
    private String newMessage;
    private Integer activeConversationId;
    private List<com.socialmedia.entity.User> allUsers;
    private List<com.socialmedia.entity.User> activeParticipants;

    // Group Creation fields
    private boolean creatingGroup = false;
    private String newGroupName;
    private String newGroupMediaUrl;
    private List<UserSelection> userSelections;

    @PostConstruct
    public void init() {
        if (!loginBean.isLoggedIn()) return;
        Integer userId = loginBean.getCurrentUser().getUserId();
        myConversations = messageDao.getUserConversations(userId);
        allUsers = userDao.getAllUsersExcept(userId);
        
        // Initialize userSelections for group creation
        userSelections = new java.util.ArrayList<>();
        if (allUsers != null) {
            for (com.socialmedia.entity.User u : allUsers) {
                userSelections.add(new UserSelection(u));
            }
        }

        String creatingGroupParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("creatingGroup");
        if ("true".equals(creatingGroupParam)) {
            creatingGroup = true;
        }
        
        String cIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("cId");
        if (cIdParam != null && !cIdParam.isEmpty()) {
            int targetId = Integer.parseInt(cIdParam);
            boolean isParticipant = false;
            Conversation matchedConversation = null;
            for (Conversation c : myConversations) {
                if (c.getConversationId().equals(targetId)) {
                    isParticipant = true;
                    matchedConversation = c;
                    break;
                }
            }
            if (isParticipant) {
                activeConversationId = targetId;
                activeConversation = matchedConversation;
                
                // Mark messages in this conversation as seen for the current user
                messageDao.markMessagesAsSeen(activeConversationId, userId);
                
                messages = messageDao.getMessages(activeConversationId);
                activeParticipants = messageDao.getConversationParticipants(activeConversationId);
            } else {
                activeConversationId = null;
                activeConversation = null;
                messages = null;
                activeParticipants = null;
            }
        }
    }

    public String loadConversation(Integer cId) {
        return "chat?faces-redirect=true&cId=" + cId;
    }

    public String startDirectChat(Integer otherUserId) {
        Conversation c = messageDao.getOrCreateDirectConversation(loginBean.getCurrentUser().getUserId(), otherUserId);
        return "chat?faces-redirect=true&cId=" + c.getConversationId();
    }

    public String startGroupChat(Integer groupId) {
        Conversation c = messageDao.getOrCreateGroupConversation(groupId);
        return "chat?faces-redirect=true&cId=" + c.getConversationId();
    }

    public String prepareCreateGroup() {
        this.creatingGroup = true;
        this.newGroupName = "";
        this.newGroupMediaUrl = "";
        if (userSelections != null) {
            for (UserSelection sel : userSelections) {
                sel.setSelected(false);
            }
        }
        return "chat?faces-redirect=true&creatingGroup=true";
    }

    public String createNewGroup() {
        if (loginBean.isLoggedIn() && newGroupName != null && !newGroupName.trim().isEmpty()) {
            Integer creatorId = loginBean.getCurrentUser().getUserId();
            // Create group
            Groupmaster group = groupDao.createGroup(newGroupName, newGroupMediaUrl, creatorId);
            if (group != null) {
                List<Integer> memberIds = new java.util.ArrayList<>();
                memberIds.add(creatorId);

                // Add selected members
                if (userSelections != null) {
                    for (UserSelection sel : userSelections) {
                        if (sel.isSelected()) {
                            groupDao.addMember(group.getGroupmasterId(), sel.getUser().getUserId());
                            memberIds.add(sel.getUser().getUserId());
                        }
                    }
                }
                // Sync/Get Group Conversation with explicit member list
                Conversation c = messageDao.getOrCreateGroupConversation(group.getGroupmasterId(), memberIds);
                return "chat?faces-redirect=true&cId=" + c.getConversationId();
            }
        }
        return "chat?faces-redirect=true";
    }

    public String sendMessage() {
        if (activeConversationId != null && newMessage != null && !newMessage.trim().isEmpty()) {
            boolean isParticipant = false;
            for (Conversation c : myConversations) {
                if (c.getConversationId().equals(activeConversationId)) {
                    isParticipant = true;
                    break;
                }
            }
            if (isParticipant) {
                messageDao.sendMessage(activeConversationId, loginBean.getCurrentUser().getUserId(), newMessage);
            }
            newMessage = "";
            return "chat?faces-redirect=true&cId=" + activeConversationId;
        }
        return null;
    }

    public List<Conversation> getMyConversations() { return myConversations; }
    public Conversation getActiveConversation() { return activeConversation; }
    public List<Message> getMessages() { return messages; }
    public String getNewMessage() { return newMessage; }
    public void setNewMessage(String newMessage) { this.newMessage = newMessage; }
    public Integer getActiveConversationId() { return activeConversationId; }
    public List<com.socialmedia.entity.User> getAllUsers() { return allUsers; }
    public List<com.socialmedia.entity.User> getActiveParticipants() { return activeParticipants; }

    public boolean isCreatingGroup() { return creatingGroup; }
    public void setCreatingGroup(boolean creatingGroup) { this.creatingGroup = creatingGroup; }
    public String getNewGroupName() { return newGroupName; }
    public void setNewGroupName(String newGroupName) { this.newGroupName = newGroupName; }
    public String getNewGroupMediaUrl() { return newGroupMediaUrl; }
    public void setNewGroupMediaUrl(String newGroupMediaUrl) { this.newGroupMediaUrl = newGroupMediaUrl; }
    public List<UserSelection> getUserSelections() { return userSelections; }
    public void setUserSelections(List<UserSelection> userSelections) { this.userSelections = userSelections; }

    // Nested Selection Helper Class
    public static class UserSelection implements java.io.Serializable {
        private com.socialmedia.entity.User user;
        private boolean selected;

        public UserSelection() {}

        public UserSelection(com.socialmedia.entity.User user) {
            this.user = user;
            this.selected = false;
        }

        public com.socialmedia.entity.User getUser() { return user; }
        public void setUser(com.socialmedia.entity.User user) { this.user = user; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
}
