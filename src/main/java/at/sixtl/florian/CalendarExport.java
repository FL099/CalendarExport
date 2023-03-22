package at.sixtl.florian;

import at.sixtl.florian.messages.Messages;
import at.sixtl.florian.models.Calendar;
import at.sixtl.florian.models.GeneralProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalendarExport {
    private static GeneralProperties properties;
    public CalendarExport() {
        // TODO: booleans als "-.." oder "--.." in der commandline (können somit überall stehen)
        // TODO: Felder einlesen (damit Reihenfolge im file nicht fix sein muss)
    }

    public static void main(String[] args) {
        properties = readArgs(args);
        try {
            readPropertyFile("AuthorData.csv");
            List<Calendar> calendar = createCalender();
            System.out.println(calendar.size() + " Events eingelesen");
            if(printCalendar(calendar))
                System.out.println(Messages.PROGRAM_SUCCESS);
            else
                System.out.println(Messages.PROGRAM_FAIL);
        } catch (Throwable e) {
            System.out.println("Filename oder Argumente nicht korrekt"); // TODO: besseres Error-handling
        } finally {
            System.out.println(Messages.PROGRAM_FINISHED);
        }
    }

    private static GeneralProperties readArgs(String[] cmdArgs) throws IllegalArgumentException{
        GeneralProperties properties;
        switch (cmdArgs.length) {
            case 0 -> throw new IllegalArgumentException(Messages.NOT_ENOUGH_ARGS);
            case 1 -> properties = new GeneralProperties(cmdArgs[0]);
            case 2 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1]);
            case 3 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1], cmdArgs[2].equals("S")); // S= single File
            case 4 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1], cmdArgs[2].equals("S"),
                    Integer.parseInt(cmdArgs[3]));
            default -> {
                System.out.println("Nicht genug Argumente angegeben, versuche es mit Standardwerten...");
                properties = new GeneralProperties("ics");
            }
        }
        return properties;
    }

    private static List<Calendar> readFileToCalendar() throws IOException {
        List<Calendar> entries = new ArrayList<>();
        String line;
        //try(BufferedReader br = new BufferedReader(new FileReader(properties.getInputFile()))) {
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(properties.getInputFile()), "UTF-8"))) {
            while ((line = br.readLine()) != null) {    //TODO mit stream ersetzen (lines())
                String[] values = line.split(properties.getSeperator());
                try {
                    String startDate = ensureNotEmpty(values[0]);
                    String startTime = ensureNotEmpty(values[1]);
                    String endDate = ensureNotEmpty(values[2]).equals(Messages.MISSING_VALUE_PH) ? startDate : ensureNotEmpty(values[2]);
                    String endTime = ensureNotEmpty(values[3]);
                    String summary = ensureNotEmpty(values[4]);
                    Calendar calendar = new Calendar(
                            startDate, startTime,
                            endDate, endTime, properties.getDTFormat(),
                            summary);
                    System.out.println(String.format(Messages.READ_ROW, values.length));
                    switch (values.length) {
                        case 8:
                            calendar.setRule(values[7]);
                        case 7:
                            calendar.setLocation(values[6]);
                        case 6:
                            calendar.setDescription(values[5]);
                            break;
                    }
                    calendar.setCurDateTime();
                    calendar.setOrganizer(properties.getOrganizer());
                    calendar.setOrganization(properties.getOrganization());
                    entries.add(calendar);
                } catch (NumberFormatException e) {
                    System.out.println(Messages.READ_HAEDER_ROW);
                }

            }
            System.out.println(Messages.READ_FILE);
        } catch (Throwable e) {
            System.out.println("Beim einlesen des Kalenderfiles ist ein Fehler aufgetreten: \n" + e);
        }
        return entries;
    }

    private static boolean writeCalendarToFiles(List<Calendar> calendarList) throws IOException{
        for (Calendar calendar :
                calendarList) {
            //try (BufferedWriter bw = new BufferedWriter(new FileWriter(calendar.getStartDateTime().getYear()+calendar.getStartDateTime().getMonthValue() + calendar.getSummary() + "." + properties.getOutputFormat()))){
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                                    calendar.getSummary().replaceAll("\\s+", "_") //
                                    + "_" + calendar.uniqueId + "_"
                                    + calendar.getStartDateTime().getYear()   //
                                    + "_"
                                    + String.format("%02d", calendar.getStartDateTime().getMonthValue()) //
                                    + "."
                                    + properties.getOutputFormat()), "UTF-8"

                    ))){
                bw.write(calendar.singleCalendarToIcs());
                System.out.printf(" -File \"%s\" geschrieben \n------\n\n", calendar.getSummary() + ".ics");
            } catch (Throwable e) {
                System.out.println("Error while writing Calendar File \"" + calendar.getSummary() + "\"\n");
            }
        }
        return true;
    }

    private static boolean writeCalendarToSingleFile(List<Calendar> calendarList) {
        StringBuilder calendarDates = new StringBuilder();
        for (Calendar calendar :
                calendarList) {
            calendarDates.append(calendar.calendarToIcsStub());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(properties.getInputFileWithoutEnding() + "." + properties.getOutputFormat()))){
            bw.write(Calendar.generateIcsFromStubs(calendarList.get(0).getMethod() ,calendarDates.toString(), calendarList.get(0).getCurDateTime().getZone()));
            System.out.printf(" -File \"%s\" geschrieben \n------\n\n", properties.getInputFile() + ".ics");
            return true;
        } catch (Throwable e) {
            System.out.println("Error while writing Calendar File \"" + properties.getInputFile() + ".ics\"");
        }
        return false;
    }
    private static String ensureNotEmpty(String value) {
        if (value.isBlank() || value.equals("") || value.equals(" "))
        {
            return Messages.MISSING_VALUE_PH;
        } else {
            return value;
        }
    }

    private static List<Calendar> createCalender() throws IOException {
        try {
            return readFileToCalendar();
        } catch (Throwable e) {
            System.out.println("Bei der Erstellung des Kalenderfiles ist ein Fehler aufgetreten: \n" + e.getCause());
            throw e;
        }
    }

    private static boolean printCalendar(List<Calendar> calendarList ) throws IOException{
        try {
            if (properties.isSingleFile()) {
                return writeCalendarToSingleFile(calendarList);
            } else {
                return writeCalendarToFiles(calendarList);
            }
        } catch (Throwable e) {
            System.out.println("Bei der Ausgabe des Kalenders ist ein Fehler aufgetreten.");
            throw e;
        }
    }

    private static void readPropertyFile(String location) {
        File file = new File(location);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            int i = 0;

            String line;
            String[] temp;
            for(temp = new String[4]; (line = br.readLine()) != null; ++i) {
                String[] junks = line.split(":");
                temp[i] = junks[1];
            }

            //properties.method = temp[0];
            properties.setOrganizer(temp[1]);
            properties.setOrganization(temp[2]);
        } catch (FileNotFoundException e) {
            System.out.println(Messages.FILE_NOT_FOUND + "\n" + Messages.FILE_READING_FALLBACK);
        } catch (IOException e) {
            System.out.println(Messages.FILE_READING_ERROR + "\n" + Messages.FILE_READING_FALLBACK);
        }
    }
}
