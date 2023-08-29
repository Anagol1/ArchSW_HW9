package org.example;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static List<Book> books = new ArrayList<>();
    private static AtomicInteger idGenerator = new AtomicInteger(1);
    private static ObjectMapper objectMapper = new ObjectMapper(); // Создаем объект ObjectMapper

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/books", new BooksHandler());
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String responseBody = objectMapper.writeValueAsString(books);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } else if ("POST".equals(exchange.getRequestMethod())) {
                int newId = idGenerator.getAndIncrement();
                Book newBook = new Book(newId, "1984", "George Orwell", 1949);
                books.add(newBook);
                Book newBook2 = new Book(newId, "Oliver Twist", "Charles Dickens", 1934);
                books.add(newBook2);
                String responseBody = objectMapper.writeValueAsString(newBook);
                String responseBody2 = objectMapper.writeValueAsString(newBook2);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(201, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                if (pathParts.length == 3) {
                    int bookId = Integer.parseInt(pathParts[2]);
                    books.removeIf(book -> book.getId() == bookId);
                    String responseBody = "{\"message\":\"Book with ID " + bookId + " removed.\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBody.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBody.getBytes());
                    }
                }
            } else {
                String responseBody = "{\"error\":\"Method not allowed\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            }
        }
    }

    static class Book {
        private int id;
        private String title;
        private String author;
        private int year;

        public Book(int id, String title, String author, int year) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
        }


        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        @Override
        public String toString() {
            return "Book{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", year=" + year +
                    '}';
        }
    }
}