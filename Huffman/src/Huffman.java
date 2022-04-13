import java.io.*;
import java.util.*;

class Node {
    Node left, right;
    double value;
    String character;

    public Node(double value, String character) {
        this.value = value;
        this.character = character;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        character = left.character + right.character;
        if(left.value < right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }
}

public class Huffman {
    static PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    static TreeMap<Character, String> codes = new TreeMap<>();
    static String text = "";
    static String encoded = "";
    static String decoded = "";
    static int ASCII[] = new int[128];

    public static void main(String[] args) throws IOException {
        int SelectN = 0;
        while(SelectN != -1) {
            if(TextReader(SelectN))
                continue;
            SelectN = console();
        }
        System.out.println("허프만 코드 종료");
    }

    private static int console() throws IOException {
        int SelectN;
        Scanner sc = new Scanner(System.in);
        System.out.println(
                "프로그램 종료하려면 -1 입력 \n" +
                "인코딩 실행하려면 1 입력 \n" +
                "디코딩 실행하려면 2 입력 \n");
        SelectN = Integer.parseInt(sc.nextLine());

        return SelectN;
    }

    private static boolean TextReader(int SelectN) throws IOException {
        if(SelectN == 1) {
            BufferedReader readFile = new BufferedReader(new FileReader("./input.txt"));
            while(true) {
                String line = readFile.readLine();
                if(line == null) break;
                handleNew(line);
            }
            readFile.close();
        } else if(SelectN == 2) {
            BufferedReader readFile = new BufferedReader(new FileReader("./encoded_result.txt"));
            while(true) {
                String line = readFile.readLine();
                if(line == null) break;
                decodeText(line);
            }
            readFile.close();
        }

        return false;
    }

    private static void handleNew(String line) throws IOException {
        text = line;
        ASCII = new int[128];
        nodes.clear();
        codes.clear();
        encoded = "";
        decoded = "";
        calculateCharIntervals(nodes);
        GenerateTree(nodes);
        generateCodes(nodes.peek(), "");
        encodeText(text);
    }

    private static void calculateCharIntervals(PriorityQueue<Node> vector) {
        for(int i = 0; i < text.length(); i++) {
            ASCII[text.charAt(i)]++;
        }
        for(int i = 0; i < ASCII.length; i++) {
            if(ASCII[i] > 0) {
                vector.add(new Node(ASCII[i] / (text.length() * 1.0), ((char) i) + ""));
            }
        }
    }

    private static void GenerateTree(PriorityQueue<Node> vector) {
        while(vector.size() > 1)
            vector.add(new Node(vector.poll(), vector.poll()));
    }

    private static void generateCodes(Node node, String s) {
        if(node != null) {
            if(node.right != null)
                generateCodes(node.right, s + "1");
            if(node.left != null)
                generateCodes(node.left, s + "0");
            if(node.left == null && node.right == null)
                codes.put(node.character.charAt(0), s);
        }
    }

    private static void encodeText(String line) throws IOException {
        encoded = "";
        String encoded_split = "";
        for(int i = 0; i < text.length(); i++) {
            encoded += codes.get(text.charAt(i));
            encoded_split += codes.get(text.charAt(i)) + " ";
        }
        System.out.println("Encoded Text: " + encoded_split);
        OutputStream output = new FileOutputStream("./encoded_result.txt");
        byte[] by = encoded.getBytes();
        output.write(by);
    }

    private static void decodeText(String line) {
        decoded = "";
        Node node = nodes.peek();
        for(int i = 0; i<encoded.length();) {
            Node tmpNode = node;
            while(tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {
                if(encoded.charAt(i) == '1') {
                    tmpNode = tmpNode.right;
                } else {
                    tmpNode = tmpNode.left;
                }
                i++;
            }
            if(tmpNode != null) {
                if(tmpNode.character.length() == 1) {
                    decoded += tmpNode.character;
                }
                else {
                    System.out.println("Input not Valid");
                }
            }
        }
        System.out.println("Decoded Text: " + decoded);
    }
}

