package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.AuthToken;
import models.SecurityInfoShare;
import models.User;
import net.minidev.json.JSONObject;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.Play;
import play.Routes;
import play.api.libs.json.JsPath;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
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
    private final static String redirectUrl = Play.application().configuration()
            .getString("redirectUrl");
	
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

        JSONObject content = new JSONObject();
        content.put("userid", localUser.getIdentifier());
        content.put("username", localUser.name);
        content.put("provider", session().get("pa.p.id"));
        content.put("token", session().get("oauthaccessToken"));
        String payload = content.toJSONString();

        SecurityInfoShare sis = new SecurityInfoShare();
        sis.loadKey();
        String result = sis.encrypt(payload);

        session().put("token", result);

        return redirect(redirectUrl);

	}

    public static Result retrievePublicKey(){
        String ipAddress = request().getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request().remoteAddress();
            Logger.info(ipAddress);
        }
        //TODO fare whitelist degli ip accettati
        SecurityInfoShare sis = new SecurityInfoShare();
        sis.loadKey();
        String content  = sis.getPublicKey();
        JSONObject data = new JSONObject();
        data.put("publicKey", content);
        response().setContentType("application/json");
        return ok(data.toJSONString());
    }

    public static Result retrieveUser(){
        String key;
        String dataEncrypted;
        String data;
        JsonNode content;
        String idUser = null;
        boolean success;
        SecurityInfoShare sis = new SecurityInfoShare();
        JsonNode body = request().body().asJson();
        if(body != null){
            if(body.has("data")){
                dataEncrypted = body.get("data").asText();
                success = sis.loadKey();
                if(success){
                    data = sis.encrypt(dataEncrypted);
                    //TODO parse data into a json and exctract idUser
                }
                else{
                    return internalServerError();
                }
            }
            else{
                return badRequest("Missing user id");
            }
            if(body.has("publicKey")){
                key = body.get("publicKey").asText();
            }
            else{
                return badRequest("Missing public key");
            }
            success = sis.setSpecificPublicKey(key);
            if(success){
                User user = new User();
                String userdata = user.retrieveUser(idUser);
                String result = sis.encrypt(userdata);
         //     //  result = test(result);
            return ok(result);
            }
            else{
                return internalServerError();
            }
        }
        else{
            return badRequest("missing body, content-type must be application/json");
        }
    }

    public static Result doExternalLogin(){
        SecurityInfoShare sis = new SecurityInfoShare();
        JsonNode body = request().body().asJson();
        String dataEncrypted;
        String data;
        JSONObject content;
        if(body != null){
            if(body.has("data")){
                dataEncrypted = body.get("data").asText();
                if(body.has("publicKey")){
                    sis.loadKey();
                    data = sis.decrypt(dataEncrypted);

                    //TODO parse data into json object, extract authentication fields, doLogin, retrieve user id, sis.setSpecificPublicKey(body.get("publicKey").asText()), sis.encrypt(id), response back




                    return ok();
                }
                else{
                    return badRequest("publicKey field is missing");
                }
            }
            else{
                return badRequest("data field is missing");
            }
        }
        else{
            return badRequest("missing body, content-type must be application/json");
        }
    }

    public static Result doExternalSignup(){
        SecurityInfoShare sis = new SecurityInfoShare();
        JsonNode body = request().body().asJson();
        String dataEncrypted;
        String data;
        JSONObject content;
        if(body != null){
            if(body.has("data")){
                dataEncrypted = body.get("data").asText();
                if(body.has("publicKey")){
                    sis.loadKey();
                    data = sis.decrypt(dataEncrypted);

                    //TODO parse data into json object, extract authentication fields, check user exists and create user, retrieve user id, sis.setSpecificPublicKey(body.get("publicKey").asText()), sis.encrypt(id), response back




                    return ok();
                }
                else{
                    return badRequest("publicKey field is missing");
                }
            }
            else{
                return badRequest("data field is missing");
            }
        }
        else{
            return badRequest("missing body, content-type must be application/json");
        }
    }

    public static Result doExternalProvider(String provider){
        //TODO log with provider, create user, retrieve id, encrypt it with received public key, send it back


        return ok();
    }


    public static String test(String text){
        SecurityInfoShare sis = new SecurityInfoShare();
        String result= "error in loading key";
        boolean success = sis.loadKey();
        if(success){
            Logger.info("before >>>>>>>>> "+text);
            result = sis.decrypt(text);
            Logger.info("after >>>>>>>>>> "+result);
        }
        return result;
    }

    public static Result test2(){
        String result;
        String token = session().get("token");
        SecurityInfoShare sis = new SecurityInfoShare();
        boolean success = sis.loadKey();
        if(success){
            result = sis.decrypt(token);
            return ok(result);
        }
        else{
            return internalServerError();
        }
    }

	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

    public static Result forceKeyCreation() {
        SecurityInfoShare sis = new SecurityInfoShare();
        boolean result = sis.createKey();

        if(result){
            return ok();
        }
        else{
            return internalServerError();
        }
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