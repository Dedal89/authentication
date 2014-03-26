package models;

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

    public void updateStatus(String user, String provider, String token){
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement1 = null;
        Statement statement3 = null;
        ResultSet rs = null;
        ResultSet rs3 = null;
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
                Logger.info("User " + user+" with provider: "+ provider+" updated");
            }
            else{
                Logger.info("User " + user+" with provider: "+ provider+" doesn't exists");
                final String query3 = "SELECT id from AUTHTOKEN  ORDER BY id DESC LIMIT 1";
                try{
                    statement3 = connection.createStatement();
                    rs3 =statement3.executeQuery(query3);
                    if(rs3.isBeforeFirst()){
                        rs3.next();
                        maxId = rs3.getLong("ID");
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
                final String query4 = "INSERT INTO AUTHTOKEN (ID,USER, PROVIDER, TOKEN) VALUES (?,?,?,?)";
                statement1 = connection.prepareStatement(query4);
                statement1.setLong(1, maxId);
                statement1.setString(2, user);
                statement1.setString(3, provider);
                statement1.setString(4, token);
                statement1.executeUpdate();
                Logger.info("User " + user+" with provider: "+ provider+" created with id: "+maxId);
            }

        } catch (final SQLException ex) {
            Logger.error("Unable to save values for: " + user+" provider: "+ provider);
           // throw new Exception("Unable to change nickname for user: " + user);
        } finally {
            try {
                if (statement != null)
                    statement.close();

                if (connection != null)
                    connection.close();
            } catch (final SQLException e) {
                play.Logger.error("Unable to close a SQL connection.");
            }

        }

    }

}