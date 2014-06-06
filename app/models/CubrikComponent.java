package models;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import play.Logger;
import play.Play;
import play.db.ebean.Model;
import de.fraunhofer.idmt.cubrik.storageclient.AuthenticationToken;
import de.fraunhofer.idmt.cubrik.storageclient.BasicAuth;
import de.fraunhofer.idmt.cubrik.storageclient.CubrikObjectStore;
import de.fraunhofer.idmt.cubrik.storageclient.PropertyNotFoundException;

/**
 * Created by Riboni1989 on 19/05/14.
 */
public class CubrikComponent extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static AuthenticationToken token;
	private static CubrikObjectStore client = null;
	private static String cosUrl;

	public static void setCosUrl() {
		final Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("conf/client.properties");
			prop.load(input);
			cosUrl = prop.getProperty("uri");
		} catch (final FileNotFoundException e) {
			Logger.error("Something went wrong during cubrik object store setup");
			e.printStackTrace();
		} catch (final IOException e) {
			Logger.error("Something went wrong during cubrik object store setup");
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static String getComponentId() {
		return Play.application().configuration().getString("component_id");
	}

	public static String getComponentPassword() {
		return Play.application().configuration().getString("component_psw");
	}

	public static String getAdminId() {
		return Play.application().configuration().getString("admin_id");
	}

	public static String getAdminPassword() {
		return Play.application().configuration().getString("admin_psw");
	}

	public AuthenticationToken getToken() {
		return token;
	}

	public static CubrikObjectStore getClient() {
		return client;
	}

	public static void setAuthToken() {
		token = new AuthenticationToken(getComponentId(),
				getComponentPassword());
	}

	public static void setClient() {
		try {
			client = new CubrikObjectStore("conf/client.properties");
			client.setAuthorizationHeader(BasicAuth.encode(getAdminId(),
					getAdminPassword()));
			setCosUrl();
		} catch (final IOException e) {
			Logger.error("Something went wrong during cubrik object store setup");
			e.printStackTrace();
		} catch (final PropertyNotFoundException e) {
			Logger.error("Something went wrong during cubrik object store setup");
			e.printStackTrace();
		}

	}

	public void testAuth() {

		try {

			final String adminId = "test_admin";
			final String adminPsw = "1234";

			final CubrikObjectStore client = new CubrikObjectStore(
					"conf/client.properties");
			// component authentication
			final String componentId = "test_component";
			final String componentPassword = "1234";
			token = new AuthenticationToken(componentId, componentPassword);

			client.setAuthorizationHeader(BasicAuth.encode(adminId, adminPsw));

			final String jsonString =
					"{'login':'idUsr1new', 'password':'pswUser', 'groups':['cubrik_users', 'users']}";
			final String oid = client.insertDocument(jsonString,
					"cubrik_users", token);

			final String BUCKET = "os_client_test_bucket.files";

			// SourceId and License are mandatory, besides that you are free to
			// add arbitrary information
			final String JSON =
					"{\"SourceId\":\"flickr\",\"License\":\"CC-BY\"}";

			// step 2: upload binary data
			final String objectId = client.putBinaryObject(BUCKET,
					new FileInputStream(
							"/Users/chiarapasini/Pictures/ADP/1111.jpg"),
							"testimageddd", "image/jpeg", token);

			// step 3: add metadata for the binary file
			client.updateObjectMetadata(BUCKET, objectId, JSON, token);

			final String tokenUsr = token
					.sign("/objectstore/binary/get/os_client_test_bucket.files/538ee8b3bbd554bb0a81683b",
							null);

			// final String tokenUsr = token.sign("/objectstore/binary/get/"
			// + BUCKET + "/" + objectId + "", null);
			Logger.debug(tokenUsr.toString());

			final String userToken = BasicAuth.encode("idUsr1new", "pswUser");

			Logger.debug(userToken);

			// Logger.debug(tokenUsr.toString());
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	public static String saveUser(final String name, final String psw) {
		if (getClient() == null) {
			setClient();
		}
		if (token == null) {
			setAuthToken();
		}

		final String jsonString = "{'login':'" + name + "', 'password':'" + psw
				+ "', 'groups':['cubrik_users', 'users']}";
		try {
			client.insertDocument(jsonString, "cubrik_users", token);
			final String userToken = BasicAuth.encode(name, psw);
			return userToken;
		} catch (final Exception e) {
			Logger.error("Something went wrong during cubrik object store, creating user");
			e.printStackTrace();
		}

		return "";

	}

	public static String buildUserToken(final String usrId,
			final String usrPsw, final String objectId) {
		final AuthenticationToken token = new AuthenticationToken(usrId, usrPsw);
		String tokenUsr;
		if (cosUrl == null) {
			setCosUrl();
		}
		try {
			final String path = objectId.replace(cosUrl, "/objectstore/");
			tokenUsr = token.sign(path, null);
			return tokenUsr.toString();
		} catch (final Exception e) {
			Logger.error("Something went wrong building token");
			e.printStackTrace();
		}
		return "";

	}

	public static InputStream readObject(final String usrId,
			final String usrPsw, final String objectId) {

		if (getClient() == null) {
			setClient();
		}

		CubrikObjectStore clientUser;
		try {
			clientUser = new CubrikObjectStore("conf/client.properties");
			clientUser.setAuthorizationHeader(BasicAuth.encode(usrId, usrPsw));


			if (token == null) {
				setAuthToken();
			}



			final String bckObj = objectId.replace(cosUrl + "binary/get/", "");
			final List<String> items = Arrays.asList(bckObj.split("\\s*/\\s*"));
			InputStream objStream;
			objStream = clientUser
					.readObject(items.get(0), items.get(1), token);
			return objStream;

		} catch (final Exception e) {
			Logger.error("Something went wrong building token");
			e.printStackTrace();
		}
		return null;

	}



	public static String buildObjToken(final String objectId) {
		if (token == null) {
			setAuthToken();
		}
		if (cosUrl == null) {
			setCosUrl();
		}
		final String path = objectId.replace(cosUrl, "/objectstore/");
		String tokenObj = null;
		try {
			tokenObj = token.sign(path, null);
		} catch (final Exception e) {
			Logger.error("Something went wrong building token");
			e.printStackTrace();
		}
		return tokenObj;
	}

}
