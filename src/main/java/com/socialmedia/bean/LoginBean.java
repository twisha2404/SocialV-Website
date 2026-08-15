package com.socialmedia.bean;

import com.socialmedia.entity.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {
    
    private String username;
    private String password;
    private User currentUser;
    
    @Inject
    private UserDaoBean userDao;
    @Inject
    private MessageDaoBean messageDao;

    public String login() {
        User user = userDao.findByUsernameAndPassword(username, password);
        if (user != null) {
            if (user.getBlocked() != null && user.getBlocked()) {
                jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
                    new jakarta.faces.application.FacesMessage(
                        jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                        "Your account has been blocked by the administrator.",
                        "Your account has been blocked by the administrator."
                    )
                );
                return "index";
            }
            this.currentUser = user;
            
            // Redirect admin to admin panel
            if (user.getGroupmaster() != null && user.getGroupmaster().getGroupmasterId() == 1) {
                return "admin?faces-redirect=true";
            }
            return "feed?faces-redirect=true";
        }
        jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
            new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                "Invalid username or password.",
                "Invalid username or password."
            )
        );
        return "index";
    }

    public String logout() {
        jakarta.faces.context.FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index?faces-redirect=true";
    }

    public void checkLogin() throws java.io.IOException {
        if (!isLoggedIn()) {
            jakarta.faces.context.FacesContext.getCurrentInstance()
                .getExternalContext().redirect("index.xhtml");
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    
    public boolean isHasUnseenMessages() {
        if (!isLoggedIn()) return false;
        return messageDao.hasUnseenMessages(currentUser.getUserId());
    }
}
