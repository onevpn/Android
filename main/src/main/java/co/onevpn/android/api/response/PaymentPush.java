package co.onevpn.android.api.response;

public class PaymentPush {
    private String message;
    private String plan;
    private String email;

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public String getPlan() {
        return plan;
    }
}
