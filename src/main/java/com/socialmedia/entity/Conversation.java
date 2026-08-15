package com.socialmedia.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer conversationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "groupId")
    private Groupmaster group;

    @Column(nullable = false)
    private String type; // 'user' or 'group'

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    @ManyToMany
    @JoinTable(
        name = "conversation_participants",
        joinColumns = @JoinColumn(name = "conversationId"),
        inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private List<User> participants;

    public Integer getConversationId() { return conversationId; }
    public void setConversationId(Integer conversationId) { this.conversationId = conversationId; }
    public Groupmaster getGroup() { return group; }
    public void setGroup(Groupmaster group) { this.group = group; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }
}
