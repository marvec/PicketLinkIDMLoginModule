package org.jboss.picketlink.idm.auth;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collection;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.transaction.TransactionManager;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.common.exception.NoSuchUserException;
import org.picketlink.idm.common.transaction.TransactionManagerProvider;
import org.picketlink.idm.common.transaction.Transactions;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;

public class PicketLinkIDMLoginModule extends UsernamePasswordLoginModule {

    protected String identitySessionFactoryJNDIName; // session factory in JNDI
    protected String realmName; // name of the security realm to use
    protected String roleGroupTypeName; // type name of the group specifier
    protected String userEnabledAttributeName; // attribute that denotes whether
                                               // a user is enabled (true|false)
    protected String additionalRole;
    protected String associatedGroupType;
    protected String associatedGroupName;
    protected String configurationFileName;
    protected boolean validateUserNameCase; // ignore username case
    protected boolean userNameToLowerCase; // automatically convert username to
                                           // lowercase
    protected boolean manageTransaction;

    private IdentitySessionFactory identitySessionFactory;

    private Group[] retrieveRoleSets(IdentitySession ids) throws LoginException {
        Group rolesGroup = new SimpleGroup("Roles");
        if (additionalRole != null) {
            rolesGroup.addMember(createIdentity(additionalRole));
        }

        try {
            User user = ids.getPersistenceManager().findUser(getUsername());
            Collection<org.picketlink.idm.api.Group> userGroups = ids.getRelationshipManager().findAssociatedGroups(
                    user, roleGroupTypeName);
            for (org.picketlink.idm.api.Group userGroup : userGroups) {
                String roleName = userGroup.getName();

                Principal p = createIdentity(roleName);
                rolesGroup.addMember(p);
            }

        } catch (Exception e) {
            throw new LoginException(e.toString());
        }

        return new Group[] { rolesGroup };

    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        try {
            TransactionManager tm = TransactionManagerProvider.JBOSS_PROVIDER.getTransactionManager();
            return (Group[]) Transactions.required(tm, new Transactions.Runnable() {
                public Object run() throws Exception {
                    IdentitySession ids = getIdentitySessionFactory().getCurrentIdentitySession(realmName);
                    ids.beginTransaction();

                    if (manageTransaction) {
                        ids.beginTransaction();
                    }

                    Group[] result = retrieveRoleSets(ids);

                    if (manageTransaction) {
                        ids.getTransaction().commit();
                    }

                    return result;
                }
            });
        } catch (Exception e) {
            throw new LoginException(e.toString());
        }
    }

    protected Principal createIdentity(String username) {
        return new SimplePrincipal(username);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        identitySessionFactoryJNDIName = (String) options.get("identitySessionFactoryJNDIName");
        configurationFileName = (String) options.get("configurationFileName");
        realmName = (String) options.get("realmName");
        roleGroupTypeName = (String) options.get("roleGroupTypeName");
        userEnabledAttributeName = (String) options.get("userEnabledAttributeName");
        additionalRole = (String) options.get("additionalRole");
        associatedGroupType = (String) options.get("associatedGroupType");
        associatedGroupName = (String) options.get("associatedGroupName");
        validateUserNameCase = Boolean.parseBoolean((String) options.get("validateUserNameCase"));
        userNameToLowerCase = Boolean.parseBoolean((String) options.get("userNameToLowerCase"));
        manageTransaction = Boolean.parseBoolean((String) options.get("transactionAware"));

        if (configurationFileName != null && identitySessionFactoryJNDIName != null) {
            throw new RuntimeException(
                    "Either identitySessionFactoryJNDIName or configurationFileName must be set. Not both at the same time.");
        }
    }

    @Override
    protected String getUsersPassword() throws LoginException {
        return null;
    }

    protected IdentitySessionFactory getIdentitySessionFactory() throws NamingException, IdentityConfigurationException {
        if (identitySessionFactory == null) {
            if (identitySessionFactoryJNDIName == null) {
                identitySessionFactory = new IdentityConfigurationImpl().configure(configurationFileName)
                        .buildIdentitySessionFactory();
            } else {
                identitySessionFactory = (IdentitySessionFactory) new InitialContext()
                        .lookup(identitySessionFactoryJNDIName);
            }
        }
        return identitySessionFactory;
    }

    @Override
    protected boolean validatePassword(String inputPassword, String expectedPassword) {
        if (inputPassword != null) {
            UserStatus status = getUserStatus(inputPassword);
            if (status == UserStatus.OK) {
                return true;
            }
        }

        return false;
    }

    private UserStatus getUserStatus(final String inputPassword) {
        UserStatus result = null;

        try {
            TransactionManager tm = TransactionManagerProvider.JBOSS_PROVIDER.getTransactionManager();
            UserStatus tmp = (UserStatus) Transactions.required(tm, new Transactions.Runnable() {
                public Object run() throws Exception {
                    IdentitySession ids = getIdentitySessionFactory().getCurrentIdentitySession(realmName);
                    ids.beginTransaction();

                    if (manageTransaction) {
                        ids.beginTransaction();
                    }

                    UserStatus status = retrieveUserStatus(ids, inputPassword);

                    if (manageTransaction) {
                        ids.getTransaction().commit();
                    }

                    return status;
                }
            });
            result = tmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected UserStatus retrieveUserStatus(IdentitySession ids, final String inputPassword) throws LoginException {
        try {
            User user = ids.getPersistenceManager().findUser(getUsername());

            if (user == null) {
                throw new NoSuchUserException("Null user returned by the parent class.");
            }

            // This is because LDAP binds can be non case sensitive
            if (validateUserNameCase && !getUsername().equals(user.getKey())) {
                return UserStatus.UNEXISTING;
            }

            // Enabled
            if (userEnabledAttributeName != null) {
                boolean enabled = false;
                try {
                    Object enabledS;
                    enabledS = ids.getAttributesManager().getAttribute(user, userEnabledAttributeName);
                    if (enabledS != null) {
                        enabled = new Boolean(enabledS.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!enabled) {
                    return UserStatus.DISABLED;
                }
            }

            if (associatedGroupName != null && associatedGroupType != null) {
                boolean hasTheGroup = false;

                org.picketlink.idm.api.Group associatedGroup = ids.getPersistenceManager().findGroup(
                        associatedGroupName, associatedGroupType);
                if (associatedGroup != null) {
                    hasTheGroup = ids.getRelationshipManager().isAssociated(associatedGroup, user);
                }
                if (!hasTheGroup) {
                    return UserStatus.NOTASSIGNEDTOROLE;
                }
            }

            if (!ids.getAttributesManager().validatePassword(user, inputPassword)) {
                return UserStatus.WRONGPASSWORD;
            }

        } catch (NoSuchUserException e1) {
            return UserStatus.UNEXISTING;
        } catch (Exception e) {
            throw new LoginException(e.toString());
        }
        return UserStatus.OK;
    }

    @Override
    protected String getUsername() {
        String userName = super.getUsername();
        return (userNameToLowerCase && userName != null) ? userName.toLowerCase() : userName;
    }

    @Override
    protected String[] getUsernameAndPassword() throws LoginException {
        String[] names = super.getUsernameAndPassword();

        if (userNameToLowerCase) {
            if (names[0] != null) {
                names[0] = names[0].toLowerCase();
            }
        }
        return names;
    }

}
