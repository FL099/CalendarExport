package at.sixtl.florian.models;

import at.sixtl.florian.messages.Messages;

import java.io.File;
import java.util.Scanner;

public class GeneralProperties {
    private String outputFormat = "ics";
    private File inputFile;
    private boolean singleFile = false;
    private int numberOfFields = 13;
    private String separator = ";";
    private String dTFormat = "DD.MM.YYYY";
    private String organizer = "someone@example.com";
    private String organization = "exampleOrg";
    private String inputType = "file";
    public String propsFile = "config/AuthorData.csv";
    public String method = "PUBLISH";

    /* TODO:
        outputfile
        weitere output-Formate
     */

    /**
     * 
     * @param outputFormat Format to output the calendar events in (e.g. ics,ical etc)
     */
    public GeneralProperties(String outputFormat) {
        this(outputFormat, "standard.csv");
    }

    /**
     * 
     * @param outputFormat Format to output the calendar events in (e.g. ics,ical etc)
     * @param inputFile Name of the inputFile
     */
    public GeneralProperties(String outputFormat, String inputFile) {
        this(outputFormat, inputFile, false);
    }

    /**
     * 
     * @param outputFormat Format to output the calendar events in (e.g. ics,ical etc)
     * @param inputFile Name of the inputFile
     * @param singleFile if the output should be written per event or in a single file
     */
    public GeneralProperties(String outputFormat, String inputFile, boolean singleFile) {
        this(outputFormat, inputFile, singleFile, 13);
    }

    /**
     * 
     * @param outputFormat Format to output the calendar events in (e.g. ics,ical etc)
     * @param inputFile Name of the inputFile
     * @param singleFile if the output should be written per event or in a single file
     * @param numberOfFields how many fields there are in the input file
     */
    public GeneralProperties(String outputFormat, String inputFile, boolean singleFile, int numberOfFields) {
        setOutputFormat(outputFormat.toLowerCase());
        setInputFile(inputFile);
        setSingleFile(singleFile);
        setNumberOfFields(numberOfFields);
    }

    public String getInputType() {
        return this.inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat)
    {
        outputFormat = outputFormat.strip();
        if (outputFormat.equals("ical") || outputFormat.equals("ics") || outputFormat.equals("apple")) {
            outputFormat = "ics";
        } else if (outputFormat.equals("csv")
                || outputFormat.equals("outlook")
                || outputFormat.equals("outlookcsv")
                || outputFormat.equals("ocsv")
                || outputFormat.equals("html")) {
            System.out.println("Momentan ist nur ics/ical möglich!\n");
            try (Scanner myObj = new Scanner(System.in)) {
                System.out.println("Mit ics fortfahren? (y/n)");
                String response = myObj.nextLine();  // Read user input
                if (response.equalsIgnoreCase("y")) {
                    outputFormat = "ics";
                } else {
                    throw new IllegalArgumentException(Messages.NOT_SUPPORTED_FORMAT);
                }
            }
        }
        this.outputFormat = outputFormat;
    }

    public File getInputFile(int i) {
        return inputFile;
    }

    public String getInputFile() {
        return inputFile.getName();
    }
    public String getInputFileWithoutEnding() {
        return inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
    }

    public void setInputFile(String inputFile) {
        inputFile = inputFile.trim();
        if (!inputFile.equals("cmd")) {
            this.inputFile = new File(inputFile.trim()); //TODO: leerzeichen im Namen usw entfernen
        } else {
            System.out.println(Messages.CMD_INSTEAD_OF_FILE);
        }

    }

    public boolean isSingleFile() {
        return singleFile;
    }

    public void setSingleFile(boolean singleFile) {
        this.singleFile = singleFile;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String seperator) {
        this.separator = seperator;
    }

    public String getDTFormat() {
        return dTFormat;
    }

    public void setDTFormat(String dTFormat) {
        this.dTFormat = dTFormat;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        if (organizer != null)
            this.organizer = organizer;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        if (organization != null)
            this.organization = organization;
    }

    public String getPropsFile() {
        return propsFile;
    }

    public void setPropsFile(String propsFile) {
        this.propsFile = propsFile;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
