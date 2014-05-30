package models;

import play.Logger;
import play.db.DB;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.*;

/**
 * Created by Riboni1989 on 30/05/14.
 */

@Entity
@Table(name = "otherUserInfo")
public class OtherUserInfo extends Model{
    @Id
    public Long id;
    public String user;
    public String jobPosition;
    public String mainInterests;
    public String areaOfSpecialization;
    public String yourLinkedin;
    public String yourResearcherGate;
    public String companyHomepage;
    public String yourHomepage;
    public String about;

    public void createInfo(String user) {
        final OtherUserInfo info = new OtherUserInfo();
        info.user = user;
        info.jobPosition = "none";
        info.mainInterests = "none";
        info.areaOfSpecialization = "none";
        info.yourLinkedin = "none";
        info.yourResearcherGate = "none";
        info.companyHomepage = "none";
        info.yourHomepage = "none";
        info.about = "none";
        info.save();
    }

    public void loadInfo(String user){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs;

        try{
            connection = DB.getConnection();
            final String query = "SELECT * FROM OTHERUSERINFO WHERE USER=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, user);
            rs = statement.executeQuery();
            if (rs.isBeforeFirst()) {
                Logger.info("User " + user+" retrieved");
                rs.next();
                this.id = rs.getLong("id");
                this.user = user;
                this.jobPosition = rs.getString("job_position");
                this.mainInterests = rs.getString("main_interests");
                this.areaOfSpecialization = rs.getString("area_of_specialization");
                this.yourLinkedin = rs.getString("your_linkedin");
                this.yourResearcherGate = rs.getString("your_researcher_gate");
                this.companyHomepage = rs.getString("company_homepage");
                this.yourHomepage = rs.getString("your_homepage");
                this.about = rs.getString("about");
            }
            else{
                this.id = null;
                this.user = user;
                this.jobPosition = "none";
                this.mainInterests = "";
                this.areaOfSpecialization = "none";
                this.yourLinkedin = "none";
                this.yourResearcherGate = "none";
                this.companyHomepage = "none";
                this.yourHomepage = "none";
                this.about = "none";
            }
        }
        catch(Exception e){
            Logger.error("Error during connection to retrieve user: "+user+" additional info");
        }
        finally {
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (final SQLException e) {
                Logger.error("Unable to close a SQL connection.");
            }
        }
    }

    public void updateInfo() {

        Connection connection= null;
        PreparedStatement statement = null;

        try {
            connection = DB.getConnection();

            final String query = "UPDATE OTHERUSERINFO SET job_position=?, main_interests=?, area_of_specialization=?, your_linkedin=?, your_researcher_gate=?, company_homepage=?, your_homepage=?, about=? WHERE USER=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, this.jobPosition);
            statement.setString(2, this.mainInterests);
            statement.setString(3, this.areaOfSpecialization);
            statement.setString(4, this.yourLinkedin);
            statement.setString(5, this.yourResearcherGate);
            statement.setString(6, this.companyHomepage);
            statement.setString(7, this.yourHomepage);
            statement.setString(8, this.about);
            statement.setString(9, this.user);

            statement.executeUpdate();
            Logger.info("User " + user+" created with id: "+this.id);
        }
        catch(Exception e){
            Logger.error("Unable to update other user info");
        }
        finally {
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (final SQLException e) {
                Logger.error("Unable to close a SQL connection.");
            }
        }

    }

    public void setJobPosition(String jobPosition){
        this.jobPosition = jobPosition;
        updateInfo();
    }
    public void setMainInterests(String mainInterests){
        loadInfo(this.user);
        this.mainInterests = mainInterests;
        updateInfo();
    }
    public void setAreaOfSpecialization(String areaOfSpecialization){
        loadInfo(this.user);
        this.areaOfSpecialization = areaOfSpecialization;
        updateInfo();
    }
    public void setYourLinkedin(String yourLinkedin){
        loadInfo(this.user);
        this.yourLinkedin = yourLinkedin;
        updateInfo();
    }
    public void setYourResearcherGate(String yourResearcherGate){
        loadInfo(this.user);
        this.yourResearcherGate = yourResearcherGate;
        updateInfo();
    }
    public void setCompanyHomepage(String companyHomepage){
        loadInfo(this.user);
        this.companyHomepage = companyHomepage;
        updateInfo();
    }
    public void setYourHomepage(String yourHomepage){
        loadInfo(this.user);
        this.yourHomepage = yourHomepage;
        updateInfo();
    }
    public void setAbout(String about){
        loadInfo(this.user);
        this.about = about;
        updateInfo();
    }


}
