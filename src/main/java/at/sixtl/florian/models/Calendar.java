package at.sixtl.florian.models;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Calendar {

    public String uniqueId;
    protected ZonedDateTime startDateTime;
    protected ZonedDateTime endDateTime;
    protected ZonedDateTime curDateTime;
    protected String method = "PUBLISH";
    protected String organizer;
    protected String organization;
    String summary = "";
    String cat = "";
    String location = "";
    String rule = "";
    String description = "";
    protected int sequenceNr = 1;   //wichtig bei mehreren Versionen(Damits beim im Kalender speichern nicht doppelt ist)

    public Calendar(String startDate,
                    String startTime,
                    String endDate,
                    String endTime,
                    String format,
                    String summary) {
        this(stringsToDateTime(startDate, startTime, format),
                stringsToDateTime(endDate, endTime, format),
                summary);
    }

    public Calendar(ZonedDateTime start,
                    ZonedDateTime end,
                    String summary) {
        setStartDateTime(start);
        setEndDateTime(end);
        setSummary(summary);
        makeId(start.getDayOfYear(), end.getDayOfYear(), start.getHour(), end.getHour(), summary);
    }

    public Calendar(){}

    public void makeId(int v1, int v2, int v3, int v4, String v5) {
        int t = (v1*v2)%10000;
        t = (t*v1)%10000;
        t += v3 + (v4*100);
        this.uniqueId = v5.substring(0,1) + t%10000 + v5.substring(v5.length()-1);
    }

    public static ZonedDateTime stringsToDateTime(String date, String time, String format) {
        if (format.equals("DD.MM.YYYY")) {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute = Integer.parseInt(time.substring(3));
            int year = Integer.parseInt(date.substring(6));
            int month = Integer.parseInt(date.substring(3,5));
            int day = Integer.parseInt(date.substring(0,2));
            return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault());
        } //TODO: mehr Formate
        return ZonedDateTime.now();
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(ZonedDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ZonedDateTime getCurDateTime() {
        return curDateTime;
    }

    public void setCurDateTime() {
        this.curDateTime = ZonedDateTime.now();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return cat;
    }

    public void setCategory(String category) {
        this.cat = category;
    }

    public String getMethod() {
        return method;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String singleCalendarToIcs() {
        return generateIcsFromStubs(this.method, calendarToIcsStub(), this.curDateTime.getZone());
    }

    public static String generateIcsFromStubs(String method, String concatedStubs, ZoneId zone) {
        return String
                .format("""
                        BEGIN:VCALENDAR
                        VERSION:2.0
                        METHOD:%s
                        %s
                        END:VCALENDAR""",
                        method,
                        concatedStubs);
    }

    public String calendarToIcsStub() {
        String inhalt;
        if (this.startDateTime.getHour() == 0
                && this.startDateTime.getMinute() == 0
                && this.endDateTime.getHour() == 0
                && this.endDateTime.getMinute() == 0) {
            inhalt = String
                    .format("""
                        BEGIN:VEVENT
                        TZID:Europe/Vienna
                        DTSTAMP:%04d%02d%02dT%02d%02d%02d
                        DTSTART:%04d%02d%02d
                        DTEND:%04d%02d%02d
                        UID:%d%d@%s
                        DESCRIPTION:%s
                        ORGANIZER:mailto:%s
                        SUMMARY:%s
                        %s
                        SEQUENCE:%d
                        STATUS:CONFIRMED%s%s
                        END:VEVENT
                        """,
                            this.curDateTime.getYear(), this.curDateTime.getMonthValue(), this.curDateTime.getDayOfMonth(),
                            this.curDateTime.getHour(), this.curDateTime.getMinute(), this.curDateTime.getSecond(),
                            this.startDateTime.getYear(), this.startDateTime.getMonthValue(), this.startDateTime.getDayOfMonth(),
                            this.endDateTime.getYear(), this.endDateTime.getMonthValue(), this.endDateTime.getDayOfMonth()+1,
                            this.curDateTime.getSecond() * this.startDateTime.getDayOfMonth() + this.endDateTime.getDayOfMonth() * 1293,
                            this.startDateTime.getMonthValue(),
                            this.organization,
                            this.description,
                            this.organizer,
                            this.summary,
                            getIfNotEmpty(this.location, "LOCATION:"),
                            this.sequenceNr,
                            getIfNotEmpty(this.cat, "CATEGORIES:"),
                            getRuleForIcs(this.rule));
        } else
            inhalt = String
                .format("""
                        BEGIN:VEVENT
                        TZID:Europe/Vienna
                        DTSTAMP:%04d%02d%02dT%02d%02d%02d
                        DTSTART:%04d%02d%02dT%02d%02d%02d
                        DTEND:%04d%02d%02dT%02d%02d%02d
                        UID:%d%d@%s
                        DESCRIPTION:%s
                        ORGANIZER:mailto:%s
                        SUMMARY:%s
                        %s
                        SEQUENCE:%d
                        STATUS:CONFIRMED%s%s
                        END:VEVENT
                        """,
                        this.curDateTime.getYear(), this.curDateTime.getMonthValue(), this.curDateTime.getDayOfMonth(),
                        this.curDateTime.getHour(), this.curDateTime.getMinute(), this.curDateTime.getSecond(),
                        this.startDateTime.getYear(), this.startDateTime.getMonthValue(), this.startDateTime.getDayOfMonth(),
                        this.startDateTime.getHour(), this.startDateTime.getMinute(), this.startDateTime.getSecond(),
                        this.endDateTime.getYear(), this.endDateTime.getMonthValue(), this.endDateTime.getDayOfMonth(),
                        this.endDateTime.getHour(), this.endDateTime.getMinute(), this.endDateTime.getSecond(),
                        this.curDateTime.getSecond() * this.startDateTime.getDayOfMonth() + this.endDateTime.getDayOfMonth() * 1293 + this.startDateTime.getHour(),
                        this.startDateTime.getMonthValue(),
                        this.organization,
                        this.description,
                        this.organizer,
                        this.summary,
                        getIfNotEmpty(this.location, "LOCATION:"),
                        this.sequenceNr,
                        getIfNotEmpty(this.cat, "CATEGORIES:"),
                        getRuleForIcs(this.rule));
        return inhalt;
    }

    private String getIfNotEmpty(String value, String text) {
        if (value.length() > 0) {
            return text + value;
        }
        return "";
    }

    public String getRuleForIcs(String i) {
        if (!i.equals("") && !i.equals(" ") && !i.equals("-") && !i.equals("1")) {
            if (i.equals("2")) {
                return "RRULE:FREQ=YEARLY\n";
            } else if (i.equals("3")) {
                return "RRULE:FREQ=MONTHLY\n";
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "{\n" +
                " startDateTime: " +
                startDateTime.getDayOfMonth() + "." +
                startDateTime.getMonthValue() + "." +
                startDateTime.getYear() + " " +
                startDateTime.getHour() + ":" +
                startDateTime.getMinute() +
                ",\n endDateTime: " +
                endDateTime.getDayOfMonth() + "." +
                endDateTime.getMonthValue() + "." +
                endDateTime.getYear() + " " +
                endDateTime.getHour() + ":" +
                endDateTime.getMinute() +
                ",\n curDateTime: " + curDateTime +
                ",\n organizer: '" + organizer + '\'' +
                ",\n organization: '" + organization + '\'' +
                ",\n summary: '" + summary + '\'' +
                ",\n location: '" + location + '\'' +
                ",\n description: '" + description + '\'' +
                "\n}";
    }
}
