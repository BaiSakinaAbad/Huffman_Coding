import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class HuffmanForm {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HuffmanFrame frame = new HuffmanFrame();
            frame.setVisible(true);
        });
    }
}

class HuffmanFrame extends JFrame {
    private JTextArea inputTextArea, freqTableArea, codeTableArea;
    private JTextField decodeInputField;
    private HuffmanEncoder encoder;
    private HuffmanTreePanel treePanel;

    public HuffmanFrame() {
        setTitle("Huffman Coding");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize encoder
        encoder = new HuffmanEncoder();

        // Input Panel (Top: Text area with Encode button on the right)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputTextArea = new JTextArea(2, 40);
        inputTextArea.setLineWrap(true);
        inputTextArea.setText("enter a string here..."); // Placeholder text
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        JButton encodeButton = new JButton("ENCODE");
        encodeButton.addActionListener(new EncodeButtonListener());
        encodeButton.setBackground(new Color(255, 182, 193)); // Light pink background
        encodeButton.setForeground(Color.BLACK); // Button text color - can be changed here
        inputPanel.add(encodeButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.NORTH);

        // Center Panel (Split into tree on the left, freq and code tables on the right)
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Tree Panel (Center-Left)
        treePanel = new HuffmanTreePanel();
        centerPanel.add(treePanel, BorderLayout.CENTER);

        // Right Panel (Frequency table and code table stacked vertically)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));

        // Frequency Table Area
        freqTableArea = new JTextArea(5, 15);
        freqTableArea.setEditable(false);
        freqTableArea.setBackground(new Color(216, 191, 216)); // Light purple background - can be changed here
        rightPanel.add(new JScrollPane(freqTableArea));

        // Character Code Table Area
        codeTableArea = new JTextArea(5, 15);
        codeTableArea.setEditable(false);
        codeTableArea.setBackground(new Color(216, 191, 216)); // Light purple background - can be changed here
        rightPanel.add(new JScrollPane(codeTableArea));

        centerPanel.add(rightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Decode Panel (Bottom: Text field with Decode button on the right)
        JPanel decodePanel = new JPanel(new BorderLayout());
        decodeInputField = new JTextField(40);
        decodeInputField.setText("enter code to decode"); // Placeholder text
        JButton decodeButton = new JButton("DECODE");
        decodeButton.addActionListener(new DecodeButtonListener());
        decodeButton.setBackground(new Color(255, 182, 193)); // Light pink background - can be changed here
        decodeButton.setForeground(Color.BLACK); // Button text color
        decodePanel.add(decodeInputField, BorderLayout.CENTER);
        decodePanel.add(decodeButton, BorderLayout.EAST);
        add(decodePanel, BorderLayout.SOUTH);
    }

    private class EncodeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputTextArea.getText();
            if (input.isEmpty() || input.equals("enter a string here...")) {
                JOptionPane.showMessageDialog(HuffmanFrame.this, "Please enter text to encode.");
                return;
            }

            encoder.buildHuffmanTree(input);
            String encoded = encoder.encode(input);
            codeTableArea.setText("Huffman Codes:\n" + encoder.getHuffmanCodesString());
            freqTableArea.setText(encoder.getFrequencyTable());
            treePanel.setRoot(encoder.getRoot());
            treePanel.repaint();
        }
    }

    private class DecodeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String code = decodeInputField.getText();
            if (code.isEmpty() || code.equals("enter code to decode")) {
                JOptionPane.showMessageDialog(HuffmanFrame.this, "Please enter binary code to decode.");
                return;
            }

            String decoded = encoder.decode(code);
            JOptionPane.showMessageDialog(HuffmanFrame.this, "Decoded: " + decoded);
        }
    }
}

class HuffmanTreePanel extends JPanel {
    private HuffmanNode root;
    private static final int NODE_WIDTH = 50;
    private static final int NODE_HEIGHT = 40;
    private static final int LEVEL_HEIGHT = 70;
    private static final int HORIZONTAL_GAP = 20;
    private int nodeCounter;

    public HuffmanTreePanel() {
        setPreferredSize(new Dimension(600, 400));
    }

