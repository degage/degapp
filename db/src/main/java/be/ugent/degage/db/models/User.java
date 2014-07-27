package be.ugent.degage.db.models;

/**
 * Represents a single user
 */
public class User {

    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String cellphone;
    private Address addressDomicile;
    private Address addressResidence;
    private UserGender gender;
    private String license;
    private UserStatus status;
    private IdentityCard identityCard;
    private String damageHistory;
    private int profilePictureId; //TODO, review if it's okay practice to -1 = NULL
    private boolean payedDeposit;
    private boolean agreeTerms;

    public User(String email) {
        this(0, email, null, null, UserStatus.REGISTERED);
    }

    public User() {
        this(0, null, null, null, UserStatus.REGISTERED);
    }

    public User(int id, String email, String firstName, String lastName, UserStatus status){
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;

        this.status = status;
        this.profilePictureId = -1;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(int profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddressDomicile() {
        return addressDomicile;
    }

    public void setAddressDomicile(Address addressDomicile) {
        this.addressDomicile = addressDomicile;
    }

    public Address getAddressResidence() {
        return addressResidence;
    }

    public void setAddressResidence(Address addressResidence) {
        this.addressResidence = addressResidence;
    }
    public String getLicense() {
        return license;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public IdentityCard getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(IdentityCard identityCard) {
        this.identityCard = identityCard;
    }

    public String getDamageHistory() {
        return damageHistory;
    }

    public void setDamageHistory(String damageHistory) {
        this.damageHistory = damageHistory;
    }

    public boolean isPayedDeposit() {
        return payedDeposit;
    }

    public void setPayedDeposit(boolean payedDeposit) {
        this.payedDeposit = payedDeposit;
    }

    public boolean isAgreeTerms() {
        return agreeTerms;
    }

    public void setAgreeTerms(boolean agreeTerms) {
        this.agreeTerms = agreeTerms;
    }

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }

    /**
     * Has this user status {@link be.ugent.degage.db.models.UserStatus#FULL}?
     * @return
     */
    public boolean hasFullStatus() {
        return status == UserStatus.FULL;
    }

    /**
     * Is this user allowed to login (depends on the user status)?
     */
    public boolean canLogin() {
        return status != UserStatus.BLOCKED && status != UserStatus.DROPPED && status != UserStatus.EMAIL_VALIDATING;
    }

    @Override
    // TODO: remove these equals and hash codes (probably used by Cache mechanism of play?)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (agreeTerms != user.agreeTerms) return false;
        if (id != user.id) return false;
        if (payedDeposit != user.payedDeposit) return false;
        if (addressDomicile != null ? !addressDomicile.equals(user.addressDomicile) : user.addressDomicile != null)
            return false;
        if (addressResidence != null ? !addressResidence.equals(user.addressResidence) : user.addressResidence != null)
            return false;
        if (cellphone != null ? !cellphone.equals(user.cellphone) : user.cellphone != null) return false;
        if (damageHistory != null ? !damageHistory.equals(user.damageHistory) : user.damageHistory != null)
            return false;
        if (!email.equals(user.email)) return false;
        if (!firstName.equals(user.firstName)) return false;
        if (gender != user.gender) return false;
        if (identityCard != null ? !identityCard.equals(user.identityCard) : user.identityCard != null) return false;
        if (!lastName.equals(user.lastName)) return false;
        if (license != null ? !license.equals(user.license) : user.license != null) return false;
        if (phone != null ? !phone.equals(user.phone) : user.phone != null) return false;
        if (status != user.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + email.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (cellphone != null ? cellphone.hashCode() : 0);
        result = 31 * result + (addressDomicile != null ? addressDomicile.hashCode() : 0);
        result = 31 * result + (addressResidence != null ? addressResidence.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (license != null ? license.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (identityCard != null ? identityCard.hashCode() : 0);
        result = 31 * result + (damageHistory != null ? damageHistory.hashCode() : 0);
        result = 31 * result + (payedDeposit ? 1 : 0);
        result = 31 * result + (agreeTerms ? 1 : 0);
        return result;
    }
}