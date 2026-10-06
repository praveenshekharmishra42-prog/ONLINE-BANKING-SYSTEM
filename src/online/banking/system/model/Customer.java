package online.banking.system.model;

import java.time.LocalDate;

/** A bank customer with personal and contact details. */
public class Customer extends Person {

    private String fatherName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String occupation;

    public Customer(int userId, String username, String name) {
        super(userId, username, name);
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    public String getWelcomeMessage() {
        return "Customer Banking - Welcome, " + getName();
    }

    public String getFatherName()     { return fatherName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender()         { return gender; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public String getAddress()        { return address; }
    public String getCity()           { return city; }
    public String getState()          { return state; }
    public String getPincode()        { return pincode; }
    public String getOccupation()     { return occupation; }

    public void setFatherName(String fatherName)      { this.fatherName = fatherName; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender)              { this.gender = gender; }
    public void setEmail(String email)                { this.email = email; }
    public void setPhone(String phone)                { this.phone = phone; }
    public void setAddress(String address)            { this.address = address; }
    public void setCity(String city)                  { this.city = city; }
    public void setState(String state)                { this.state = state; }
    public void setPincode(String pincode)            { this.pincode = pincode; }
    public void setOccupation(String occupation)      { this.occupation = occupation; }
}
