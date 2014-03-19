package models;

import play.Logger;
import play.db.DB;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Riboni1989 on 19/03/14.
 */

@Entity
@Table(name = "authToken")
public class AuthToken extends Model {


    public String user;
    public String provider;

    public String token;

    public void updateStatus(String user, String provider, String token){
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement1 = null;
        ResultSet rs = null;
        try {
            connection = DB.getConnection();

            final String query = "SELECT * FROM AUTHTOKEN WHERE USER=? AND PROVIDER=? ";

            statement = connection.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, provider);
            rs = statement.executeQuery();

            if (rs.isBeforeFirst()) {
                final String query2 = "UPDATE AUTHTOKEN SET TOKEN=? WHERE USER=? AND PROVIDER=? ";
                statement1 = connection.prepareStatement(query2);
                statement1.setString(1, token);
                statement1.setString(2, user);
                statement1.setString(3, provider);
                statement1.executeUpdate();
            }
            else{
                final String query3 = "INSERT INTO AUTHTOKEN (USER, PROVIDER, TOKEN) VALUES (?,?,?)";
                statement1 = connection.prepareStatement(query3);
                statement1.setString(1, user);
                statement1.setString(2, provider);
                statement1.setString(3, token);
                statement1.executeUpdate();
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