package Main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SettingsFile {
    public SettingsFile() {

    }

    public static byte[] LoadROM(String ROMFile) {
        byte[] memory = new byte[2048];

        try {
            FileInputStream file = new FileInputStream(ROMFile);
            try {
                memory = file.readAllBytes();
            } catch (IOException e) {
                System.out.println("Error reading ROM file: " + e.getMessage());
            }
        } catch(FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return memory;
    }
}