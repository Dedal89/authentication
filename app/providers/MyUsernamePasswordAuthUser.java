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
    private String companyName;
    private String mainInterests;
    private String businessDimension;
    private String city;


	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
		this.name = signup.name;
        this.nation = signup.nation;
        this.companyName = signup.companyName;
        this.mainInterests = signup.mainInterests;
        this.businessDimension = signup.businessDimension;
        this.city = signup.city;
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

    public String getCompanyName() {
        return companyName;
    }

    public String getMainInterests() {
        return mainInterests;
    }

    public String getBusinessDimension() {
        return businessDimension;
    }

    public String getCity() {
        return city;
    }

}
