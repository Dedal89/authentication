package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import org.json.JSONArray;
import org.json.JSONObject;
import play.data.validation.Constraints;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import models.TokenAction.Type;
import play.Logger;
import play.data.format.Formats;
import play.db.ebean.Model;
import providers.MyUsernamePasswordAuthUser;

import play.db.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Random;

import javax.persistence.*;
import java.sql.Connection;
import java.util.*;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
@Table(name = "users")
public class User extends Model implements Subject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	//@Column(unique = true)
	public String email;

	public String name;

    public String companyName;
    public String mainInterests;
    public String businessDimension;
    public String city;

	public String firstName;

	public String lastName;


    public String nation;


	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastLogin;

	public boolean active;

	public boolean emailValidated;

	@ManyToMany
	public List<SecurityRole> roles;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	@ManyToMany
	public List<UserPermission> permissions;

	public static final Finder<Long, User> find = new Finder<Long, User>(
			Long.class, User.class);

	@Override
	public String getIdentifier()
	{
		return Long.toString(id);
	}

	@Override
	public List<? extends Role> getRoles() {
		return roles;
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return permissions;
	}

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		final ExpressionList<User> exp;
		if (identity instanceof UsernamePasswordAuthUser) {
			exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
		} else {
			exp = getAuthUserFind(identity);
		}
		return exp.findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.where().eq("active", true)
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider());
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else {
			return getAuthUserFind(identity).findUnique();
		}
	}

	public static User findByUsernamePasswordIdentity(
			final UsernamePasswordAuthUser identity) {
		return getUsernamePasswordAuthUserFind(identity).findUnique();
	}

	private static ExpressionList<User> getUsernamePasswordAuthUserFind(
			final UsernamePasswordAuthUser identity) {
		return getEmailUserFind(identity.getEmail()).eq(
				"linkedAccounts.providerKey", identity.getProvider());
	}

	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.roles = Collections.singletonList(SecurityRole
				.findByRoleName(controllers.Application.USER_ROLE));
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.active = true;
		user.lastLogin = new Date();
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
            user.email = identity.getEmail();


            if(user.checkMail(identity.getEmail())){

                return user;
            }

			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
                user.name = user.checkNickname(name);
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
		    user.firstName = firstName;
		  }
		  if (lastName != null) {
		    user.lastName = lastName;
		  }
		}

        if (authUser instanceof MyUsernamePasswordAuthUser){

            final MyUsernamePasswordAuthUser identity = (MyUsernamePasswordAuthUser) authUser;
            user.email = identity.getEmail();
            user.emailValidated = false;
            user.name = identity.getName();

            user.nation = identity.getNation();
            user.companyName = identity.getCompanyName();
            user.businessDimension = identity.getBusinessDimension();
            user.mainInterests = identity.getMainInterests();
            user.city = identity.getCity();
        }



		user.save();
		user.saveManyToManyAssociations("roles");
		// user.saveManyToManyAssociations("permissions");
		return user;
	}

    public String retrieveUser(String id){
        String result;
        JSONObject content = new JSONObject();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs;
        try{
            connection = DB.getConnection();
            final String query = "SELECT * FROM USERS WHERE ID=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, id);
            rs = statement.executeQuery();
            if(rs.isBeforeFirst()){
                rs.next();
                content.put("id",id);
                content.put("email",rs.getString("email"));
                content.put("name",rs.getString("name"));
                content.put("companyName",rs.getString("company_name"));
                content.put("mainInterests",rs.getString("main_interests"));
                content.put("businessDimensions",rs.getString("business_dimension"));
                content.put("city",rs.getString("city"));
                content.put("firstname",rs.getString("first_name"));
                content.put("lastname",rs.getString("last_name"));
                content.put("country",rs.getString("nation"));
                rs.close();
                statement.close();
                final String query1 = "SELECT * FROM AUTHTOKEN WHERE USER=?";
                statement = connection.prepareStatement(query1);
                statement.setString(1, id);
                rs = statement.executeQuery();
                while(rs.next()){
                    content.put(rs.getString("provider"),rs.getString("token"));
                    content.put("cubrik-storage-psw",rs.getString("cubrikpsw"));
                    content.put("cubrik-storage-token",rs.getString("cubriktoken"));
                }
            }
            else{
                content.put("error","there is no user with id: "+id);
            }
        }
        catch(Exception e){
            Logger.error("Error during connection for retrieving user "+id+" data.");
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
        result = content.toString();
        return result;
    }

    public String checkNickname(String nickname){

        try{
            Connection connection = DB.getConnection();

            String query = "SELECT * FROM USERS WHERE NAME=? ";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nickname);
            ResultSet rs = statement.executeQuery();

            if(rs.isBeforeFirst()){
                Random randomGeneretor = new Random();
                int n = randomGeneretor.nextInt(5000);
                int n2 = randomGeneretor.nextInt(5000);
                n = n + n2;
                String ns = Integer.toString(n);
                nickname=  nickname+ns;
            }

        }
        catch(SQLException ex){

        }


        return nickname;
    }

    public boolean checkMail(String email){

        try{
            Connection connection = DB.getConnection();

            String query = "SELECT * FROM USERS WHERE EMAIL=? ";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if(rs.isBeforeFirst()){
                return true;
            }

        }
        catch(SQLException ex){

        }


        return false;
    }

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}

	public static User findByEmail(final String email) {
		return getEmailUserFind(email).findUnique();
	}

	private static ExpressionList<User> getEmailUserFind(final String email) {
		return find.where().eq("active", true).eq("email", email);
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.user = this;
			} else {
				throw new RuntimeException(
						"Account not enabled for password usage");
			}
		}
		a.providerUserId = authUser.getHashedPassword();
		a.save();
	}

    public void changeNickname(final UsernamePasswordAuthUser authUser,String newNick) {

              LinkedAccount a = this.getAccountByProvider(authUser.getProvider());

              if (a == null) {

                    a = LinkedAccount.create(authUser);
                    a.user = this;

              }

              Long tempo = a.user.id;
              String utente = Long.toString(tempo);

              newNick = checkNickname(newNick);

              try{
              Connection connection = DB.getConnection();

              String query = "UPDATE USERS SET NAME = ? WHERE ID = ? ";
              PreparedStatement statement = connection.prepareStatement(query);
              statement.setString(1, newNick);
              statement.setString(2, utente);
              statement.executeUpdate();

              }
              catch(SQLException ex){

              }

    }

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
}
