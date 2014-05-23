package models;

import de.fraunhofer.idmt.cubrik.storageclient.*;
import play.Logger;
import play.db.ebean.Model;

import java.io.IOException;

/**
 * Created by Riboni1989 on 19/05/14.
 */
public class CubrikComponent extends Model {
    private String componentId = "test_component";
    private String componentPassword = "1234";
    private AuthenticationToken token;
    private CubrikObjectStore client = null;

    public String getComponentId(){
        return this.componentId;
    }

    public String getComponentPassword(){
        return this.componentPassword;
    }

    public AuthenticationToken getToken(){
        return this.token;
    }

    public CubrikObjectStore getClient(){
        return this.client;
    }

    public void setComponentId(String id){
        this.componentId = id;
    }

    public void setComponentPassword(String psw){
        this.componentPassword = psw;
    }

    public void setAuthToken(){
        this.token = new AuthenticationToken(getComponentId(), getComponentPassword());
    }

    public void setClient(){
        try {
            this.client = new CubrikObjectStore("conf/client.properties");
        } catch (IOException e) {
            Logger.error("Something went wrong during cubrik object store setup");
            e.printStackTrace();
        } catch (PropertyNotFoundException e) {
            Logger.error("Something went wrong during cubrik object store setup");
            e.printStackTrace();
        }

    }

    public void testAuth(){

        try {
            String login = "test_admin";
            String password = "1234";

            // Test BasicAuth class
            String enc = BasicAuth.encode(login, password);

            AuthenticationToken token = new AuthenticationToken(
                    "test_component", "1234");
            CubrikObjectStore cos = new CubrikObjectStore("conf/client.properties");
            Logger.debug(token.toString());

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


    public AuthenticationToken userAuthentication(String userId, String userPassword){
        if(getClient() == null){
            setClient();
        }
        this.client.setAuthorizationHeader(BasicAuth.encode(userId, userPassword));
        return new AuthenticationToken("dummy_component","fubar");
    }





}
