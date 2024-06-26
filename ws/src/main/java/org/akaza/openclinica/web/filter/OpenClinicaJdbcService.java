/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

// TODO duplicate of the version in the web module?
public class OpenClinicaJdbcService extends JdbcDaoImpl {

    private OcUsersByUsernameMapping ocUsersByUsernameMapping;

    /**
     * Executes the <tt>usersByUsernameQuery</tt> and returns a list of UserDetails objects (there should normally only be one matching user).
     */
    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        this.ocUsersByUsernameMapping = new OcUsersByUsernameMapping(getDataSource());
        return ocUsersByUsernameMapping.execute(username);
    }

    /**
     * Can be overridden to customize the creation of the final UserDetailsObject returnd from <tt>loadUserByUsername</tt>.
     * 
     * @param username
     *            the name originally passed to loadUserByUsername
     * @param userFromUserQuery
     *            the object returned from the execution of the
     * @param combinedAuthorities
     *            the combined array of authorities from all the authority loading queries.
     * @return the final UserDetails which should be used in the system.
     */
    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery, GrantedAuthority[] combinedAuthorities) {
        String returnUsername = userFromUserQuery.getUsername();

        if (!isUsernameBasedPrimaryKey()) {
            returnUsername = username;
        }

        List<GrantedAuthority> grantedAuthorities = Arrays.asList(combinedAuthorities);
        return new User(returnUsername, userFromUserQuery.getPassword(), userFromUserQuery.isEnabled(), true, true, userFromUserQuery.isAccountNonLocked(),
                grantedAuthorities);
    }

    /**
     * Query object to look up a user.
     */
    private class OcUsersByUsernameMapping extends MappingSqlQuery<UserDetails> {
        protected OcUsersByUsernameMapping(DataSource ds) {
            super(ds, getUsersByUsernameQuery());
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        @Override
        protected UserDetails mapRow(ResultSet rs, int rownum) throws SQLException {
            String username = rs.getString(1);
            String password = rs.getString(2);
            boolean enabled = rs.getBoolean(3);
            boolean nonLocked = rs.getBoolean(4);
            List<SimpleGrantedAuthority> grantedAuthorities = Arrays.asList(new SimpleGrantedAuthority("HOLDER"));
            UserDetails user = new User(username, password, enabled, true, true, nonLocked, grantedAuthorities);

            return user;
        }
    }

}
