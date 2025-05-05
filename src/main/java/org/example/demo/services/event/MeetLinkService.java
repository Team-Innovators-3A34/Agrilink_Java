package org.example.demo.services.event;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MeetLinkService extends Service<String> {

    private final String phpPath = "C:\\xampp\\php\\php.exe";
    private final String scriptPath = "C:\\xampp\\htdocs\\mywebsite\\agrilink\\test.php";

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                ProcessBuilder builder = new ProcessBuilder(phpPath, scriptPath);
                builder.redirectErrorStream(true);

                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0 && output.toString().startsWith("http")) {
                    return output.toString();
                } else {
                    throw new RuntimeException("PHP script failed: " + output);
                }
            }
        };
    }
}
