package com.socialmedia.bean;

import com.socialmedia.entity.Groupmaster;
import com.socialmedia.entity.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterBean {
    
    private String username;
    private String fullName;
    private String password;
    private String profilePic;
    
    @Inject
    private UserDaoBean userDao;

    public String register() {
        // Find existing to prevent duplicates (basic check)
        if (userDao.findByUsername(username) != null) {
            jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(
                    jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                    "Username is already taken.",
                    "Username is already taken."
                )
            );
            return "register"; // username taken
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullName(fullName);
        newUser.setPassword(UserDaoBean.hashPassword(password));
        newUser.setProfilePic(profilePic != null && !profilePic.isEmpty() ? profilePic : "https://ui-avatars.com/api/?name=" + username);
        
        // Setup default groupmaster for ordinary user (ID 2 usually based on schema)
        Groupmaster defaultGroup = userDao.findGroupmasterById(2);
        if (defaultGroup == null) {
            defaultGroup = new Groupmaster();
            defaultGroup.setGroupmasterId(2);
        }
        newUser.setGroupmaster(defaultGroup);

        userDao.create(newUser);
        return "index?faces-redirect=true";
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
}
