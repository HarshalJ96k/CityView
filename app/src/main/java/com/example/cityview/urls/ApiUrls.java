package com.example.cityview.urls;

public class ApiUrls {

    // IMPORTANT: This is the only place you will need to change your IP address.
    private static final String ROOT_URL = "http://192.168.0.102/cityview_api/";

    public static final String URL_REGISTER = ROOT_URL + "register.php";
    public static final String URL_LOGIN = ROOT_URL + "login.php";
    public static final String URL_GET_USER_DETAILS = ROOT_URL + "get_user_details.php";
    public static final String URL_SUBMIT_REPORT = ROOT_URL + "submit_report.php";
    public static final String URL_UPDATE_PROFILE = ROOT_URL + "update_profile.php";
    public static final String URL_AI_ASSISTANT = ROOT_URL + "ai_assistant.php";
    public static final String URL_GET_CITY_BOUNDARY = ROOT_URL + "get_city_boundary.php";
    public static final String URL_GET_WEATHER = ROOT_URL + "get_weather.php";

    // Official Dashboard
    public static final String URL_GET_DASHBOARD_STATS = ROOT_URL + "get_dashboard_stats.php";
    public static final String URL_GET_REPORTS = ROOT_URL + "get_reports.php";
    public static final String URL_UPDATE_REPORT_STATUS = ROOT_URL + "update_report_status.php";
    public static final String URL_UPDATE_ADMIN_PROFILE = ROOT_URL + "update_admin_profile.php";
    public static final String URL_GET_ADMIN_PROFILE = ROOT_URL + "get_admin_profile.php";
    public static final String URL_GET_ALL_USERS = ROOT_URL + "get_all_users.php";
    public static final String URL_DELETE_REPORT = ROOT_URL + "delete_report.php";

    // City Highlights
    public static final String URL_GET_CITY_HIGHLIGHTS = ROOT_URL + "get_city_highlights.php";

    // 🔔 Notifications
    public static final String URL_GET_NOTIFICATIONS = ROOT_URL + "get_notifications.php";
    public static final String URL_MARK_NOTIFICATION_READ = ROOT_URL + "mark_notification_read.php";
    public static final String URL_GET_REPORT_DETAILS = ROOT_URL + "get_report_details.php";
    public static final String URL_DELETE_NOTIFICATION = ROOT_URL + "delete_notification.php";

    public static String getRootUrl() {
        return ROOT_URL;
    }
}
