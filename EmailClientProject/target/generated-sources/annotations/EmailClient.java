import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class EmailClient extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton receiveButton;
    private JFrame loginFrame;
    private JFrame emailFrame;
    private Session session;

    public EmailClient() {
        // Setup login frame
        loginFrame = new JFrame("JavaMail Email Client - Login");
        loginFrame.setSize(400, 200);
        loginFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(3, 2, 10, 10));

        emailField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginFrame.add(new JLabel("Email:"));
        loginFrame.add(emailField);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passwordField);
        loginFrame.add(new JPanel()); // Empty panel for spacing
        loginFrame.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        loginFrame.setLocationRelativeTo(null); // Center the login window
        loginFrame.setVisible(true);

        // Setup email frame
        emailFrame = new JFrame("JavaMail Email Client - Send and Receive Emails");
        emailFrame.setSize(600, 400);
        emailFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        emailFrame.setLayout(new BorderLayout(10, 10));

        JPanel emailPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        toField = new JTextField();
        subjectField = new JTextField();
        messageArea = new JTextArea();
        sendButton = new JButton("Send Email");
        receiveButton = new JButton("Receive Email");

        emailPanel.add(new JLabel("To:"));
        emailPanel.add(toField);
        emailPanel.add(new JLabel("Subject:"));
        emailPanel.add(subjectField);
        emailPanel.add(new JLabel("Message:"));
        emailPanel.add(new JScrollPane(messageArea));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(receiveButton);

        emailFrame.add(emailPanel, BorderLayout.CENTER);
        emailFrame.add(buttonPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendEmail();
            }
        });

        receiveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                receiveEmail();
            }
        });

        emailFrame.setLocationRelativeTo(null); // Center the email window
    }

    private void login() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        JOptionPane.showMessageDialog(loginFrame, "Login successful!");
        loginFrame.setVisible(false);
        emailFrame.setVisible(true);
    }

    private void sendEmail() {
        try {
            String to = toField.getText();
            String subject = subjectField.getText();
            String messageText = messageArea.getText();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailField.getText()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            JOptionPane.showMessageDialog(emailFrame, "Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(emailFrame, "Error sending email: " + e.getMessage());
        }
    }

    private void receiveEmail() {
        try {
            Properties properties = new Properties();
            properties.put("mail.pop3.host", "pop.gmail.com");
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");

            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("pop3s");
            store.connect("pop.gmail.com", email, password);

            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            System.out.println("Total messages: " + messages.length);

            for (Message message : messages) {
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);

                Object content = message.getContent();
                if (content instanceof String) {
                    System.out.println("Text: " + content);
                } else if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (bodyPart.isMimeType("text/plain")) {
                            System.out.println("Text: " + bodyPart.getContent());
                        } else if (bodyPart.isMimeType("text/html")) {
                            String html = (String) bodyPart.getContent();
                            System.out.println("HTML: " + html);
                        }
                    }
                }
            }

            emailFolder.close(false);
            store.close();

            JOptionPane.showMessageDialog(emailFrame, "Emails received successfully!");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(emailFrame, "No provider: " + e.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(emailFrame, "Messaging exception: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(emailFrame, "IO exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        EmailClient client = new EmailClient();
        client.setVisible(true);
    }
}
