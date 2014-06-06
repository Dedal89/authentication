package models;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import models.TokenAction.Type;

import org.json.JSONArray;
import org.json.JSONObject;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.DB;
import play.db.ebean.Model;
import providers.MyUsernamePasswordAuthUser;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;

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
	// @Column(unique = true)
	public String email;

	public String name;

	public String companyName;
	public String affiliation;
	public String businessDimension;
	public String city;
	public String fieldOfExpertise;
	public String firstName;
	public int yearOfExperience;
	public String lastName;
	public boolean showMail;
	public String appCode;
	public String cosObjId;
	public String cosPsw;
	public String cosTkn;

	public String country;

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
	public String getIdentifier() {
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

			if (user.checkMail(identity.getEmail())) {

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

		if (authUser instanceof MyUsernamePasswordAuthUser) {

			final MyUsernamePasswordAuthUser identity = (MyUsernamePasswordAuthUser) authUser;
			user.email = identity.getEmail();
			user.emailValidated = false;
			user.name = identity.getName();

			user.country = identity.getCountry();
			user.companyName = identity.getCompanyName();
			user.affiliation = identity.getAffiliation();
			user.businessDimension = identity.getBusinessDimension();
			user.city = identity.getCity();
			user.fieldOfExpertise = identity.getFieldOfExpertise();
			user.yearOfExperience = identity.getYearOfExperience();
			user.showMail = identity.getShowMail();
			user.appCode = identity.getAppCode();
		}
		final SecureRandom random = new SecureRandom();
		user.cosPsw = new BigInteger(60, random).toString(5);
		user.cosObjId = new BigInteger(60, random).toString(5);

		user.cosTkn = CubrikComponent.saveUser(user.cosObjId, user.cosPsw);

		user.save();
		user.saveManyToManyAssociations("roles");
		// user.saveManyToManyAssociations("permissions");
		final OtherUserInfo otherInfo = new OtherUserInfo();
		otherInfo.createInfo(user.getIdentifier());
		return user;
	}

	public String retrieveUserCOS(final String id) {
		String result;
		String content = "";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs;
		try {
			connection = DB.getConnection();
			final String query = "SELECT * FROM USERS WHERE ID=?";
			statement = connection.prepareStatement(query);
			statement.setString(1, id);
			rs = statement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				content = rs.getString("cos_obj_id") + ","
						+ rs.getString("cos_psw") + ","
						+ rs.getString("cos_tkn");


				rs.close();
				statement.close();

			}
		} catch (final Exception e) {
			Logger.error("Error during connection for retrieving user " + id
					+ " data.");
		} finally {
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

	public String retrieveUser(final String id) {
		String result;
		final JSONObject content = new JSONObject();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs;
		try {
			connection = DB.getConnection();
			final String query = "SELECT * FROM USERS WHERE ID=?";
			statement = connection.prepareStatement(query);
			statement.setString(1, id);
			rs = statement.executeQuery();
			if (rs.isBeforeFirst()) {
				rs.next();
				content.put("id", id);
				content.put("email", rs.getString("email"));
				content.put("name", rs.getString("name"));
				content.put("companyName", rs.getString("company_name"));
				content.put("affiliation", rs.getString("affiliation"));
				content.put("businessDimensions",
						rs.getString("business_dimension"));
				content.put("city", rs.getString("city"));
				content.put("firstname", rs.getString("first_name"));
				content.put("lastname", rs.getString("last_name"));
				content.put("country", rs.getString("country"));
				content.put("fieldOfExpertise",
						rs.getString("field_of_expertise"));
				content.put("yearOfExperience", rs.getInt("year_of_experience"));
				content.put("showMail", rs.getBoolean("show_mail"));
				content.put("appCode", rs.getString("app_code"));
				content.put("cosObjId", rs.getString("cos_obj_id"));
				content.put("cosPsw", rs.getString("cos_psw"));
				content.put("cosTkn", rs.getString("cos_tkn"));
				// Save additional information regarding the user
				content.put("otherUserInfo", new OtherUserInfo().loadInfo(id));
				rs.close();
				statement.close();
				final String query1 = "SELECT * FROM AUTHTOKEN WHERE USER=?";
				statement = connection.prepareStatement(query1);
				statement.setString(1, id);
				rs = statement.executeQuery();
				while (rs.next()) {
					content.put(rs.getString("provider"), rs.getString("token"));
					content.put("cubrik-storage-psw", rs.getString("cubrikpsw"));
					content.put("cubrik-storage-token",
							rs.getString("cubriktoken"));
				}
			} else {
				content.put("error", "there is no user with id: " + id);
			}
		} catch (final Exception e) {
			Logger.error("Error during connection for retrieving user " + id
					+ " data.");
		} finally {
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

	public String retrieveAllUser() {
		String result = "";
		final JSONArray payload = new JSONArray();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs;
		try {
			connection = DB.getConnection();
			// final String query = "SELECT * FROM USERS";
			final String query = "SELECT id FROM USERS";
			statement = connection.createStatement();
			rs = statement.executeQuery(query);
			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					final JSONObject content = new JSONObject();
					content.put("id", rs.getString("id"));

					payload.put(content);
				}
				rs.close();
				statement.close();
			} else {
				final JSONObject content = new JSONObject();
				content.put("error", "there is no user with id: " + id);
				payload.put(content);
			}
		} catch (final Exception e) {
			Logger.error("Error during connection for retrieving all user data.");
		} finally {
			try {
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (final SQLException e) {
				Logger.error("Unable to close a SQL connection.");
			}
		}

		result = payload.toString();
		return result;
	}

	public String checkNickname(String nickname) {
		Logger.debug("USER", "Checking if the nickname is already been used: "
				+ nickname);
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = DB.getConnection();

			final String query = "SELECT * FROM USERS WHERE NAME=? ";
			statement = connection.prepareStatement(query);
			statement.setString(1, nickname);
			rs = statement.executeQuery();

			if (rs.isBeforeFirst()) {
				// a user already exists with the same nickname, changing the
				// new nickname

				Logger.debug("USER",
						"A user already exists with the same nickname, changing the new nickname: "
								+ nickname);
				final Random randomGeneretor = new Random();
				int n = randomGeneretor.nextInt(5000);
				final int n2 = randomGeneretor.nextInt(5000);
				n = n + n2;
				final String ns = Integer.toString(n);
				nickname = nickname + ns;
				Logger.debug("USER", "New Nickname: " + nickname);
				checkNickname(nickname);
			}

		} catch (final SQLException ex) {
			// FIXME!!! If I was unable to verify the nickname I have to do
			// something
			Logger.error("Unable to verify nickname: " + nickname, ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null)
					statement.close();

				if (connection != null)
					connection.close();
			} catch (final SQLException e) {
				Logger.error("Unable to close a SQL connection.", e);
			}

		}

		return nickname;
	}

	public static boolean checkMail(final String email) {
		Logger.debug("USER", "Checking if the mail already exists: " + email);

		Boolean mailExists = false;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = DB.getConnection();

			final String query = "SELECT * FROM USERS WHERE EMAIL=? ";
			statement = connection.prepareStatement(query);
			statement.setString(1, email);
			rs = statement.executeQuery();

			if (rs.isBeforeFirst()) {
				Logger.debug("USER",
						"A user has already been registered with the email: "
								+ email);
				mailExists = true;
			}

		} catch (final SQLException ex) {
			Logger.error("Unable to check the email: " + email, ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (statement != null)
					statement.close();

				if (connection != null)
					connection.close();
			} catch (final SQLException e) {
				Logger.error("Unable to close a SQL connection.", e);
			}
		}
		return mailExists;
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

	public void changeNickname(final UsernamePasswordAuthUser authUser,
			String newNick) throws Exception {

		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());

		if (a == null) {

			a = LinkedAccount.create(authUser);
			a.user = this;

		}

		final Long tempo = a.user.id;
		final String utente = Long.toString(tempo);

		newNick = checkNickname(newNick);

		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = DB.getConnection();

			final String query = "UPDATE USERS SET NAME = ? WHERE ID = ? ";
			statement = connection.prepareStatement(query);
			statement.setString(1, newNick);
			statement.setString(2, utente);
			statement.executeUpdate();

		} catch (final SQLException ex) {
			Logger.error("Unable to change nickname for user: " + utente, ex);
			throw new Exception("Unable to change nickname for user: " + utente);
		} finally {
			try {
				if (statement != null)
					statement.close();

				if (connection != null)
					connection.close();
			} catch (final SQLException e) {
				Logger.error("Unable to close a SQL connection.", e);
			}
		}
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
}
