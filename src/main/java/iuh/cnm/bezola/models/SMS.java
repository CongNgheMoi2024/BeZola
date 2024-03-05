package iuh.cnm.bezola.models;

public class SMS {
    private String phoneNo;

    public SMS() {
    }

    public SMS(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
