package at.sixtl.florian.models;

import at.sixtl.florian.messages.Messages;

import java.io.File;
import java.util.Scanner;

public class GeneralProperties {
    private String outputFormat = "ics";
    private File inputFile;
    private boolean singleFile = false;
    private int numberOfFields = 13;
    private String seperator = ";";
    private String dTFormat = "DD.MM.YYYY";
    private String organizer = "someone@example.com";
    private String organization = "exampleOrg";

    /* TODO:
        outputfile
        weitere output-Formate
     */

    public GeneralProperties(String outputFormat) {
        this(outputFormat, "standard.csv");
    }

    public GeneralProperties(String outputFormat, String inputFile) {
        this(outputFormat, inputFile, false);
    }

    public GeneralProperties(String outputFormat, String inputFile, boolean singleFile) {
        this(outputFormat, inputFile, singleFile, 13);
    }

    public GeneralProperties(String outputFormat, String inputFile, boolean singleFile, int numberOfFields) {
        setOutputFormat(outputFormat.toLowerCase());
        setInputFile(inputFile);
        setSingleFile(singleFile);
        setNumberOfFields(numberOfFields);
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        if (outputFormat.equals("ical") || outputFormat.equals("ics") || outputFormat.equals("apple")) {
            this.outputFormat = "ics";
        } else if (outputFormat.equals("csv")
                || outputFormat.equals("outlook")
                || outputFormat.equals("outlookcsv")
                || outputFormat.equals("ocsv")
                || outputFormat.equals("html")) {
            System.out.printf("Momentan ist nur ics/ical möglich!\n");
            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Mit ics fortfahren? (y/n)");
            String response = myObj.nextLine();  // Read user input
            if (response.equalsIgnoreCase("y")) {
                this.outputFormat = "ics";
            } else {
                throw new IllegalArgumentException(Messages.NOT_SUPPORTED_FORMAT);
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
        this.inputFile = new File(inputFile.trim()); //TODO: leerzeichen im Namen usw entfernen
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

    public String getSeperator() {
        return seperator;
    }

    public void setSeperator(String seperator) {
        this.seperator = seperator;
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
        this.organizer = organizer;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
