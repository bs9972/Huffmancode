# 허프만 코드(Huffman code) 성능 측정 보고서

> **컴퓨터 알고리즘, 김동훈 교수님, 8조**
>
> > 코드 구현: bs9972(신범식)  
> > 성능 측정: hankom(한경환)

## 허프만 코드란

허프만 코딩(Huffman coding)은 대부분의 압축 프로그램에서 사용하는 방법으로, 자주 사용되는 문자는 적은 비트로 된 코드로 변환해서 표현하고, 별로 사용되지 않는 문자는 많은 비트로 된 코드로 변환하여 표현함으로써 전체 데이터를 표현하는 데 필요한 비트의 양을 줄이는 방법이다.

## 압축 과정

- 1. 데이터에서 사용되는 각 문자에 대한 출현 빈도수를 구한다.
- 2. 빈도수를 기준으로 내림차순으로 정렬한다.
- 3. 출현 빈도가 가장 적은 2개의 문자를 가지로 연결하고, 가지 위에 두 문자의 빈도수의 합을 적는다. `(Node Class)`
- 4. 적어놓은 빈도수의 합을 기준으로 재배열한다.
- 5. 이 과정을 반복한다.
- 6. 더 이상 연결할 수 없으면 동작을 종료한다.

---

## 코드 구현 + 설명
```java
import java.io.*;
import java.util.*;

//노드 생성 빈도수,문자,왼쪽,오른쪽 노드 생성
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
    static String text = "";//string text 초기화
    static String encoded = "";//인코딩 문자열 초기화
    static String decoded = "";//디코딩 문자열 초기화
    static int ASCII[] = new int[128];// ascii 코드는 7비트 이므로 128 크기의 배열 초기화
//메인
    public static void main(String[] args) throws IOException {
        int SelectN = 0;//selectn 초기화
        while(SelectN != -1) {
            if(TextReader(SelectN))
                continue;
            SelectN = console();
        }
        System.out.println("허프만 코드 종료");
    }
//화면에 입출력할 것
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
//인코딩 파일 디코딩 파일 받고 인코딩 디코딩
    private static boolean TextReader(int SelectN) throws IOException {
        if(SelectN == 1) {
            BufferedReader readFile = new BufferedReader(new FileReader("./input.txt"));//
            while(true) {
                String line = readFile.readLine();//txt로 받은 문자열 line 에저장
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
//txt파일 처리
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
//트리 생성
    private static void GenerateTree(PriorityQueue<Node> vector) {
        while(vector.size() > 1)
            vector.add(new Node(vector.poll(), vector.poll()));
    }
// left를 탐색할 경우 0을, right를 탐색할 경우 1을 문자열에 추가
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
//인코딩
    private static void encodeText(String line) throws IOException {
        encoded = "";
        String encoded_split = "";
        //문자마다 인코딩 
        for(int i = 0; i < text.length(); i++) {
            encoded += codes.get(text.charAt(i));
            encoded_split += codes.get(text.charAt(i)) + " ";
        }
        System.out.println("Encoded Text: " + encoded_split);
        OutputStream output = new FileOutputStream("./encoded_result.txt");//txt로 암호화된 문자열 출력
        byte[] by = encoded.getBytes();
        output.write(by);
    }
//디코딩 
    private static void decodeText(String line) {
        decoded = "";//문자열 초기화
        Node node = nodes.peek();
        for(int i = 0; i<encoded.length();) {
            Node tmpNode = node;
            while(tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {
                //암호화된 string 문자열을 char 문자타입으로 변환한 값이 1이면 오른쪽 노드값을 0이면 왼쪽 노드값을
                switch(encoded.charAt(i)){
                    case '1':
                        tmpNode = tmpNode.right;
                        break;
                    case '0':
                        tmpNode = tmpNode.left;
                        break; 
                }
                i++;
            }
            //노드가 있을때
            if(tmpNode != null) {
                //노드 있을때
                if(tmpNode.character.length() == 1) {
                    //디코드 문자열에 해당 암호화된 문자 입력
                    decoded += tmpNode.character;
                }
                //노드 없을때
                else {
                    System.out.println("Input not Valid");
                }
            }
        }
        System.out.println("Decoded Text: " + decoded);
    }
}
```

### 작동 원리

상위 폴더에 input.txt와 encoded_result.txt를 생성하고 input.txt에 부호화(encode)할 텍스트 내용을 입력한다. 여기서 입력한 텍스트는 다음과 같다.

