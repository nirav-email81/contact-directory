import java.util.regex.Pattern;

/** Regex validation for phone and email fields. */
public final class ContactValidator {
    private static final Pattern PHONE_LETTERS = Pattern.compile("[a-zA-Z]");
    private static final Pattern PHONE_VALID = Pattern.compile("^[0-9+\\-().\\s]+$");
    private static final Pattern EMAIL_HAS_AT = Pattern.compile("@");

    private ContactValidator() {
    }

    public static String validate(String phone, String email) {
        if (phone != null && PHONE_LETTERS.matcher(phone).find()) {
            return "Phone number cannot contain letters.";
        }
        if (phone != null && !phone.isEmpty() && !PHONE_VALID.matcher(phone).matches()) {
            return "Phone number may only contain digits and + - ( ) . spaces.";
        }
        if (email != null && !email.isEmpty() && !EMAIL_HAS_AT.matcher(email).find()) {
            return "Email must contain an @ symbol.";
        }
        return null;
    }
}
