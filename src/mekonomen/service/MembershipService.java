package mekonomen.service;

import mekonomen.model.Member;
import mekonomen.model.MemberLevel;
import mekonomen.repo.MemberRegistry;

import java.time.LocalDate;

public class MembershipService {
    private final MemberRegistry registry;

    public MembershipService(MemberRegistry registry) {
        this.registry = registry;
    }

    public void registerMember(Member m) {
        registry.add(m);
        m.addHistory(LocalDate.now() + ": Registrerad (" + m.getLevel() + ")");
    }

    public void updateMember(Member m, String first, String last, String phone, String address, MemberLevel level) {
        m.setFirstName(first);
        m.setLastName(last);
        m.setPhone(phone);
        m.setAddress(address);
        m.setLevel(level);
        m.addHistory(LocalDate.now() + ": Uppdaterad profil/level");
    }
}
