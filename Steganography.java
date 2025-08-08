import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Steganography {

    /**
     * Hides the given message bytes inside the provided image file and
     * prompts the user to save the stego image.
     * 
     * @param imageFile The original image file
     * @param message The message bytes to embed
     * @return true if embedding and saving succeed
     * @throws IOException if file read/write fails
     */
    public static boolean hideMessageWithSaveDialog(File imageFile, byte[] message) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);

        // Convert image to 3BYTE_BGR if not already for easier manipulation
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(image, 0, 0, null);
            image = convertedImg;
        }

        // Prepare message: length (4 bytes) + message bytes
        byte[] lengthBytes = intToByteArray(message.length);
        byte[] fullMessage = new byte[lengthBytes.length + message.length];
        System.arraycopy(lengthBytes, 0, fullMessage, 0, lengthBytes.length);
        System.arraycopy(message, 0, fullMessage, lengthBytes.length, message.length);

        int capacityBits = image.getWidth() * image.getHeight() * 3;
        if (fullMessage.length * 8 > capacityBits) {
            JOptionPane.showMessageDialog(null, "Message too large to fit in this image.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        byte[] imgBytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Embed message bits into the LSBs of image bytes
        for (int i = 0; i < fullMessage.length * 8; i++) {
            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8);
            int bit = (fullMessage[byteIndex] >> bitIndex) & 1;

            imgBytes[i] = (byte)((imgBytes[i] & 0xFE) | bit);
        }

        // Prompt user to save the new image
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Save Stego Image As");
        saveChooser.setSelectedFile(new File("stego_" + imageFile.getName()));
        int userSelection = saveChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File saveFile = saveChooser.getSelectedFile();
            String formatName = getExtension(saveFile.getName());
            if (!formatName.equalsIgnoreCase("png")) {
                // Force PNG for lossless compression
                saveFile = new File(saveFile.getAbsolutePath() + ".png");
                formatName = "png";
            }
            ImageIO.write(image, formatName, saveFile);
            return true;
        }

        return false;
    }

    /**
     * Extracts the hidden message bytes from the image file.
     * 
     * @param imageFile The image file with hidden message
     * @return The extracted message bytes, or null if no message found
     * @throws IOException If file reading fails
     */
    public static byte[] extractMessageFromImage(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);

        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(image, 0, 0, null);
            image = convertedImg;
        }

        byte[] imgBytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Extract first 32 bits = message length
        byte[] lengthBytes = new byte[4];
        for (int i = 0; i < 32; i++) {
            lengthBytes[i / 8] = (byte)((lengthBytes[i / 8] << 1) | (imgBytes[i] & 1));
        }
        int messageLength = byteArrayToInt(lengthBytes);

        if (messageLength <= 0 || messageLength > (imgBytes.length / 8)) {
            // Probably no hidden message or corrupted
            return null;
        }

        byte[] message = new byte[messageLength];

        for (int i = 0; i < messageLength * 8; i++) {
            int byteIndex = i / 8;
            message[byteIndex] = (byte)((message[byteIndex] << 1) | (imgBytes[32 + i] & 1));
        }

        return message;
    }

    // Helper methods

    private static byte[] intToByteArray(int val) {
        return new byte[]{
                (byte)(val >>> 24),
                (byte)(val >>> 16),
                (byte)(val >>> 8),
                (byte) val
        };
    }

    private static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) return "";
        return filename.substring(dotIndex + 1);
    }
}
