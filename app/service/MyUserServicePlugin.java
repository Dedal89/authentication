package service;

import com.feth.play.module.pa.providers.oauth1.OAuth1AuthUser;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;
import models.AuthToken;
import models.User;
import play.Application;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;
import play.mvc.Http;


public class MyUserServicePlugin extends UserServicePlugin {

	public MyUserServicePlugin(final Application app) {
		super(app);
	}

	@Override
	public Object save(final AuthUser authUser) {
		final boolean isLinked = User.existsByAuthUserIdentity(authUser);
		if (!isLinked) {
            if (authUser instanceof EmailIdentity) {
                final EmailIdentity identity = (EmailIdentity) authUser;
                final User user = new User();
                // Remember, even when getting them from FB & Co., emails should be
                // verified within the application as a security breach there might
                // break your security as well!
                user.email = identity.getEmail();


                if(user.checkMail(identity.getEmail())){

                    return null;
                }


            }

			return User.create(authUser).id;
		} else {
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		final User u = User.findByAuthUserIdentity(identity);
		if(u != null) {
			return u.id;
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
		if (!oldUser.equals(newUser)) {
			User.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
		User.addLinkedAccount(oldUser, newUser);
		return newUser;
	}

	@Override
	public AuthUser update(final AuthUser knownUser) {
		// User logged in again, bump last login date

        String token = null;
        String provider = "password";
        String userId = Http.Context.current().session().get("userId");

        if (knownUser instanceof OAuth2AuthUser)
        {
            OAuth2AuthUser oAuth2AuthUser = (OAuth2AuthUser) knownUser;
            token = oAuth2AuthUser.getOAuth2AuthInfo().getAccessToken();
            provider = oAuth2AuthUser.getProvider();
        }

        if (knownUser instanceof OAuth1AuthUser)
        {
            OAuth1AuthUser oAuth1AuthUser = (OAuth1AuthUser) knownUser;
            token = oAuth1AuthUser.getOAuth1AuthInfo().getAccessToken();
            provider = oAuth1AuthUser.getProvider();
        }
        if(!provider.equals("password")){
            AuthToken auth = new AuthToken();
            auth.updateStatus(userId,provider, token);
        }
        User.setLastLoginDate(knownUser);
		return knownUser;
	}

}
