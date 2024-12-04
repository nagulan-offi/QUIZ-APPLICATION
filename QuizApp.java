import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

class Question implements Serializable {
    private String questionText;
    private ArrayList<String> options;
    private int correctOption;

    public Question(String questionText, ArrayList<String> options, int correctOption) {
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getQuestionText() {
        return questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public boolean isCorrect(int userAnswer) {
        return userAnswer == correctOption;
    }
}

class Quiz implements Serializable {
    private ArrayList<Question> questions;

    public Quiz() {
        questions = new ArrayList<>();
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void start(JFrame frame) {
        int score = 0;
        for (Question question : questions) {
            int userAnswer = displayQuestion(frame, question);
            if (userAnswer == -1) {
                JOptionPane.showMessageDialog(frame, "No answer selected for this question.");
            } else if (question.isCorrect(userAnswer)) {
                score++;
            }
        }

        double percentage = ((double) score / questions.size()) * 100;
        String feedback;
        if (percentage == 100) {
            feedback = "Perfect score! You're a genius! 🎉";
        } else if (percentage >= 80) {
            feedback = "Great job! You scored really well. Keep it up! 💪";
        } else if (percentage >= 50) {
            feedback = "Good effort! You can do even better with some more practice! 👍";
        } else {
            feedback = "Don't worry, keep trying, and you'll improve! 🌟";
        }

        JOptionPane.showMessageDialog(frame,
                "Quiz Over! Your final score: " + score + "/" + questions.size() +
                        "\n" + feedback);
    }

    private int displayQuestion(JFrame frame, Question question) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JLabel questionLabel = new JLabel("<html><b>" + question.getQuestionText() + "</b></html>");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(questionLabel);

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] options = new JRadioButton[question.getOptions().size()];
        for (int i = 0; i < question.getOptions().size(); i++) {
            options[i] = new JRadioButton(question.getOptions().get(i));
            group.add(options[i]);
            panel.add(options[i]);
        }

        int result = JOptionPane.showConfirmDialog(frame, panel, "Question", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].isSelected()) {
                    return i;
                }
            }
        }
        return -1;
    }
}

public class QuizApp {
    private static final String QUIZ_STORAGE_FILE = "quizzes.ser";
    private static final String DEFAULT_QUIZ_NAME = "Ultimate Trivia Challenge";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Quiz App");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        HashMap<String, Quiz> quizzes = loadQuizzes();

        if (quizzes.isEmpty()) {
            quizzes.put(DEFAULT_QUIZ_NAME, createDefaultQuiz());
            saveQuizzes(quizzes);
        }

        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton createQuizButton = new JButton("Create Quiz");
        JButton playQuizButton = new JButton("Play Quiz");
        JButton deleteQuizButton = new JButton("Delete Quiz");
        JButton exitButton = new JButton("Exit");

        mainPanel.add(createQuizButton);
        mainPanel.add(playQuizButton);
        mainPanel.add(deleteQuizButton);
        mainPanel.add(exitButton);

        frame.add(mainPanel);
        frame.setVisible(true);

