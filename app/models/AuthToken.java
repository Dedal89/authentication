package models;

import org.json.JSONArray;
import org.json.JSONObject;
import play.Logger;
import play.db.DB;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Riboni1989 on 19/03/14.
 */

@Entity
@Table(name = "authToken")
public class AuthToken extends Model {

    @Id
    public Long id;
    public String user;
    public String provider;
    public String token;
    public String cubrikpsw;
    public String cubriktoken;

    public void updateStatus(String user, String provider, String token){
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement1 = null;
        Statement statement2 = null;
        ResultSet rs;
        ResultSet rs2;
        Long maxId = 1L;
        try {
            connection = DB.getConnection();
            final String query = "SELECT * FROM AUTHTOKEN WHERE USER=? AND PROVIDER=? ";
            statement = connection.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, provider);
            rs = statement.executeQuery();
            if (rs.isBeforeFirst()) {
                Logger.info("User " + user+" with provider: "+ provider+" exists");
                final String query2 = "UPDATE AUTHTOKEN SET TOKEN=? WHERE USER=? AND PROVIDER=? ";
                statement1 = connection.prepareStatement(query2);
                statement1.setString(1, token);
                statement1.setString(2, user);
                statement1.setString(3, provider);
                statement1.executeUpdate();
                statement1.close();
                Logger.info("User " + user+" with provider: "+ provider+" updated");
            }
            else{
                Logger.info("User " + user+" with provider: "+ provider+" doesn't exists");
                final String query3 = "SELECT id from AUTHTOKEN  ORDER BY id DESC LIMIT 1";
                try{
                    statement2 = connection.createStatement();
                    rs2 =statement2.executeQuery(query3);
                    if(rs2.isBeforeFirst()){
                        rs2.next();
                        maxId = rs2.getLong("ID");
                        Logger.info("max id is: " +maxId);
                        maxId++;
                    }
                    else{
                        Logger.info("this will be the first row");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Logger.error("Error in retrieving max id");
                }

                //send registration to cubrik-storage
                cubrikpsw = "1234";
                cubriktoken = "none";
                /*
                try{
                    JSONObject userObj= new JSONObject();
                    userObj.put("login",user);
                    userObj.put("password",cubrikpsw);
                    JSONArray groups = new JSONArray();
                    groups.put("fu");
                    groups.put("bar");
                    groups.put("users");
                    userObj.put("groups",groups);
                    String jsonString = userObj.toString();
                }
                catch(Exception e){
                    Logger.error("Error during creation of JSON for cubrik-storage");
                }
                */

                CubrikComponent component = new CubrikComponent();
                cubriktoken = component.userAuthentication(user,cubrikpsw).toString();

                final String query4 = "INSERT INTO AUTHTOKEN (ID,USER, PROVIDER, TOKEN, CUBRIKPSW, CUBRIKTOKEN) VALUES (?,?,?,?,?,?)";
                statement1 = connection.prepareStatement(query4);
                statement1.setLong(1, maxId);
                statement1.setString(2, user);
                statement1.setString(3, provider);
                statement1.setString(4, token);
                statement1.setString(5, cubrikpsw);
                statement1.setString(6, cubriktoken);
                statement1.executeUpdate();
                Logger.info("User " + user+" with provider: "+ provider+" created with id: "+maxId);
            }
        } catch (final SQLException ex) {
            Logger.error("Unable to save values for: " + user+" provider: "+ provider);
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (statement1 != null)
                    statement1.close();
                if (statement2 != null)
                    statement2.close();
                if (connection != null)
                    connection.close();
            } catch (final SQLException e) {
                play.Logger.error("Unable to close a SQL connection.");
            }
        }
    }
}