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
            this.client = new CubrikObjectStore("src/test/resources/client.properties");
        } catch (IOException e) {
            Logger.error("Something went wrong during cubrik object store setup");
            e.printStackTrace();
        } catch (PropertyNotFoundException e) {
            Logger.error("Something went wrong during cubrik object store setup");
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
