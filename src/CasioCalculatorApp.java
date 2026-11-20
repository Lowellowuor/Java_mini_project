import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class CasioCalculatorApp extends JFrame {
    private final ScientificCalculatorService service = new ScientificCalculatorService();
    private final JTextField displayField = new JTextField();
    private final JLabel statusLabel = new JLabel("DEG");
    private boolean shiftActive = false;
    private boolean alphaActive = false;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##########");
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);

    public CasioCalculatorApp() {
        setTitle("Casio FX-350ES Plus 2nd Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(createDisplayPanel(), BorderLayout.NORTH);
        add(createKeypadPanel(), BorderLayout.CENTER);
        setSize(560, 780);
        setMinimumSize(new Dimension(480, 680));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        statusLabel.setForeground(new Color(0x1A237E));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        displayField.setFont(new Font("Monospaced", Font.BOLD, 24));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(true);
        displayField.setBackground(new Color(0xE8F5E9));
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x1A237E), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        displayField.addActionListener(e -> evaluate());
        displayField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clearAll();
                }
            }
        });
        panel.setPreferredSize(new Dimension(0, 90));
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(displayField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKeypadPanel() {
        JPanel keypad = new JPanel();
        keypad.setLayout(new BoxLayout(keypad, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new GridLayout(1, 4, 6, 6));
        topRow.add(createButton("SHIFT", () -> toggleShift()));
        topRow.add(createButton("ALPHA", () -> toggleAlpha()));
        topRow.add(createButton("MODE", () -> toggleMode()));
        topRow.add(createButton("ON", () -> clearAll()));
        keypad.add(topRow);
        keypad.add(Box.createVerticalStrut(8));

        JPanel sciPanel = new JPanel(new GridLayout(3, 5, 6, 6));
        sciPanel.add(createInsertButton("Abs", "abs(", null, "M"));
        sciPanel.add(createInsertButton("x⁻¹", "^(-1)", null, null));
        sciPanel.add(createInsertButton("log", "log(", "10^(", "D"));
        sciPanel.add(createInsertButton("ln", "ln(", "e^(", "E"));
        sciPanel.add(createInsertButton("√", "sqrt(", "^2", "F"));
        sciPanel.add(createInsertButton("sin", "sin(", "asin(", "A"));
        sciPanel.add(createInsertButton("cos", "cos(", "acos(", "B"));
        sciPanel.add(createInsertButton("tan", "tan(", "atan(", "C"));
        sciPanel.add(createInsertButton("x²", "^2", "^3", "X"));
        sciPanel.add(createInsertButton("x³", "^3", null, "Y"));
        sciPanel.add(createInsertButton("(", "(", null, null));
        sciPanel.add(createInsertButton(")", ")", null, null));
        sciPanel.add(createInsertButton("π", "π", "e", null));
        sciPanel.add(createInsertButton("e", "e", null, null));
        sciPanel.add(createInsertButton("^", "^", null, null));
        keypad.add(sciPanel);
        keypad.add(Box.createVerticalStrut(8));

        JPanel numPanel = new JPanel(new GridLayout(5, 4, 6, 6));
        numPanel.add(createInsertButton("7", "7", null, null));
        numPanel.add(createInsertButton("8", "8", null, null));
        numPanel.add(createInsertButton("9", "9", null, null));
        numPanel.add(createButton("DEL", () -> backspace()));
        numPanel.add(createInsertButton("4", "4", null, null));
        numPanel.add(createInsertButton("5", "5", null, null));
        numPanel.add(createInsertButton("6", "6", null, null));
        numPanel.add(createInsertButton("×", "×", null, null));
        numPanel.add(createInsertButton("1", "1", null, null));
        numPanel.add(createInsertButton("2", "2", null, null));
        numPanel.add(createInsertButton("3", "3", null, null));
        numPanel.add(createInsertButton("÷", "÷", null, null));
        numPanel.add(createInsertButton("0", "0", null, null));
        numPanel.add(createInsertButton(".", ".", null, null));
        numPanel.add(createInsertButton("+", "+", null, null));
        numPanel.add(createInsertButton("-", "-", null, null));
        numPanel.add(createInsertButton("Ans", "Ans", null, null));
        numPanel.add(createInsertButton("EXP", "×10^(", "π", "e"));
        numPanel.add(createButton("AC", () -> clearAll()));
        numPanel.add(createButton("=", () -> evaluate()));
        keypad.add(numPanel);

        return keypad;
    }

    private JButton createButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JButton createInsertButton(String label, String primary, String shift, String alpha) {
        JButton button = new JButton(label);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.addActionListener(e -> handleInsert(primary, shift, alpha));
        return button;
    }

    private void handleInsert(String primary, String shift, String alpha) {
        String text;
        if (shiftActive && shift != null) {
            text = shift;
        } else if (alphaActive && alpha != null) {
            text = alpha;
        } else {
            text = primary;
        }
        resetModifiers();
        insert(text);
    }

    private void insert(String text) {
        String current = displayField.getText();
        if (current.equals("Math ERROR") || current.equals("Syntax ERROR")) {
            current = "";
        }
        displayField.setText(current + text);
        displayField.setCaretPosition(displayField.getText().length());
    }

    private void evaluate() {
        try {
            String expr = displayField.getText();
            if (expr.trim().isEmpty()) {
                return;
            }
            double result = service.evaluate(expr);
            displayField.setText(formatResult(result));
            displayField.setCaretPosition(displayField.getText().length());
        } catch (Exception ex) {
            displayField.setText("Math ERROR");
        }
        resetModifiers();
    }

    private String formatResult(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Math ERROR";
        }
        if (value == Math.floor(value) && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        if (Math.abs(value) >= 1e10 || (Math.abs(value) < 1e-9 && value != 0)) {
            String s = String.format("%.6e", value);
            return s.replace("e", "×10^");
        }
        return DECIMAL_FORMAT.format(value);
    }

    private void toggleShift() {
        shiftActive = !shiftActive;
        if (shiftActive) {
            alphaActive = false;
        }
        updateStatus();
    }

    private void toggleAlpha() {
        alphaActive = !alphaActive;
        if (alphaActive) {
            shiftActive = false;
        }
        updateStatus();
    }

    private void toggleMode() {
        service.toggleDegreeMode();
        updateStatus();
    }

    private void clearAll() {
        displayField.setText("");
        resetModifiers();
    }

    private void backspace() {
        String text = displayField.getText();
        if (text.equals("Math ERROR") || text.equals("Syntax ERROR")) {
            displayField.setText("");
            return;
        }
        if (!text.isEmpty()) {
            displayField.setText(text.substring(0, text.length() - 1));
            displayField.setCaretPosition(displayField.getText().length());
        }
    }

    private void resetModifiers() {
        shiftActive = false;
        alphaActive = false;
        updateStatus();
    }

    private void updateStatus() {
        StringBuilder sb = new StringBuilder();
        if (shiftActive) sb.append("SHIFT ");
        if (alphaActive) sb.append("ALPHA ");
        sb.append(service.isDegreeMode() ? "DEG" : "RAD");
        statusLabel.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CasioCalculatorApp().setVisible(true));
    }
}