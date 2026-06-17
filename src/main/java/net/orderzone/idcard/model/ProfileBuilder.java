package net.orderzone.idcard.model;

public class ProfileBuilder {

    public static Profile buildDefaultStudentProfile() {
        Profile profile = new Profile();
        profile.setType(ProfileType.STUDENT);
        profile.setDepartment("Computer Science");
        profile.setTitle("Student");
        return profile;
    }

    public static Profile buildDefaultEmployeeProfile() {
        Profile profile = new Profile();
        profile.setType(ProfileType.EMPLOYEE);
        profile.setDepartment("Administration");
        profile.setTitle("Employee");
        return profile;
    }

    public static Profile buildDefaultUserProfile() {
        Profile profile = new Profile();
        profile.setType(ProfileType.USER);
        profile.setTitle("User");
        return profile;
    }
}