package at.sixtl.florian;

import at.sixtl.florian.messages.Messages;
import at.sixtl.florian.models.Calendar;
import at.sixtl.florian.models.GeneralProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CalendarExport {
    private static GeneralProperties properties;
    public CalendarExport() {
        // TODO: booleans als "-.." oder "--.." in der commandline (können somit überall stehen)
        // TODO: Felder einlesen (damit Reihenfolge im file nicht fix sein muss)
    }

    public static void main(String[] args) {
        properties = readArgsAndFlags(args);
        try {
            readPropertyFile(properties.propsFile);
            List<Calendar> calendar = createCalender(properties);
            System.out.println(calendar.size() + Messages.EVENTS_READ);
            if(printCalendar(calendar))
                System.out.println(Messages.PROGRAM_SUCCESS);
            else
                System.out.println(Messages.PROGRAM_FAIL);
        } catch (IOException e) {
            System.out.println(Messages.ERROR_READING_GENERAL + e);
        } catch (Throwable e) {
            System.out.println(Messages.CMD_ARGS_NOT_CORRECT); // TODO: besseres Error-handling
        } finally {
            System.out.println(Messages.PROGRAM_FINISHED);
        }
        System.exit(0);
    }

    @Deprecated
    private static GeneralProperties readArgs(String[] cmdArgs) throws IllegalArgumentException{
        GeneralProperties properties;
        switch (cmdArgs.length) {
            case 0 -> throw new IllegalArgumentException(Messages.NOT_ENOUGH_ARGS);
            case 1 -> properties = new GeneralProperties(cmdArgs[0]);               // nur outputformat
            case 2 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1]);   // outputformat + inputFile
            case 3 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1], cmdArgs[2].equals("S")); // outputformat + inputfile + S=single File(y/n)
            case 4 -> properties = new GeneralProperties(cmdArgs[0], cmdArgs[1], cmdArgs[2].equals("S"),  // outputformat + inputfile + single File + num of Fields
                    Integer.parseInt(cmdArgs[3]));
            default -> {
                System.out.println(Messages.CMD_NOT_ENOUGH_ARGS);
                properties = new GeneralProperties("ics");
            }
        }
        return properties;
    }

    private static GeneralProperties readArgsAndFlags(String[] cmdArgs) throws IllegalArgumentException {
        GeneralProperties properties;
        Map<String, String> options = new HashMap<>();
        List<String> inputs = new ArrayList<>();

        for (int i = 0; i < cmdArgs.length; i++) {
            if (cmdArgs[i].startsWith("-")) {
                String key = cmdArgs[i].substring(1); // Remove the "-" character
                    if (i < cmdArgs.length - 1 && !cmdArgs[i+1].startsWith("-")) {
                        options.put(key, cmdArgs[i+1]);
                        i++; // Increment i to skip the next argument
                    } else {
                        options.put(key, null); // Flag without value
                    }
            } else {
                inputs.add(cmdArgs[i]);
            }
        }

        switch (inputs.size()) {
            case 0 -> properties = new GeneralProperties("ics");
            case 1 -> properties = new GeneralProperties(inputs.get(0));               // nur outputformat
            case 2 -> properties = new GeneralProperties(inputs.get(0), inputs.get(1));   // outputformat + inputFile
            case 3 -> properties = new GeneralProperties(inputs.get(0), inputs.get(1), options.containsKey("S")); // outputformat + inputfile + S=single File(y/n)
            case 4 -> properties = new GeneralProperties(inputs.get(0), inputs.get(1), options.containsKey("S"),  // outputformat + inputfile + single File + num of Fields
                    Integer.parseInt(inputs.get(2)));
            default -> {
                System.out.println(Messages.CMD_NOT_ENOUGH_ARGS);
                properties = new GeneralProperties("ics");
            }
        }

        if (options.containsKey("n")) {
            properties.setNumberOfFields(Integer.parseInt(options.get("n")));
        }

        if (options.containsKey("t")) {
            properties.setSeperator(options.get("t"));
        }

        if (options.containsKey("i")) {
            properties.setInputType(options.get("i"));
        }

        properties.setSingleFile(options.containsKey("S"));
        
        /*for (String key : options.keySet()) {
            System.out.println("  " + key + ": " + options.get(key));
        }*/
        return properties;
    }

    private static List<Calendar> readFileToCalendar() throws IOException {
        List<Calendar> entries = new ArrayList<>();
        String line;
        //try(BufferedReader br = new BufferedReader(new FileReader(properties.getInputFile()))) {
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(properties.getInputFile()), StandardCharsets.UTF_8))) {
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
            System.out.println(Messages.ERROR_READING_TO_CALENDAR + e);
        }
        return entries;
    }

    private static List<Calendar> readCmdToCalendar() throws IOException {
        Scanner scanner = new Scanner(System.in);
        List<Calendar> entries = new ArrayList<>();
        boolean keepGoing = true;
        boolean skip = false;
        while (keepGoing) {
            Calendar newEvent;
            System.out.println(Messages.READING_CMD_NEW_EVENT);
            try {
                System.out.print(Messages.READING_CMD_EVENT_NAME);
                String summary = scanner.nextLine();
                String startDate;
                String startTime;
                String endDate;
                String endTime;
                String description;
                String duration = "0";
                System.out.println(Messages.READING_CMD_EVENT_TYPE);
                int type = scanner.nextInt();
                scanner.nextLine();
                if (type == 2) {
                    System.out.println(Messages.READING_CMD_EVENT_STARTDATE);
                    startDate = scanner.nextLine();
                    System.out.println(Messages.READING_CMD_EVENT_STARTTIME);
                    startTime = scanner.nextLine();
                    System.out.println(Messages.READING_CMD_EVENT_ENDDATE);
                    endDate = scanner.nextLine();
                    System.out.println(Messages.READING_CMD_EVENT_ENDTIME);
                    endTime = scanner.nextLine();
                } else if(type == 3) {
                    System.out.println(Messages.READING_CMD_EVENT_STARTDATE);
                    startDate = scanner.nextLine();
                    endDate = startDate;
                    startTime = "00:00";
                    endTime = startTime;
                } else {
                    System.out.println(Messages.READING_CMD_EVENT_STARTDATE);
                    startDate = scanner.nextLine();
                    endDate = startDate;
                    System.out.println(Messages.READING_CMD_EVENT_STARTTIME);
                    startTime = scanner.nextLine();
                    endTime = startTime;
                    System.out.println(Messages.READING_CMD_EVENT_DURATION);
                    duration = scanner.nextLine();
                }

                if(!duration.equals("0"))
                {
                    //TODO: startDate+Duration => endDate und startTime+Duration => endTime
                }
                
                newEvent = new Calendar(startDate, startTime, endDate, endTime, "", summary);
                System.out.println(Messages.READING_CMD_EVENT_DESCRIPTION);
                description = scanner.nextLine();
                newEvent.setDescription(description);
                int step;
                do {
                    System.out.println(Messages.READING_CMD_EVENT_ADDITIONAL);
                    step = scanner.nextInt();
                    scanner.nextLine();
                    switch (step) {
                        case 0:
                            break;
                        case 1:
                            keepGoing = false;
                            break;
                        case 2:
                            System.out.println(Messages.READING_CMD_EVENT_LOCATION);
                            newEvent.setLocation(scanner.nextLine());
                            break;
                        case 3:
                            System.out.println(Messages.READING_CMD_EVENT_CATEGORY);
                            newEvent.setCategory(scanner.nextLine());
                            break;
                        case 4:
                            System.out.println(Messages.READING_CMD_EVENT_RULES);
                            break;
                        case 8:
                            System.out.println(Messages.READING_CMD_EVENT_CONFIRM);
                            if(scanner.nextLine().strip().equals("y")) {
                                skip = true;
                                System.out.println(Messages.READING_CMD_EVENT_ENTER_AGAIN);
                            }
                            break;
                        case 9:
                            printAllEvents(entries, newEvent);
                            step = -1;
                            break;
                        case 10:
                            throw new Exception("Abbruch durch User");
                        default:
                            break;
                    }
                } while (step >= 2 && step < 8 || step == -1);
                if (!skip) {
                    newEvent.setCurDateTime();
                    newEvent.setOrganizer(properties.getOrganizer());
                    newEvent.setOrganization(properties.getOrganization());
                    entries.add(newEvent);
                    System.out.println(Messages.SUCCESS_READING_EVENT);
                } else {
                    System.out.println(Messages.READING_CMD_REPEAT_EVENT);
                }

            } catch (Exception e) {
                System.out.println(Messages.ERROR_READING_CMD);
                System.out.println(e);
            }

        }
        scanner.close();
        return entries;
    }

    private static void printAllEvents(List<Calendar> entries, Calendar currentEvent) {
        System.out.println(Messages.PRINTING_CMD_EVENTS);
        if (entries.size() > 0) {
            System.out.println("Events bisher: \n" + entries);
        }
        System.out.println("Aktuelles Event: \n" + currentEvent);
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
                System.out.println(Messages.ERROR_WRITING_FILE + calendar.getSummary() + "\"\n");
            }
        }
        return true;
    }

    private static boolean writeCalendarToSingleFile(List<Calendar> calendarList) {
        StringBuilder calendarDates = new StringBuilder();
        String outputFile;
        for (Calendar calendar :
                calendarList) {
            calendarDates.append(calendar.calendarToIcsStub());
        }
        if (properties.getInputType().equals("cmd")) {
            outputFile = "cmdGeneratedCal." + properties.getOutputFormat();
        } else {
            outputFile = properties.getInputFileWithoutEnding() + "." + properties.getOutputFormat();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))){
            bw.write(Calendar.generateIcsFromStubs(calendarList.get(0).getMethod() ,calendarDates.toString(), calendarList.get(0).getCurDateTime().getZone()));
            System.out.printf(" -File \"%s\" geschrieben \n------\n\n", outputFile);
            return true;
        } catch (Throwable e) {
            System.out.println(Messages.ERROR_WRITING_FILE + outputFile);
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

    private static List<Calendar> createCalender(GeneralProperties properties) throws Exception {
        try {
            if (properties.getInputType().equals("cmd")) {
                return readCmdToCalendar();
            } else {
                return readFileToCalendar();

            }
        } catch (Throwable e) {
            System.out.println(Messages.ERROR_CREATING_FILE + e.getCause());
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
            System.out.println(Messages.ERROR_PRINTING_CALENDAR);
            throw e;
        }
    }

    private static void readPropertyFile(String location) {
        File file = new File(location);
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            HashMap<String, String> values = new HashMap<>();
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] junks = line.split(":");
                    values.put(junks[0], junks[1]);
                }
            }
            if (values.containsKey("method")) {
                properties.setMethod(values.get("method"));
            }
            if (values.containsKey("organizer")) {
                properties.setMethod(values.get("organizer"));
            }
            if (values.containsKey("organization")) {
                properties.setMethod(values.get("organization"));
            }
        } catch (FileNotFoundException e) {
            System.out.println(Messages.FILE_NOT_FOUND + "\n" + Messages.FILE_READING_FALLBACK);
        } catch (IOException e) {
            System.out.println(Messages.ERROR_READING_FILE + "\n" + Messages.FILE_READING_FALLBACK);
        }
    }
}
