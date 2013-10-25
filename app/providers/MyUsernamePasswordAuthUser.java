package providers;

import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser
		implements NameIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String name;
    private String nation;
    private Boolean gender;


	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
        this.nation = signup.nation;
        this.gender = signup.gender;
	}

	/**
	 * Used for password reset only - do not use this to signup a user!
	 * @param password
	 */
	public MyUsernamePasswordAuthUser(final String password) {
		super(password, null);
		name = null;
	}


	@Override
	public String getName() {
		return name;
	}

    public String getNation() {
        return nation;
    }

    public Boolean getGender() {
        return gender;
    }


}
