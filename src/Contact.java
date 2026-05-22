/** A single contact entry. */
public class Contact {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String group;
    private String department;
    private String organization;
    private String extension;

    /** New contact (not yet stored in the database). */
    public Contact(String name, String phone, String email, String group) {
        this(name, phone, email, group, "", "", "");
    }

    public Contact(String name, String phone, String email, String group,
                   String department, String organization, String extension) {
        this(0, name, phone, email, group, department, organization, extension);
    }

    public Contact(int id, String name, String phone, String email, String group) {
        this(id, name, phone, email, group, "", "", "");
    }

    public Contact(int id, String name, String phone, String email, String group,
                   String department, String organization, String extension) {
        this.id = id;
        this.name = name != null ? name : "";
        this.phone = phone != null ? phone : "";
        this.email = email != null ? email : "";
        this.group = group != null ? group : "";
        this.department = department != null ? department : "";
        this.organization = organization != null ? organization : "";
        this.extension = extension != null ? extension : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
