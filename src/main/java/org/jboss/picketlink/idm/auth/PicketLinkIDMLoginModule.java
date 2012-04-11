package org.jboss.picketlink.idm.auth;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

public class PicketLinkIDMLoginModule extends UsernamePasswordLoginModule {

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

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
		super.initialize(subject, callbackHandler, sharedState, options);
	}

	@Override
	protected String getUsersPassword() throws LoginException {
		// TODO Auto-generated method stub
		return null;
	}

/* From IDMProcessor example
 * 
 *    
import java.util.logging.Level;
import org.picketlink.idm.api.Attribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jboss.jbossidmservlet.bean.GroupBean;
import org.jboss.jbossidmservlet.bean.UserBean;
import org.picketlink.idm.api.AttributesManager;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.IdentitySearchCriteria;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.IdentityType;
import org.picketlink.idm.api.Role;
import org.picketlink.idm.api.RoleManager;
import org.picketlink.idm.api.RoleType;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.FeatureNotSupportedException;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.api.SimpleAttribute;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.picketlink.idm.spi.model.IdentityObject;

    
    public IdmProcessor() throws IdentityConfigurationException, IdentityException {
        identitySessionFactory = new IdentityConfigurationImpl().configure("picketlink-config.xml").buildIdentitySessionFactory();
        init();
    }

    private void init() throws IdentityException {
        identitySession = identitySessionFactory.createIdentitySession("idm_realm");
    }

    public Collection<Group> getAssignedGroups(String username) {
        Collection<Group> groups = new ArrayList<Group>();
        try {
            identitySession.beginTransaction();
            User user = identitySession.getPersistenceManager().findUser(username);
            groups = identitySession.getRelationshipManager().findAssociatedGroups(user);
            identitySession.getTransaction().commit();
            identitySession.close();
        } catch (IdentityConfigurationException ex) {
            Logger.getLogger(IdmProcessor.class.getName()).error(ex);
        } catch (IdentityException ex) {
            Logger.getLogger(IdmProcessor.class.getName()).error(ex);
        }
        return groups;
    }



  private String idmSessionFactoryJNDI = "java:/IdentitySessionFactory";

   public String getIdmSessionFactoryJNDI()
   {
      return idmSessionFactoryJNDI;
   }

   public void setIdmSessionFactoryJNDI(String idmSessionFactoryJNDI)
   {
      this.idmSessionFactoryJNDI = idmSessionFactoryJNDI;
   }

   public void start() throws Exception
   {

      logger.fine("Starting example population service");

      Context ctx = new InitialContext();
      IdentitySessionFactory ids = (IdentitySessionFactory)ctx.lookup(getIdmSessionFactoryJNDI());

      IdentitySession is = ids.getCurrentIdentitySession("realm://JBossIdentity");
 

 * 
 * 
 * 
 * 
 * 	
 */
	
	
}
