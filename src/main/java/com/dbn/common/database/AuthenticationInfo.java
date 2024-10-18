package com.dbn.common.database;

import com.dbn.common.constant.Constants;
import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.util.Cloneable;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Strings;
import com.dbn.common.util.TimeAware;
import com.dbn.connection.AuthenticationType;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ConnectionDatabaseSettings;
import com.dbn.connection.config.Passwords;
import com.dbn.credentials.DatabaseCredentialManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dbn.common.options.setting.Settings.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class AuthenticationInfo extends BasicConfiguration<ConnectionDatabaseSettings, ConfigurationEditorForm> implements Cloneable<AuthenticationInfo>, TimeAware {
    private static final String USE_BROWSER_FOR_TOKEN_ATTRIBUTE = "useBrowserForToken";
	private static final String PROFILE_ATTRIBUTE = "profile";
	private static final String CONFIG_FILE_PATH_ATTRIBUTE = "configFilePath";
	@Deprecated // TODO move to keychain
    private static final String OLD_PWD_ATTRIBUTE = "password";
    @Deprecated // TODO move to keychain
    private static final String TEMP_PWD_ATTRIBUTE = "deprecated-pwd";

    private final long timestamp = System.currentTimeMillis();

    private AuthenticationType type = AuthenticationType.USER_PASSWORD;
    private String user;
    private String password;
    private boolean temporary;
    
    // token auth
    private boolean useBrowserForTokenAuth;
    private String pathToConfigFile;
    private String profile;

    public AuthenticationInfo(ConnectionDatabaseSettings parent, boolean temporary) {
        super(parent);
        this.temporary = temporary;
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    public String getPassword() {
    	return this.password;
    }
    public void setPassword(String password) {
        this.password = Strings.isEmpty(password) ? null : password;
    }

    public String getProfile() {
        return profile;
    }

    public String getPathToConfigFile() {
        return pathToConfigFile;
    }

    public boolean isUseBrowserForTokenAuth() {
		return useBrowserForTokenAuth;
	}

	public boolean isProvided() {
        switch (type) {
            case NONE: return true;
            case USER: return Strings.isNotEmpty(user);
            case USER_PASSWORD: return Strings.isNotEmpty(user) && Strings.isNotEmpty(password);
            case OS_CREDENTIALS: return true;
//            case TOKEN_AUTHENTICATION: return useBrowserForTokenAuth || 
//            		Strings.isNotEmpty(pathToConfigFile) && 
//            		Strings.isNotEmpty(profile);
        }
        return true;
    }

    public boolean isSame(AuthenticationInfo authenticationInfo) {
    	if (authenticationInfo.type != type) return false;
    	switch (type) {
    		case NONE:
    		case USER:
    		case USER_PASSWORD:
    		case OS_CREDENTIALS:
    			return Commons.match(this.user, authenticationInfo.user) &&
    		           Commons.match(this.getPassword(), authenticationInfo.getPassword());
    		case TOKEN_AUTHENTICATION:
    			return Commons.match(this.pathToConfigFile, authenticationInfo.pathToConfigFile) &&
    				   Commons.match(this.profile, profile) &&
    				   this.useBrowserForTokenAuth == authenticationInfo.useBrowserForTokenAuth;
    		default:
    			return false;
    	}
        
    }

    @Override
    public void readConfiguration(Element element) {
        user = getString(element, "user", user);
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();

        if (DatabaseCredentialManager.USE) {
            password = credentialManager.getPassword(getConnectionId(), user);
        }

        pathToConfigFile = getString(element, CONFIG_FILE_PATH_ATTRIBUTE, pathToConfigFile);
        profile = getString(element, PROFILE_ATTRIBUTE, profile);
        useBrowserForTokenAuth = getBoolean(element, USE_BROWSER_FOR_TOKEN_ATTRIBUTE, false);

        // old storage fallback - TODO cleanup
        if (Strings.isEmpty(password)) {
            password = Passwords.decodePassword(getString(element, TEMP_PWD_ATTRIBUTE, password));
            if (Strings.isEmpty(password)) {
                password = Passwords.decodePassword(getString(element, OLD_PWD_ATTRIBUTE, password));
            }

            if (Strings.isNotEmpty(this.password) && DatabaseCredentialManager.USE) {
                credentialManager.setPassword(getConnectionId(), user, this.password);
            }
        }

        type = getEnum(element, "type", type);

        AuthenticationType[] supportedAuthTypes = getParent().getDatabaseType().getAuthTypes();
        if (!Constants.isOneOf(type, supportedAuthTypes)) {
            type = supportedAuthTypes[0];
        }

        // TODO backward compatibility
        if (getBoolean(element, "os-authentication", false)) {
            type = AuthenticationType.OS_CREDENTIALS;
        } else if (getBoolean(element, "empty-authentication", false)) {
            type = AuthenticationType.USER;
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setEnum(element, "type", type);
        setString(element, "user", nvl(user));

        String encodedPassword = Passwords.encodePassword(password);
        if (!DatabaseCredentialManager.USE){
            setString(element, TEMP_PWD_ATTRIBUTE, encodedPassword);
        }
        
        setString(element, CONFIG_FILE_PATH_ATTRIBUTE, pathToConfigFile);
        setString(element, PROFILE_ATTRIBUTE, profile);
        setBoolean(element, USE_BROWSER_FOR_TOKEN_ATTRIBUTE, useBrowserForTokenAuth);
    }

    @Override
    public AuthenticationInfo clone() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo(getParent(), temporary);
        authenticationInfo.type = type;
        authenticationInfo.user = user;
        authenticationInfo.password = password;
        
        // Token Auth
        authenticationInfo.pathToConfigFile = this.pathToConfigFile;
        authenticationInfo.profile = this.profile;
        authenticationInfo.useBrowserForTokenAuth = this.useBrowserForTokenAuth;
        
        return authenticationInfo;
    }

    public void updateKeyChain(String oldUserName, String oldPassword) {
        if (type == AuthenticationType.USER_PASSWORD && !temporary && DatabaseCredentialManager.USE) {
            oldUserName = nvl(oldUserName);
            oldPassword = nvl(oldPassword);

            String newUserName = nvl(user);
            String newPassword = nvl(password);

            boolean userNameChanged = !Commons.match(oldUserName, newUserName);
            boolean passwordChanged = !Commons.match(oldPassword, newPassword);
            if (userNameChanged || passwordChanged) {
                DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
                ConnectionId connectionId = getConnectionId();

                if (userNameChanged) {
                    credentialManager.removePassword(connectionId, oldUserName);
                }
                if (Strings.isNotEmpty(newUserName) && Strings.isNotEmpty(newPassword)) {
                    credentialManager.setPassword(connectionId, newUserName, newPassword);
                }
            }
        }
    }
}
