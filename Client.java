import java.io.*; 
import java.net.*; 
import javax.swing.*; 
import java.awt.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
//client
public class Client {
    private static boolean isInGame = false;
    private static boolean scoreShown = false;


private static PrintWriter out;
    private static String playerName;
    private static JFrame frame;
    private static DefaultListModel<String> playerListModel = new DefaultListModel<>();
    private static DefaultListModel<String> waitingListModel = new DefaultListModel<>();
    private static JList<String> playerList;
    private static JList<String> waitingList;
    private static JButton playButton;
    //private static JButton startGameButton;
    private static JButton connectButton;
    private static JLabel timerLabel;
    private static JPanel gamePlayersLabel;
    private static String currentQuestionText = "";
    private static JLabel questionLabel;
    private static Map<String, Integer> playerScores = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::showLoginScreen);
    }

    private static void showLoginScreen() {
        frame = new JFrame("Math Game - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(173, 216, 230));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel nameLabel = new JLabel("Enter Name:");
        nameLabel.setFont(new Font("Serif", Font.BOLD, 14));
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(10);
        nameField.setFont(new Font("Serif", Font.PLAIN, 14));
        panel.add(nameField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        connectButton = new JButton("Connect");
        styleButton(connectButton);
        panel.add(connectButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        connectButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (!playerName.isEmpty()) {
                new Thread(Client::connectToServer).start();
            }
        });
    }

    private static void showGameScreen() {
        scoreShown = false;
        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Connected Players");
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(173, 216, 230));

        JLabel titleLabel = new JLabel("The Connected Players", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        playerList = new JList<>(playerListModel);
        playerList.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playButton = new JButton("Play");
        styleButton(playButton);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(173, 216, 230));
        buttonPanel.add(playButton);

        frame.add(titleLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        playButton.addActionListener(e -> {
            if (out != null) {
                out.println("play");
            }
        });
    }

    private static void showWaitingRoom() {
        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Waiting Room");
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(173, 216, 230));

        JLabel titleLabel = new JLabel("Waiting Room", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        waitingList = new JList<>(waitingListModel);
        waitingList.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane waitingScrollPane = new JScrollPane(waitingList);
        waitingScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(173, 216, 230));
        // ❌ (Removed Start Game Button here)

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(173, 216, 230));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 14));
        timerLabel.setForeground(Color.RED);
        topPanel.add(timerLabel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(waitingScrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }


    private static void showGame() {
        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Question Time");
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(220, 235, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gamePlayersLabel = new JPanel();
        gamePlayersLabel.setLayout(new BoxLayout(gamePlayersLabel, BoxLayout.Y_AXIS));
        gamePlayersLabel.setBackground(new Color(220, 235, 245));
        updateGamePlayersLabel();

        JScrollPane playerPane = new JScrollPane(gamePlayersLabel);
        topPanel.add(playerPane, BorderLayout.WEST);

        JButton leaveButton = new JButton("Leave");
        styleButton(leaveButton);
        leaveButton.setBackground(new Color(204, 0, 0));
        topPanel.add(leaveButton, BorderLayout.EAST);
            
        timerLabel = new JLabel("Time: 120s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 14));
        timerLabel.setForeground(Color.RED);
        topPanel.add(timerLabel, BorderLayout.CENTER);


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        questionLabel = new JLabel(currentQuestionText, SwingConstants.CENTER);
        questionLabel.setFont(new Font("Serif", Font.BOLD, 20));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea answerArea = new JTextArea(2, 10);
        answerArea.setFont(new Font("Serif", Font.PLAIN, 16));
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(answerArea);
        scrollPane.setMaximumSize(new Dimension(150, 50));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton submitButton = new JButton("Submit");
        styleButton(submitButton);
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(submitButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();

        leaveButton.addActionListener(e -> {
            if (out != null) {
                out.println("LEAVE");
            }
            playerScores.remove(playerName);
            updateGamePlayersLabel();
            frame.dispose();
            System.exit(0);
        });

        submitButton.addActionListener(e -> {
            String answer = answerArea.getText().trim();
            if (!answer.isEmpty() && out != null) {
                out.println("ANSWER:" + answer);
                answerArea.setText("");
            }
        });
    }

    private static void updateGamePlayersLabel() {
        if (gamePlayersLabel == null) return;
        gamePlayersLabel.removeAll();
        for (String name : playerScores.keySet()) {
            JLabel label = new JLabel(name + " : " + playerScores.get(name));
            label.setFont(new Font("Serif", Font.PLAIN, 14));
            gamePlayersLabel.add(label);
        }
        gamePlayersLabel.revalidate();
        gamePlayersLabel.repaint();
    }

    private static void processServerMessage(String message) {
        if (message.contains("Players connected:")) {
            playerListModel.clear();
            for (String name : message.replace("Players connected: ", "").split(", ")) {
                if (!name.trim().isEmpty()) {
                    playerListModel.addElement(name);
                }
            }
        } else if (message.equals("WAITING_ROOM")) {
            SwingUtilities.invokeLater(Client::showWaitingRoom);
        } else if (message.startsWith("Waiting Room:")) {
            waitingListModel.clear();
            for (String name : message.replace("Waiting Room: ", "").split(", ")) {
                if (!name.trim().isEmpty()) {
                    waitingListModel.addElement(name);
                    playerScores.putIfAbsent(name, 0);
                    updateGamePlayersLabel();
                }
            }
        } else if (message.equals("WAITING_ROOM_FULL")) {
            if (playButton != null) playButton.setEnabled(false);
         
        } else if (message.equals("START_GAME_NOW")) {
                isInGame = true; 
                updateGamePlayersLabel();
            //playerScores.clear();
            showGame();
            if (out != null) out.println("READY_FOR_QUESTION"); 
        } else if (message.startsWith("TIMER:")) {
            if (timerLabel != null) {
                timerLabel.setText("Time: " + message.split(":")[1] + "s");
            }
        } else if (message.startsWith("QUESTION:")) {
            currentQuestionText = message.substring("QUESTION:".length());
            if (questionLabel != null) {
                questionLabel.setText(currentQuestionText);
            }
        } else if (message.startsWith("CORRECT:")) {
            String name = message.substring("CORRECT:".length());
            playerScores.put(name, playerScores.getOrDefault(name, 0) + 1);
            updateGamePlayersLabel();
        }
        
    else if (message.startsWith("WINNERS:")) {
    if (!isInGame || scoreShown) return;
    scoreShown = true;

    boolean isTimeUpNoWinner = false;
    String winnersRaw = message.substring("WINNERS:".length()).trim();

    if (winnersRaw.endsWith("TIME_UP:NO_WINNER")) {
        winnersRaw = winnersRaw.replace("TIME_UP:NO_WINNER", "").trim();
        isTimeUpNoWinner = true;
    }

    String[] lines = winnersRaw.split(", ");
    List<String> rankings = new ArrayList<>();
    String winner = "";
    int highestScore = -1;

    for (String line : lines) {
        if (!line.contains("(")) continue;
        String name = line.substring(0, line.indexOf(" ("));
        int score = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
        if (score > highestScore) {
            highestScore = score;
            winner = name;
        }
    }

    for (String line : lines) {
        if (!line.contains("(")) continue;
        String name = line.substring(0, line.indexOf(" ("));
        int score = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
        if (waitingListModel.contains(name)) {
            String label = name + " (" + score + " scores)";
            if (score == highestScore && !isTimeUpNoWinner) {
                label += " (Winner)";
            }
            rankings.add(label);
        }
    }

    if (isTimeUpNoWinner) {
        JOptionPane.showMessageDialog(frame, "⏰ Time's up! No winner.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    String finalWinner = isTimeUpNoWinner ? null : winner;
    SwingUtilities.invokeLater(() -> showScoreBoard(rankings, finalWinner));
}




        
        
        else if (message.startsWith("SCORES:")) {
            String[] entries = message.substring("SCORES:".length()).split(", ");
            for (String entry : entries) {
                if (!entry.isEmpty() && entry.contains(":")) {
                    String[] parts = entry.split(":");
                    playerScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            updateGamePlayersLabel();
        } else if (message.equals("TRY_AGAIN")) {
            JOptionPane.showMessageDialog(frame, "Try again!", "Incorrect", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith("REMOVE_PLAYER:")) {
            String removedName = message.substring("REMOVE_PLAYER:".length());
            playerScores.remove(removedName);
            for (int i = 0; i < waitingListModel.size(); i++) {
                if (waitingListModel.get(i).equals(removedName)) {
                    waitingListModel.remove(i);
                    break;
                }
            }
            updateGamePlayersLabel();
        }
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket("178.196..6.10", 5555); //leen's IP
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(playerName);
            SwingUtilities.invokeLater(Client::showGameScreen);
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        final String msg = serverMessage;
                        SwingUtilities.invokeLater(() -> processServerMessage(msg));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Serif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 102, 204));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 30));
    }
    
    private static void showScoreBoard(List<String> rankings, String winnerName) {
        JPanel scoreboardPanel = new JPanel(new BorderLayout());
        scoreboardPanel.setBackground(new Color(220, 235, 245));

        String scoreboardTitle = (winnerName == null) ? "🏁 Final Rankings (No Winner)" : "🏆 Final Rankings";
        JLabel title = new JLabel(scoreboardTitle, SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scoreboardPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(220, 235, 245));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (int i = 0; i < rankings.size(); i++) {
            JLabel label = new JLabel((i + 1) + ". " + rankings.get(i));
            label.setFont(new Font("Serif", Font.PLAIN, 16));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(label);
            centerPanel.add(Box.createVerticalStrut(5));
        }

        scoreboardPanel.add(centerPanel, BorderLayout.CENTER);

        frame.getContentPane().removeAll(); 
        frame.setTitle("Score Board");
        frame.setSize(400, 400);
        frame.add(scoreboardPanel);
        frame.revalidate();
        frame.repaint();
    }

}
