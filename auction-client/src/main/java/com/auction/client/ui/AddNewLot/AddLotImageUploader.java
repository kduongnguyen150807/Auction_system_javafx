package com.auction.client.ui.AddNewLot;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.scene.image.Image;

/** Uploads lot images to Cloudinary on a background thread. */
final class AddLotImageUploader {

  interface Callbacks {
    void onSuccess(String imageUrl, Image preview);

    void onFailure();
  }

  private AddLotImageUploader() {}

  static void uploadAsync(File file, AtomicLong generation, Callbacks callbacks) {
    if (file == null || callbacks == null) {
      return;
    }
    final long gen = generation.incrementAndGet();
    Thread thread =
        new Thread(
            () -> {
              try {
                String boundary = "boundary123";
                byte[] head =
                    ("--"
                            + boundary
                            + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n")
                        .getBytes();
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                byte[] tail =
                    ("\r\n--"
                            + boundary
                            + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--"
                            + boundary
                            + "--\r\n")
                        .getBytes();
                byte[] body = new byte[head.length + fileBytes.length + tail.length];
                System.arraycopy(head, 0, body, 0, head.length);
                System.arraycopy(fileBytes, 0, body, head.length, fileBytes.length);
                System.arraycopy(tail, 0, body, head.length + fileBytes.length, tail.length);
                HttpRequest req =
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();
                HttpResponse<String> response =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();
                if (!responseBody.contains("\"secure_url\"")) {
                  Platform.runLater(
                      () -> {
                        if (gen == generation.get()) {
                          callbacks.onFailure();
                        }
                      });
                  return;
                }
                String url = responseBody.split("\"secure_url\":\"")[1].split("\"")[0];
                if (url.contains(".webp")) {
                  url = url.replace(".webp", ".jpg");
                }
                String finalUrl = url;
                Platform.runLater(
                    () -> {
                      if (gen == generation.get()) {
                        callbacks.onSuccess(finalUrl, new Image(finalUrl, true));
                      }
                    });
              } catch (Exception ex) {
                Platform.runLater(
                    () -> {
                      if (gen == generation.get()) {
                        callbacks.onFailure();
                      }
                    });
              }
            });
    thread.setDaemon(true);
    thread.start();
  }
}
