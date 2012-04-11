package org.jboss.picketlink.idm.security;

import java.security.Principal;
import java.security.acl.Group;

import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.AbstractServerLoginModule;

public class PicketLinkIDMLoginModule extends AbstractServerLoginModule {

	@Override
	protected Principal getIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Group[] getRoleSets() throws LoginException {
		// TODO Auto-generated method stub
		return null;
	}

}
