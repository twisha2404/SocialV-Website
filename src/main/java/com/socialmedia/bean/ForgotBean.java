package com.socialmedia.bean;

import com.socialmedia.entity.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;

@Named
@RequestScoped
public class ForgotBean {

    private String username;
    private String fullName;
    private String phone;
    private Date dob;
    private String newPassword;
    private String confirmPassword;

    @Inject
    private UserDaoBean userDao;

    public String resetPassword() {
        FacesContext context = FacesContext.getCurrentInstance();

        // 1. Validate fields are not empty
        if (username == null || username.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username is required.", ""));
            return null;
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Full Name is required.", ""));
            return null;
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "New Password is required.", ""));
            return null;
        }
        if (!newPassword.equals(confirmPassword)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match.", ""));
            return null;
        }

        // 2. Find user
        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username not found.", ""));
            return null;
        }

        // 3. Verify security details
        // Check Full Name (case-insensitive, trimmed)
        if (user.getFullName() == null || !user.getFullName().trim().equalsIgnoreCase(fullName.trim())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect verification details (Full Name mismatch).", ""));
            return null;
        }

        // If phone is configured on user's profile, it must match
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (phone == null || !user.getPhone().trim().equals(phone.trim())) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect verification details (Phone Number mismatch).", ""));
                return null;
            }
        }

        // If DOB is configured on user's profile, it must match
        if (user.getDob() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dbDob = sdf.format(user.getDob());
            String inputDob = (dob != null) ? sdf.format(dob) : "";
            if (!dbDob.equals(inputDob)) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect verification details (Date of Birth mismatch).", ""));
                return null;
            }
        }

        // 4. Perform password update
        user.setPassword(UserDaoBean.hashPassword(newPassword));
        userDao.update(user);

        // Set success message in Flash scope for redirect
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Password reset successfully. Please log in with your new password.", ""));

        return "index?faces-redirect=true";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
