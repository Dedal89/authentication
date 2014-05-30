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
    private String country;
    private String companyName;
    private String affiliation;
    private String businessDimension;
    private String city;
    private String fieldOfExpertise;
    private Integer yearOfExperience;
    private Boolean showMail;
    private String appCode;


	public MyUsernamePasswordAuthUser(final MySignup signup) {
		super(signup.password, signup.email);
        if(signup.name != null) this.name = signup.name;
        else this.name = "";
        if(signup.country != null) this.country = signup.country;
        else this.country = "";


        if(signup.companyName != null) this.companyName = signup.companyName;
        else this.companyName = "";
        if(signup.affiliation != null) this.affiliation = signup.affiliation;
        else this.affiliation = "";
        if(signup.businessDimension != null) this.businessDimension = signup.businessDimension;
        else this.businessDimension = "";
        if(signup.yearOfExperience != null) this.yearOfExperience = signup.yearOfExperience;
        else this.yearOfExperience = 0;
        if(signup.showMail != null) this.showMail = signup.showMail;
        else this.showMail = false;
        if(signup.city != null) this.city = signup.city;
        else this.city = "";
        if(signup.appCode != null) this.appCode = signup.appCode;
        else this.appCode = "";


        if(" -- other -- ".equals(signup.fieldOfExpertise)){
            if(signup.otherFieldOfExpertise != null) this.fieldOfExpertise = signup.otherFieldOfExpertise;
            else this.fieldOfExpertise = "";
        }
        else{
            if(signup.fieldOfExpertise != null) this.fieldOfExpertise = signup.fieldOfExpertise;
            else this.fieldOfExpertise = "";
        }
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

    public String getCountry() {
        return country;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getBusinessDimension() {
        return businessDimension;
    }

    public String getCity() {
        return city;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getFieldOfExpertise(){
        return fieldOfExpertise;
    }

    public int getYearOfExperience(){
        return yearOfExperience;
    }

    public boolean getShowMail(){
        return showMail;
    }

    public String getAppCode(){
        return appCode;
    }
}
