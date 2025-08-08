import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SteganographyApp {

    private final JFrame frame;
    private final JTextArea messageArea;
    private final JPasswordField passwordField;
    private final JLabel filePathLabel;
    private File selectedFile;
    private JComboBox<String> fileTypeComboBox;

    // Holds encrypted message bytes for in-memory decrypt demo
    private byte[] encryptedMessageBytes;

    public SteganographyApp() {
        // Frame setup
        frame = new JFrame("🔐 Steganography & TEA Encryption Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 700);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Main panel with padding and background color
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Header label with style
        JLabel headerLabel = new JLabel("Secure Message Embedding & Extraction", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        headerLabel.setForeground(new Color(30, 30, 30));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // File selection panel
        JPanel fileSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        fileSelectionPanel.setBackground(new Color(245, 245, 245));

        fileTypeComboBox = new JComboBox<>(new String[]{"Image"});
        // Restricting to Image for now for best support

        JButton selectFileButton = new JButton("📂 Select File");
        selectFileButton.setFocusPainted(false);
        selectFileButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectFileButton.addActionListener(this::selectFile);

        filePathLabel = new JLabel("No file selected");
        filePathLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        filePathLabel.setForeground(Color.DARK_GRAY);

        fileSelectionPanel.add(new JLabel("File Type:"));
        fileSelectionPanel.add(fileTypeComboBox);
        fileSelectionPanel.add(selectFileButton);
        fileSelectionPanel.add(filePathLabel);

        mainPanel.add(fileSelectionPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Message and password input area
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 15));
        inputPanel.setBackground(new Color(245, 245, 245));

        messageArea = new JTextArea(10, 45);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageArea.setBorder(BorderFactory.createTitledBorder("Enter Message to Encrypt / Embed"));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        inputPanel.add(scrollPane);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(new Color(245, 245, 245));
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Password (for Encryption / Decryption)"));

        passwordField = new JPasswordField(25);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        inputPanel.add(passwordPanel);

        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 18, 18));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton encryptButton = new JButton("🔒 Encrypt Message");
        encryptButton.setFocusPainted(false);
        encryptButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        encryptButton.addActionListener(this::encryptMessageOnly);
        buttonPanel.add(encryptButton);

        JButton embedPlaintextButton = new JButton("🖼️ Embed Plaintext");
        embedPlaintextButton.setFocusPainted(false);
        embedPlaintextButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        embedPlaintextButton.addActionListener(this::embedPlaintext);
        buttonPanel.add(embedPlaintextButton);

        JButton combinedEmbedButton = new JButton("🔐 Encrypt & Embed");
        combinedEmbedButton.setFocusPainted(false);
        combinedEmbedButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        combinedEmbedButton.addActionListener(this::embedEncryptedMessage);
        buttonPanel.add(combinedEmbedButton);

        JButton decryptButton = new JButton("🔓 Decrypt Message");
        decryptButton.setFocusPainted(false);
        decryptButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        decryptButton.addActionListener(this::decryptMessageOnly);
        buttonPanel.add(decryptButton);

        JButton extractPlaintextButton = new JButton("📤 Extract Plaintext");
        extractPlaintextButton.setFocusPainted(false);
        extractPlaintextButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        extractPlaintextButton.addActionListener(this::extractPlaintext);
        buttonPanel.add(extractPlaintextButton);

        JButton combinedExtractButton = new JButton("🔍 Extract & Decrypt");
        combinedExtractButton.setFocusPainted(false);
        combinedExtractButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        combinedExtractButton.addActionListener(this::extractAndDecryptMessage);
        buttonPanel.add(combinedExtractButton);

        mainPanel.add(buttonPanel);

        // Add main panel to frame
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // Select file with extension filter and update label
    private void selectFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image File");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathLabel.setText(selectedFile.getName());
        }
    }

    // Encrypt message only (stores encrypted bytes in memory)
    private void encryptMessageOnly(ActionEvent e) {
        String message = messageArea.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (message.isEmpty() || password.isEmpty()) {
            showError("Message and password cannot be empty.");
            return;
        }

        try {
            byte[] key = createMD5Key(password);
            encryptedMessageBytes = TEA.encryptWithPadding(message.getBytes(StandardCharsets.UTF_8), key);
            showInfo("Message encrypted successfully and stored in memory.");
        } catch (Exception ex) {
            showError("Encryption failed: " + ex.getMessage());
        }
    }

    // Decrypt message from memory
    private void decryptMessageOnly(ActionEvent e) {
        if (encryptedMessageBytes == null) {
            showError("No encrypted message found in memory. Please encrypt a message first.");
            return;
        }
        String password = new String(passwordField.getPassword()).trim();
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        try {
            byte[] key = createMD5Key(password);
            byte[] decrypted = TEA.decryptWithPadding(encryptedMessageBytes, key);
            String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
            messageArea.setText(decryptedText);
            showInfo("Message decrypted successfully!");
        } catch (Exception ex) {
            showError("Decryption failed: Possibly incorrect password or corrupted data.");
        }
    }

    // Embed plaintext (no encryption) into selected image and save output file
    private void embedPlaintext(ActionEvent e) {
        if (!validateFileAndMessage()) return;
        String message = messageArea.getText().trim();

        try {
            boolean success = Steganography.hideMessageWithSaveDialog(selectedFile, message.getBytes(StandardCharsets.UTF_8));
            if (success) {
                showInfo("Plaintext message embedded and saved successfully!");
            } else {
                showError("Failed to embed message. Image may be too small.");
            }
        } catch (Exception ex) {
            showError("Embedding failed: " + ex.getMessage());
        }
    }

    // Extract plaintext from selected image
    private void extractPlaintext(ActionEvent e) {
        if (selectedFile == null) {
            showError("Please select an image file first.");
            return;
        }

        try {
            byte[] extracted = Steganography.extractMessageFromImage(selectedFile);
            if (extracted == null) {
                showError("No hidden message found or unsupported image.");
                return;
            }
            String extractedText = new String(extracted, StandardCharsets.UTF_8);
            messageArea.setText(extractedText);
            showInfo("Plaintext message extracted successfully!");
        } catch (Exception ex) {
            showError("Extraction failed: " + ex.getMessage());
        }
    }

    // Encrypt and embed message, save output file
    private void embedEncryptedMessage(ActionEvent e) {
        if (!validateFileAndMessage()) return;
        String message = messageArea.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        try {
            byte[] key = createMD5Key(password);
            byte[] encrypted = TEA.encryptWithPadding(message.getBytes(StandardCharsets.UTF_8), key);
            boolean success = Steganography.hideMessageWithSaveDialog(selectedFile, encrypted);
            if (success) {
                showInfo("Message encrypted, embedded, and saved successfully!");
            } else {
                showError("Failed to embed message. Image may be too small.");
            }
        } catch (Exception ex) {
            showError("Encryption or embedding failed: " + ex.getMessage());
        }
    }

    // Extract and decrypt message from image
    private void extractAndDecryptMessage(ActionEvent e) {
        if (selectedFile == null) {
            showError("Please select an image file first.");
            return;
        }
        String password = new String(passwordField.getPassword()).trim();
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        try {
            byte[] extracted = Steganography.extractMessageFromImage(selectedFile);
            if (extracted == null) {
                showError("No hidden message found or unsupported image.");
                return;
            }
            byte[] key = createMD5Key(password);
            byte[] decrypted = TEA.decryptWithPadding(extracted, key);
            String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
            messageArea.setText(decryptedText);
            showInfo("Message extracted and decrypted successfully!");
        } catch (Exception ex) {
            showError("Extraction or decryption failed: Possibly incorrect password or corrupted data.");
        }
    }

    private boolean validateFileAndMessage() {
        if (selectedFile == null) {
            showError("Please select an image file first.");
            return false;
        }
        if (messageArea.getText().trim().isEmpty()) {
            showError("Message cannot be empty.");
            return false;
        }
        return true;
    }

    // Helper: Create MD5 hash key from password (128-bit)
    private byte[] createMD5Key(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(md.digest(), 16);
    }

    // Helper: Show error dialog
    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper: Show info dialog
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "✅ Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SteganographyApp::new);
    }
}