    public void setRoot(HuffmanNode root) {
        this.root = root;
        this.nodeCounter = 0; // Reset counter for new tree
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (root == null) {
            // Draw placeholder text if tree is empty
            g2d.setColor(Color.BLACK);
            g2d.drawString("empty tree", getWidth() / 2 - 30, getHeight() / 2);
        } else {
            nodeCounter = 0; // Reset counter before drawing
            drawTree(g2d, root, getWidth() / 2, 30, getWidth() / 4);
        }
    }

    private void drawTree(Graphics g, HuffmanNode node, int x, int y, int xOffset) {
        if (node == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw node as a rectangle
        g2d.setColor(Color.WHITE); // Node background color - can be changed here
        g2d.fillRect(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, NODE_WIDTH, NODE_HEIGHT);
        g2d.setColor(Color.BLACK); // Node border color - can be changed here
        g2d.drawRect(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, NODE_WIDTH, NODE_HEIGHT);

        // Determine label: character or "Nx" for internal nodes
        String label;
        if (node.left == null && node.right == null) {
            label = node.data + ":" + node.frequency;
        } else {
            nodeCounter++;
            label = "N" + nodeCounter + ":" + node.frequency;
        }
        g2d.setColor(Color.BLACK); // Node text color - can be changed here
        g2d.drawString(label, x - NODE_WIDTH / 3, y + 5);

        // Draw children
        if (node.left != null) {
            int leftX = x - xOffset;
            int leftY = y + LEVEL_HEIGHT;
            g2d.setColor(Color.BLACK); // Line color - can be changed here
            g2d.drawLine(x - NODE_WIDTH / 4, y + NODE_HEIGHT / 2, leftX + NODE_WIDTH / 4, leftY - NODE_HEIGHT / 2);
            g2d.drawString("0", x - xOffset / 2, y + LEVEL_HEIGHT / 2);
            drawTree(g2d, node.left, leftX, leftY, xOffset / 2);
        }
        if (node.right != null) {
            int rightX = x + xOffset;
            int rightY = y + LEVEL_HEIGHT;
            g2d.setColor(Color.BLACK); // Line color - can be changed here
            g2d.drawLine(x + NODE_WIDTH / 4, y + NODE_HEIGHT / 2, rightX - NODE_WIDTH / 4, rightY - NODE_HEIGHT / 2);
            g2d.drawString("1", x + xOffset / 2, y + LEVEL_HEIGHT / 2);
            drawTree(g2d, node.right, rightX, rightY, xOffset / 2);
        }
    }
}

class HuffmanNode {
    char data;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    public HuffmanNode(int frequency) {
        this.data = '\0';
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }
}

class HuffmanEncoder {
    private HuffmanNode root;
    private Map<Character, String> huffmanCodes;
    private Map<Character, Integer> frequencyMap;

    public HuffmanEncoder() {
        huffmanCodes = new HashMap<>();
        frequencyMap = new HashMap<>();
    }

    public void buildHuffmanTree(String text) {
        // Build frequency table
        frequencyMap.clear();
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Create min heap
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>((a, b) -> a.frequency - b.frequency);
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // Build Huffman Tree
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            pq.add(parent);
        }

        root = pq.poll();
        huffmanCodes.clear();
        generateCodes(root, "");
    }

    private void generateCodes(HuffmanNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.data, code);
            return;
        }
        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }

    public String encode(String text) {
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(huffmanCodes.get(c));
        }
        return encoded.toString();
    }

    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encoded.toCharArray()) {
            current = (bit == '0') ? current.left : current.right;
            if (current.left == null && current.right == null) {
                decoded.append(current.data);
                current = root;
            }
        }
        return decoded.toString();
    }

    public String getHuffmanCodesString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            sb.append("'").append(entry.getKey()).append("': ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public String getFrequencyTable() {
        StringBuilder sb = new StringBuilder("FREQUENCY TABLE:\n");
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            sb.append("'").append(entry.getKey()).append("': ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public HuffmanNode getRoot() {
        return root;
    }
}