        createQuizButton.addActionListener(e -> createQuiz(frame, quizzes));
        playQuizButton.addActionListener(e -> playQuiz(frame, quizzes));
        deleteQuizButton.addActionListener(e -> deleteQuiz(frame, quizzes));
        exitButton.addActionListener(e -> {
            saveQuizzes(quizzes);
            frame.dispose();
        });
    }

    private static void createQuiz(JFrame frame, HashMap<String, Quiz> quizzes) {
        String quizName = JOptionPane.showInputDialog(frame, "Enter the name of the quiz:");
        if (quizName == null || quizName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Quiz name cannot be empty!");
            return;
        }

        String questionCountInput = JOptionPane.showInputDialog(frame, "Enter the number of questions:");
        try {
            int questionCount = Integer.parseInt(questionCountInput);
            Quiz quiz = new Quiz();
            for (int i = 0; i < questionCount; i++) {
                // Ask for the question text
                JTextField questionField = new JTextField();
                JPanel questionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                questionPanel.add(new JLabel("Enter question " + (i + 1) + ":"));
                questionPanel.add(questionField);

                int result = JOptionPane.showConfirmDialog(frame, questionPanel, "Enter Question", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String questionText = questionField.getText().trim();
                    if (questionText.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Question text cannot be empty. Please try again.");
                        return;
                    }

                    // Ask for the number of options for the question
                    String optionCountInput = JOptionPane.showInputDialog(frame, "Enter the number of options for Question " + (i + 1) + ":");
                    int optionCount = Integer.parseInt(optionCountInput);
                    if (optionCount < 2) {
                        JOptionPane.showMessageDialog(frame, "A question must have at least 2 options!");
                        return;
                    }

                    // Create a panel to input the options
                    JTextField[] optionFields = new JTextField[optionCount];
                    JPanel optionPanel = new JPanel(new GridLayout(optionCount + 1, 1, 5, 5));
                    optionPanel.add(new JLabel("Enter options:"));
                    for (int j = 0; j < optionCount; j++) {
                        optionFields[j] = new JTextField();
                        optionPanel.add(new JLabel("Option " + (j + 1) + ":"));
                        optionPanel.add(optionFields[j]);
                    }

                    // Ask for the correct option number
                    String correctOptionInput = JOptionPane.showInputDialog(frame, optionPanel, "Select Correct Option", JOptionPane.PLAIN_MESSAGE);
                    int correctOption = Integer.parseInt(correctOptionInput) - 1;
                    if (correctOption < 0 || correctOption >= optionCount) {
                        JOptionPane.showMessageDialog(frame, "Invalid option selected for the correct answer.");
                        return;
                    }

                    // Add the options to the question
                    ArrayList<String> options = new ArrayList<>();
                    for (JTextField optionField : optionFields) {
                        options.add(optionField.getText().trim());
                    }

                    // Create and add the question to the quiz
                    Question question = new Question(questionText, options, correctOption);
                    quiz.addQuestion(question);
                }
            }
            quizzes.put(quizName, quiz);
            saveQuizzes(quizzes);
            JOptionPane.showMessageDialog(frame, "Quiz \"" + quizName + "\" created successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input! Please enter a valid number.");
        }
    }

    private static void playQuiz(JFrame frame, HashMap<String, Quiz> quizzes) {
        if (quizzes.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No quizzes available to play!");
            return;
        }
        String[] quizNames = quizzes.keySet().toArray(new String[0]);
        String selectedQuiz = (String) JOptionPane.showInputDialog(frame, "Select a quiz to play:",
                "Play Quiz", JOptionPane.PLAIN_MESSAGE, null, quizNames, quizNames[0]);
        if (selectedQuiz != null) {
            quizzes.get(selectedQuiz).start(frame);
        }
    }

    private static void deleteQuiz(JFrame frame, HashMap<String, Quiz> quizzes) {
        if (quizzes.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No quizzes available to delete!");
            return;
        }
        String[] quizNames = quizzes.keySet().toArray(new String[0]);
        String selectedQuiz = (String) JOptionPane.showInputDialog(frame, "Select a quiz to delete:",
                "Delete Quiz", JOptionPane.PLAIN_MESSAGE, null, quizNames, quizNames[0]);
        if (selectedQuiz != null && !selectedQuiz.equals(DEFAULT_QUIZ_NAME)) {
            quizzes.remove(selectedQuiz);
            saveQuizzes(quizzes);
            JOptionPane.showMessageDialog(frame, "Quiz deleted successfully!");
        } else if (selectedQuiz != null) {
            JOptionPane.showMessageDialog(frame, "The default quiz cannot be deleted!");
        }
    }

    private static Quiz createDefaultQuiz() {
        Quiz quiz = new Quiz();
        quiz.addQuestion(new Question("What is 2 + 2?", new ArrayList<>(Arrays.asList("2", "4", "6", "8")), 1));
        return quiz;
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Quiz> loadQuizzes() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("quizzes.ser"))) {
            return (HashMap<String, Quiz>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    private static void saveQuizzes(HashMap<String, Quiz> quizzes) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("quizzes.ser"))) {
            oos.writeObject(quizzes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
