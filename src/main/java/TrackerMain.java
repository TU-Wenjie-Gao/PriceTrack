import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TrackerMain extends JFrame {
    private JTextField urlField;
    private JTextArea resultArea;

    public TrackerMain() {
        setTitle("Amazon Price Scraper");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        urlField = new JTextField(20);
        JButton scrapeButton = new JButton("Scrape Price");
        inputPanel.add(urlField);
        inputPanel.add(scrapeButton);

        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        scrapeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amazonUrl = urlField.getText();
                if (!amazonUrl.isEmpty()) {
                    try {
                        String price = scrapeAmazonPrice(amazonUrl);
                        resultArea.setText("Price: " + price);
                    } catch (IOException ex) {
                        resultArea.setText("Error: " + ex.getMessage());
                    }
                }
            }
        });

        add(panel);
    }

    private String scrapeAmazonPrice(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response code: " + response);
        }

        // Parse the HTML content using Jsoup
        Document doc = Jsoup.parse(response.body().string());

        // Modify the CSS selector to match the new structure if needed
        String price = doc.body().select("#mainContent > div > div.vim.x-price-section.mar-t-16.bor-top.pad-t-24 > div > div > div.x-price-primary > span").text();
        System.out.println("Found Price is " + price);


        if (price.isEmpty()) {
            // Try to locate the price using a different selector
            price = doc.select("#corePriceDisplay_desktop_feature_div > div.a-section.a-spacing-none.aok-align-center > span.a-price.aok-align-center.reinventPricePriceToPayMargin.priceToPay > span.a-offscreen").text();
        }


        Elements elements = doc.body().select("*");

        for (Element element : elements) {
            String value = element.ownText();
            System.out.println("Line: " + value);
            if (value.contains("$")) return value;
        }


        return "Not found";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrackerMain scraper = new TrackerMain();
            scraper.setVisible(true);
        });
    }
}
