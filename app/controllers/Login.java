package controllers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import views.html.sketchness_login;

public class Login extends Controller {

    public static Result login() {
        return ok(sketchness_login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));

    }


    public static Result doLogin() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(sketchness_login.render(filledForm));
        }
        else {
            // Everything was filled
            return UsernamePasswordAuthProvider.handleLogin(ctx());
        }
    }


}
