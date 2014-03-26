package controllers;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;


import models.AuthToken;
import models.User;
import net.minidev.json.JSONObject;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String USER_ROLE = "user";
	
	public static Result index() {
		return ok(index.render());
	}


	public static User getLocalUser(final Session session) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
		final User localUser = User.findByAuthUserIdentity(currentAuthUser);
		return localUser;
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result restricted() {
		final User localUser = getLocalUser(session());

        AuthToken auth = new AuthToken();
        auth.updateStatus(localUser.getIdentifier(),session().get("pa.p.id"), session().get("oauthaccessToken"));

     //   response().setCookie("token", session().get("oauthaccessToken"));

        JSONObject content = new JSONObject();
        content.put("userid", localUser.getIdentifier());
        content.put("username", localUser.name);
        content.put("provider", session().get("pa.p.id"));
        content.put("token", session().get("oauthaccessToken"));

        String payload = content.toJSONString();

        Payload userToken = new Payload(payload);
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setContentType("text/plain");
        JWSObject jwsObject = new JWSObject(header, userToken);
        String sharedKey = "a0a2abd8-6162-41c3-83d6-1cf559b46afc";
        JWSSigner signer = new MACSigner(sharedKey.getBytes());
        try{
            jwsObject.sign(signer);
        }
        catch(Exception e){
            Logger.error("Error signing jwsObject");
        }
        String serializedObj = jwsObject.serialize();
        Logger.info("Serialised JWS object: " + serializedObj);

        //   Code to parse, verify and read the payload, use as example
        try{
            jwsObject = JWSObject.parse(serializedObj);
            JWSVerifier verifier = new MACVerifier(sharedKey.getBytes());
            boolean verifiedSignature = jwsObject.verify(verifier);
            if (verifiedSignature){
                Logger.info("Verified JWS signature");
            }
            else{
                Logger.info("Bad JWS signature");
            }
            Logger.info("Recovered payload message: " + jwsObject.getPayload());
        }
        catch(ParseException e){
            Logger.error("Error in parsing jwsObject");
        }
        catch (JOSEException e){
            Logger.error("Error in verifying jwsObject");
        }

        return redirect("http://www.prova.com/?token="+serializedObj);
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

	public static Result login() {
		return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}


	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(login.render(filledForm));
		} else {
			// Everything was filled
			return UsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}




	public static Result signup() {
		return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}


	public static Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes",
						controllers.routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
	}



	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}



	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

}