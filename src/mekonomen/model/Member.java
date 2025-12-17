package mekonomen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.io.Serializable;


public class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;

    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    private MemberLevel level;
    private final List<String> history = new ArrayList<String>();

    public Member(String firstName, String lastName, String phone, String address, MemberLevel level) {
        this(UUID.randomUUID().toString(), firstName, lastName, phone, address, level, new ArrayList<String>());
    }

    public Member(String id, String firstName, String lastName, String phone, String address,
                  MemberLevel level, List<String> history) {
        this.id = Objects.requireNonNull(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.level = (level == null ? MemberLevel.STANDARD : level);
        if (history != null) this.history.addAll(history);
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public MemberLevel getLevel() { return level; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setLevel(MemberLevel level) { this.level = level; }

    public String getFullName() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + " " + ln).trim();
    }

    public List<String> getHistory() {
        return new ArrayList<String>(history);
    }

    public void addHistory(String entry) {
        if (entry != null && !entry.trim().isEmpty()) history.add(entry.trim());
    }

    @Override
    public String toString() {
        return getFullName() + " (" + id.substring(0, 8) + ")";
    }
}