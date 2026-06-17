package net.orderzone.idcard.model;

public class ProfileBuilder {

    public static Profile buildDefaultStudentProfile() {
        Profile profile = new Profile();
        profile.setProfileType(ProfileType.STUDENT);
        profile.setDepartment("Computer Science");
        profile.setPosition("Student");
        return profile;
    }

    public static Profile buildDefaultEmployeeProfile() {
        Profile profile = new Profile();
        profile.setProfileType(ProfileType.EMPLOYEE);
        profile.setDepartment("Administration");
        profile.setPosition("Employee");
        return profile;
    }

    public static Profile buildDefaultUserProfile() {
        Profile profile = new Profile();
        profile.setProfileType(ProfileType.USER);
        profile.setPosition("User");
        return profile;
    }
}