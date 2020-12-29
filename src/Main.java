import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    // thing to set
    private static final String recipient = "xxx@yyy.zzz";
    private static final String URL_TO_SCRAPE = "https://sites.google.com/view/bigdatafelician/website-monitoring";


    private static final int LOOP_TIME = 120;
    private static final String HTML_FILE_NAME = "WebPage.html";
    private static final String LOG_FILE_NAME = "logFile.txt";
    private static List<String> names = new ArrayList();

    public static void main(String[] args) {

        Date lastUpdate = new Date();

        scanFirstTime();

        while (true) {

            downloadWebPage(URL_TO_SCRAPE);
            File file = new File(HTML_FILE_NAME);
            try {
                Document doc = Jsoup.parse(file, "UTF-8", URL_TO_SCRAPE);
                int listSize = doc.body().child(0).getElementsByTag("li").size();
                List<String> newNames = new ArrayList();
                for (int i = 0; i < listSize; i++) {
                    String temp = String.valueOf(doc.body().child(0).getElementsByTag("li").get(i).getElementsByTag("strong"));
                    if (temp.equals("")) {
                        continue;
                    }
                    String name = (String) temp.subSequence(8, temp.length() - 9);
                    if (!names.contains(name)) {
                        // add to the list
                        newNames.add(name);
                        names.add(name);
                        printAndLog("OK " + new Date() + " New Name: " + name);
                    }
                }
                if (newNames.size() > 0) {
                    String subject = getSubject(newNames);
                    // send email
                    Email email = new Email(subject, "I nomi presenti nell'oggetto dell'email sono stati aggiunti sul sito!\nCordiali saluti,\nAndrea Gonzato");
                    email.sendMail(recipient);
                }
            } catch (IOException e) {
                printAndLog("ER " + new Date() + " IOException raised during parsing html file " + e.getCause() + e.getMessage());
                e.printStackTrace();
            }


            try {
                TimeUnit.SECONDS.sleep(LOOP_TIME); // wait LOOP_TIME seconds
            } catch (InterruptedException e) {
                printAndLog("ER " + new Date() + " SleepException, " + e.getCause() + e.getMessage());
            }

            Date diff = new Date(new Date().getTime() - lastUpdate.getTime());
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(diff);
            int hours = calendar.get(Calendar.HOUR);
            if (hours > 0) {
                printAndLog("ST " + new Date() + " Online, email sent: " + Email.getEmailSent() + " List names size: " + names.size());
                lastUpdate = new Date();
            }

        }
    }

    private static String getSubject(List<String> newNames) {
        StringBuilder subject = new StringBuilder();
        for (int i = 0; i < newNames.size(); i++) {
            subject.append(newNames.get(i));
            if (i < newNames.size() - 1) {
                subject.append(", ");
            }
        }
        return subject.toString();
    }

    public static void scanFirstTime() {
        int var = Email.getEmailSent(); // just to be sure that static block of Email run
        printAndLog("OK -----------------initial---------------------");
        downloadWebPage(URL_TO_SCRAPE);
        File file = new File(HTML_FILE_NAME);
        try {
            Document doc = Jsoup.parse(file, "UTF-8", URL_TO_SCRAPE);
            int listSize = doc.body().child(0).getElementsByTag("li").size();
            for (int i = 0; i < listSize; i++) {
                String temp = String.valueOf(doc.body().child(0).getElementsByTag("li").get(i).getElementsByTag("strong"));
                if (temp.equals("")) {
                    continue;
                }
                // clear the String removing <strong>...<\strong>
                String name = (String) temp.subSequence(8, temp.length() - 9);

                if (!names.contains(name)) {
                    names.add(name);
                    printAndLog("OK " + new Date() + " Initial Name: " + name);
                }

            }
            printAndLog("OK -----------------initial---------------------");
        } catch (IOException e) {
            printAndLog("ER " + new Date() + " IOException raised during parsing html file " + e.getCause() + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void downloadWebPage(String webpage) {
        BufferedReader readr = null;
        BufferedWriter writer = null;
        try {
            // Create URL object
            URL url = new URL(webpage);
            readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            // Enter filename in which you want to download
            writer =
                    new BufferedWriter(new FileWriter(HTML_FILE_NAME));

            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line);
            }
        }
        // Exceptions
        catch (MalformedURLException mue) {
            printAndLog("ER " + new Date() + " Malformed URL Exception raised. " + mue.getCause() + mue.getMessage());
        } catch (IOException ie) {
            printAndLog("ER " + new Date() + " Can not download Web Page");
        } finally {
            try {
                if (readr != null) {
                    readr.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                printAndLog("ER " + new Date() + " Unable to close resources in downloadWebPage");
            }
        }
    }


    public static void printAndLog(String text) {
        System.out.println(text);
        log(text);
    }

    public static void log(String text) {
        File file = new File(LOG_FILE_NAME);
        try (FileWriter fr = new FileWriter(file, true)) {
            fr.write(text + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