```
ABCACBCDCBCACDCACDDD
```

A: 4개, B: 3개, C: 8개, D: 5개,
`1` 을 입력하여 부호화를 실행한다.

```
Encoded Text: 111 110 0 111 0 110 0 10 0 110 0 111 0 10 0 111 0 10 10 10 
```

부호화 과정이 끝나고 encoded_result.txt에 결과값이 저장된다.  
저장된 값은 다음과 같다.

```
111110011101100100110011101001110101010
```

테스트를 위해 부호화된 텍스트를 다시 복호화(decode) 해보면

```
Decoded Text: ABCACBCDCBCACDCACDDD
```

input.txt에 입력한 텍스트와 같음을 알 수 있다.

---

#### 트리
             [20]
               │
            0┌───┐1
         [C:8]   [12] 
                   │
                0┌───┐1
             [D:5]   [7]  
                      │
                    0┌───┐1
                 [B:3]   [A:4]

## 성능 측정

허프만 코드는 통계적으로 문자 출현 빈도가 높은 문자일수록 짧은 부호로 변환하여 데이터를 압축한다. 따라서 허프만 코드의 성능을 압축률을 기준으로 평가하고자 한다.

### 입력 텍스트

테스트할 텍스트는 다음과 같다.

```
I'm on the next level
I: 1, ': 1, m: 1, o: 1, n: 2, t: 2, h: 1, e: 4, x: 1, l: 2, v: 1.
```

```
XXXXAAACVVVEEEEEGGFGGGFAAS
X: 4, A: 5, C: 1, V: 3, E: 5, G: 5, F: 2, S: 1
```

### 출력값과 용량 비교

ASCII의 한 글자의 용량은 1 Byte(=8 bits)이다.  
첫번째 텍스트의 부호화 전 용량은 `17 Bytes(=136 bits)`.

첫번째 텍스트의 결과값에 따른 허프만 트리는 다음과 같다.

```
                            [17 + X]
                0             │                 1
                ┌───────────────────────────────┐
               [8]                       [nexmt + X: 9 + X]
          0     │       1               0       │         1
          ┌─────────────┐               ┌─────────────────┐
         [4]         [space:4]         [X]             [exmt:7]
     0    │      1                 0    │      1    0     │     1
     ┌───────────┐                 ┌───────────┐    ┌───────────┐
    ['I:2]     ['I:2]             [n:2]       [X] [e:4]      [xmt:3]
  0  │   1    0  │   1                                     0    │    1
   ┌──────┐   ┌──────┐                                     ┌─────────┐
  [v:1] [h:1][I:1]  [':1]                                [xm:2]    [t:1]
                                                      0    │    1
                                                      ┌─────────┐
                                                    [x:1]     [m:1]

```

이론적으로 구상한 허프만 트리와 달리 결과값에 따른 허프만 트리는 노드의 빈 공간이 생겨
총 빈도수의 합은 `17 + X`이 나왔다. 이론적인 총 빈도수의 합과 `4`의 차이가 발생했다.


```
001000111110101101010001111100011100110011011100111101101111000001101011
```

부호화 후 용량은 `72 bits`로 64 bits가 감소한 것을 알 수 있다.

두번째 텍스트의 부호화 전 용량은 `26 Bytes(=208 bits)`.

```
11011011011001010110101100100100111111111111111000010110000001011010110100
```

부호화 후 용량은 `74 bits`로 134 bits가 감소하여 더 높은 압축률을 보여준다.

# 결론

허프만 알고리즘을 통해 주어진 텍스트를 부호화하고, 복호화하는데 오류 없이 작동함을 확인할 수 있었다. 하지만 부호화 과정에서 빈도수가 같은 문자들의 수가 늘어날 경우 노드의 빈 공간이 생겨 효율성을 떨어뜨린다. 부호화 과정을 반복하며 여러 조건에 있는 텍스트들의 압축률을 비교해본 결과, 압축률에 제일 영향을 끼치는 요인은 `문자의 빈도수`이다. 문자들의 빈도수가 같은 상황일때를 대비한 특수한 알고리즘을 덧붙이면 더 좋은 압축률을 보여줄 수 있을거라 생각된다. 추가적으로 허프만 트리 제작의 시간 복잡도에 관한 사실로 구글링해 조사해본 결과 최소 값 추출에 logN의 시간을 소모하고, N개의 원소를 이용해 트리를 만들기 때문에 O(NlogN)의 시간복잡도를 가진다.
