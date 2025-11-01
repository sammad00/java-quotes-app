import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {
    private static List<String> quotes;

    public static void main(String[] args) throws IOException {
        // Load quotes from external file
        quotes = loadQuotesFromFile("quotes.txt");
        if (quotes.isEmpty()) {
            System.err.println("No quotes found. Please make sure 'quotes.txt' exists.");
            return;
        }

        // Create an HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve HTML page with animated background
        server.createContext("/", exchange -> {
            String htmlResponse = getHTMLPage();
            sendResponse(exchange, htmlResponse, "text/html");
        });

        // Serve API endpoint for random quotes
        server.createContext("/api/quote", exchange -> {
            String quote = getRandomQuote();
            String jsonResponse = String.format("{\"quote\": \"%s\"}", quote);
            sendResponse(exchange, jsonResponse, "application/json");
        });

        server.start();
        System.out.println("🌐 Server running on http://localhost:8000");
        System.out.println("📜 Random quote API -> http://localhost:8000/api/quote");
    }

    // Helper: send response
    private static void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType + "; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Helper: random quote
    private static String getRandomQuote() {
        Random random = new Random();
        return quotes.get(random.nextInt(quotes.size()));
    }

    // Helper: load quotes from file
    private static List<String> loadQuotesFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return List.of();
        }
    }

    // HTML Page with animated backgrounds and quote overlay
    private static String getHTMLPage() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dynamic Quote Viewer</title>
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    overflow: hidden;
                    font-family: 'Segoe UI', sans-serif;
                    color: white;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }

                /* Slideshow background */
                .background {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background-size: cover;
                    background-position: center;
                    animation: slideShow 30s infinite;
                    z-index: -1;
                }

                @keyframes slideShow {
                    0% { background-image: url('https://source.unsplash.com/1920x1080/?nature'); }
                    25% { background-image: url('https://source.unsplash.com/1920x1080/?animals'); }
                    50% { background-image: url('https://source.unsplash.com/1920x1080/?city,buildings'); }
                    75% { background-image: url('https://source.unsplash.com/1920x1080/?mountains,forest'); }
                    100% { background-image: url('https://source.unsplash.com/1920x1080/?sea,sky'); }
                }

                .quote-box {
                    text-align: center;
                    background: rgba(0, 0, 0, 0.4);
                    padding: 40px 60px;
                    border-radius: 20px;
                    box-shadow: 0 4px 30px rgba(0,0,0,0.5);
                    max-width: 800px;
                    animation: fadeIn 2s;
                }

                @keyframes fadeIn {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }

                h1 {
                    font-size: 2rem;
                    line-height: 1.5;
                    font-style: italic;
                }

                .refresh {
                    margin-top: 20px;
                    padding: 10px 20px;
                    border: none;
                    border-radius: 10px;
                    background: #ffffff88;
                    color: #000;
                    font-weight: bold;
                    cursor: pointer;
                    transition: background 0.3s;
                }

                .refresh:hover {
                    background: #ffffffcc;
                }
            </style>
        </head>
        <body>
            <div class="background"></div>
            <div class="quote-box">
                <h1 id="quote">Loading quote...</h1>
                <button class="refresh" onclick="loadQuote()">New Quote</button>
            </div>

            <script>
                async function loadQuote() {
                    const res = await fetch('/api/quote');
                    const data = await res.json();
                    document.getElementById('quote').innerText = data.quote;
                }

                // Auto-load and refresh every 10 seconds
                loadQuote();
                setInterval(loadQuote, 10000);
            </script>
        </body>
        </html>
        """;
    }
}
