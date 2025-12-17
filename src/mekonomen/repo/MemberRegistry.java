package mekonomen.repo;

import mekonomen.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberRegistry {
    private final List<Member> members = new ArrayList<Member>();

    public List<Member> getAll() {
        return new ArrayList<Member>(members);
    }

    public void setAll(List<Member> newMembers) {
        members.clear();
        if (newMembers != null) members.addAll(newMembers);
    }

    public void add(Member m) { members.add(m); }

    public boolean removeById(String id) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(id)) {
                members.remove(i);
                return true;
            }
        }
        return false;
    }

    public Member findById(String id) {
        for (Member m : members) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }
}