package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class OAuthAuthentication implements Authentication, UserDetails {
    private UserAccountBean oauthAccount;

    public OAuthAuthentication(UserAccountBean oauthAccount) {
        this.oauthAccount = oauthAccount;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(1);
        auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return auths;

    }

    @Override
    public String getPassword() {
        return oauthAccount.getPasswd();

    }

    @Override
    public String getUsername() {
        return oauthAccount.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return oauthAccount.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return oauthAccount.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return oauthAccount.isActive();
    }

    @Override
    public boolean isEnabled() {
        return oauthAccount.isActive();
    }

    @Override
    public Object getCredentials() {
        return getPassword();
    }

    @Override
    public Object getDetails() {
        return oauthAccount;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return oauthAccount.getName();
    }
}
