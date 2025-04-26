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
    private JTextArea inputTextArea, outputTextArea, freqTableArea;
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

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputTextArea = new JTextArea(5, 30);
        inputTextArea.setLineWrap(true);
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        JButton encodeButton = new JButton("Encode");
        encodeButton.addActionListener(new EncodeButtonListener());
        inputPanel.add(encodeButton, BorderLayout.SOUTH);
        add(inputPanel, BorderLayout.NORTH);

        // Center Panel (split into frequency table and tree)
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Frequency Table Area
        freqTableArea = new JTextArea(10, 10);
        freqTableArea.setEditable(false);
        centerPanel.add(new JScrollPane(freqTableArea), BorderLayout.WEST);

        // Tree Panel
        treePanel = new HuffmanTreePanel();
        centerPanel.add(treePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Output Panel
        outputTextArea = new JTextArea(10, 20);
        outputTextArea.setEditable(false);
        outputTextArea.setLineWrap(true);
        add(new JScrollPane(outputTextArea), BorderLayout.EAST);

        // Decode Panel
        JPanel decodePanel = new JPanel(new BorderLayout());
        decodeInputField = new JTextField(30);
        JButton decodeButton = new JButton("Decode");
        decodeButton.addActionListener(new DecodeButtonListener());
        decodePanel.add(decodeInputField, BorderLayout.CENTER);
        decodePanel.add(decodeButton, BorderLayout.EAST);
        add(decodePanel, BorderLayout.SOUTH);
    }

    private class EncodeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputTextArea.getText();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(HuffmanFrame.this, "Please enter text to encode.");
                return;
            }

            encoder.buildHuffmanTree(input);
            String encoded = encoder.encode(input);
            outputTextArea.setText("Encoded: " + encoded + "\n\nHuffman Codes:\n" + encoder.getHuffmanCodesString());
            freqTableArea.setText(encoder.getFrequencyTable());
            treePanel.setRoot(encoder.getRoot());
            treePanel.repaint();
        }
    }

    private class DecodeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String code = decodeInputField.getText();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(HuffmanFrame.this, "Please enter binary code to decode.");
                return;
            }

            String decoded = encoder.decode(code);
            outputTextArea.append("\n\nDecoded: " + decoded);
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
        if (root != null) {
            nodeCounter = 0; // Reset counter before drawing
            drawTree(g, root, getWidth() / 2, 30, getWidth() / 4);
        }
    }

    private void drawTree(Graphics g, HuffmanNode node, int x, int y, int xOffset) {
        if (node == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw node as a rectangle
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, NODE_WIDTH, NODE_HEIGHT);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x - NODE_WIDTH / 2, y - NODE_HEIGHT / 2, NODE_WIDTH, NODE_HEIGHT);

        // Determine label: character or "Nx" for internal nodes
        String label;
        if (node.left == null && node.right == null) {
            label = node.data + ":" + node.frequency;
        } else {
            nodeCounter++;
            label = "N" + nodeCounter + ":" + node.frequency;
        }
        g2d.drawString(label, x - NODE_WIDTH / 3, y + 5);

        // Draw children
        if (node.left != null) {
            int leftX = x - xOffset;
            int leftY = y + LEVEL_HEIGHT;
            g2d.drawLine(x - NODE_WIDTH / 4, y + NODE_HEIGHT / 2, leftX + NODE_WIDTH / 4, leftY - NODE_HEIGHT / 2);
            g2d.drawString("0", x - xOffset / 2, y + LEVEL_HEIGHT / 2);
            drawTree(g2d, node.left, leftX, leftY, xOffset / 2);
        }
        if (node.right != null) {
            int rightX = x + xOffset;
            int rightY = y + LEVEL_HEIGHT;
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
        StringBuilder sb = new StringBuilder("Frequency Table:\n");
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            sb.append("'").append(entry.getKey()).append("': ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public HuffmanNode getRoot() {
        return root;
    }
}