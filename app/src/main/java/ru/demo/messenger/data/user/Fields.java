package ru.demo.messenger.data.user;

public enum Fields {
    FIRST_NAME("first_name"),
    LAST_NAME("last_name"),
    SUBDIVISION("subdivision"),
    POSITION("position"),
    DATE_OF_BIRTH("date_Of_Birth"),
    MOBILE_PHONE("phone_mobile"),
    WORK_PHONE("phone_work"),
    SKYPE("skype"),
    GOOGLE_TALK("google_Talk"),
    TWITTER("twitter"),
    //        VK_URL("VKUrl"),
//        FB_URL("facebook_Url"),
    ICQ("ICQ"),
    ROOM_LOCATION("room_Location"),
    ABOUT_ME("about_Me"),
    HOBBY("hobby"),
    EDUCATION("education"),
    WORK_EXPERIENCE("work_experience"),
    REGION("region");

    public final String key;

    Fields(String key) {
        this.key = key;
    }
}
