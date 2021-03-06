/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.security;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.login.LoginContext;
import org.sipfoundry.sipxconfig.permission.PermissionManager;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.springframework.security.core.GrantedAuthority;

public class PasswordEncoderImplTest extends TestCase {
    private static final String USER_NAME = "angelina";
    private static final String USER_ALIAS = "lara";
    private static final String RAW_PASSWORD = "croft";
    // dummy result based on userName and password
    private static final String ENCODED_PASSWORD = USER_NAME + RAW_PASSWORD;
    private LoginContext m_loginContext;
    private PasswordEncoderImpl m_passwordEncoder;
    private UserDetailsImpl m_userDetails;
    private SaltSourceImpl m_saltSourceImpl;

    @Override
    protected void setUp() throws Exception {
        m_loginContext = createMock(LoginContext.class);
        m_passwordEncoder = new PasswordEncoderImpl();
        m_passwordEncoder.setLoginContext(m_loginContext);

        final User user = new UserDetailsImplTest.RegularUser();
        user.setUserName(USER_NAME);
        PermissionManager pManager = createMock(PermissionManager.class);
        pManager.getPermissionModel();
        expectLastCall().andReturn(TestHelper.loadSettings("commserver/user-settings.xml")).anyTimes();
        replay(pManager);
        user.setPermissionManager(pManager);

        m_userDetails = new UserDetailsImpl(user, USER_ALIAS, new ArrayList<GrantedAuthority>());

        m_saltSourceImpl = new SaltSourceImpl();
    }

    public void testisPasswordValid() {
        m_loginContext.getEncodedPassword(USER_NAME, RAW_PASSWORD);
        expectLastCall().andReturn(ENCODED_PASSWORD).times(2);
        replay(m_loginContext);
        Object salt = m_saltSourceImpl.getSalt(m_userDetails);
        assertTrue(m_passwordEncoder.isPasswordValid(ENCODED_PASSWORD, RAW_PASSWORD, salt));
        assertFalse(m_passwordEncoder.isPasswordValid("bad encoded password", RAW_PASSWORD, salt));
        verify(m_loginContext);
    }

    public void testEncodePassword() {
        m_loginContext.getEncodedPassword(USER_NAME, RAW_PASSWORD);
        expectLastCall().andReturn(ENCODED_PASSWORD);
        replay(m_loginContext);
        Object salt = m_saltSourceImpl.getSalt(m_userDetails);
        assertEquals(ENCODED_PASSWORD, m_passwordEncoder.encodePassword(RAW_PASSWORD, salt));
        verify(m_loginContext);
    }
}
