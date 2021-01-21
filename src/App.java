import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.io.FileNotFoundException;

public class App {

    static HashMap<String, Rule> grammar = new HashMap<String, Rule>();
    static Stack<Character> inputS = new Stack<Character>();
    static Stack<CString> parser = new Stack<CString>();
    static Stack<ArrayList<CString>> children = new Stack<ArrayList<CString>>();
    static CString historyParent = new CString();
    static int expandCtr = 0;
    static char lexerValue;
    static char inputValue;
    static String offendingString;
    static boolean isAccepted = true;
    static boolean isKleenePlus = false;
    static boolean isRequired = true;
    static boolean canPop = true;
    static String FINAL_STRING = "";

    public static void main(String[] args) throws Exception{
        File output = new File("src/txtFiles/output.txt");
        FileWriter fw = new FileWriter(output);

        parser.push(new CString("start"));
        
        try{
            File grammar = new File("src/txtFiles/grammar.txt");
            File input = new File("src/txtFiles/input.txt");
            FileReader fr = new FileReader(input);
            BufferedReader reader = new BufferedReader(fr);
            String line;
            inputGrammar(grammar);

            while ((line = reader.readLine()) != null){ //iterate per line in input text
                if(!line.equals("")){
                    FINAL_STRING += line;
                    line = line.replaceAll("\\s","");
                    char[] chars = line.toCharArray();
                    
                    for(int x = chars.length-1; x >= 0; x--){
                        inputS.push(chars[x]);
                    }
                    
                    parse();
                    reset();
                }

                FINAL_STRING += '\n';
            }
            reader.close();
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            e.printStackTrace();
        }

        fw.write(FINAL_STRING);
        fw.close();
        
    }

    public static void inputGrammar(File grammarFile) throws Exception{

        FileReader fr = new FileReader(grammarFile);
        BufferedReader reader = new BufferedReader(fr);
        String line;

        while ((line = reader.readLine()) != null){
            if(!line.equals("")){
                String clean = line.replace(";", "");
                String[] texts = clean.split(":");
                String leftRule = texts[0];
                leftRule = leftRule.trim();
                    
                String[] rightRules = texts[1].split("\\|");

                if(Character.isUpperCase(leftRule.charAt(0))){
                    rightRules[0] = rightRules[0].replace("'", "");
                    grammar.put(leftRule, new Rule(leftRule, rightRules));
                }else{
                    grammar.put(leftRule, new Rule(leftRule, rightRules));
                }
            }
        }

        reader.close();
        //grammar.forEach((k, v) -> System.out.println(k + " : " + v.getGrammar()));
    }

    public static void parse(){        
        String top = "";
            
        while(!inputS.isEmpty()){
            if(parser.isEmpty()){
                isAccepted = false;
                break;
            }else{
                top = parser.peek().Name;
            }
            
            // System.out.println(top);
            if(!Character.isUpperCase(top.charAt(0))){ // check if lowercase
                expand();
            }else{ // if uppercase
                if(grammar.get(parser.peek().Name).RHS.get(0).get(0).Name.equals(inputS.peek().toString())){ // MATCH
                    // System.out.println(historyParent.Name);
                    resetHistory();
                    inputS.pop();
                    parser.pop();
                    if(isKleenePlus){
                        isRequired = false;
                    }
                }else{
                    performBacktrack();
                }

            }
            // for(int y = 0; y < parser.size(); y++){
            //     System.out.print(parser.get(y).Name);
            //     System.out.println(",");
            // }
            // // System.out.println(parser.peek().Name);
            // System.out.println(inputS);
            if(!isAccepted){
                break;
            }
        }

        int temp_size = parser.size();

        // for(int y = 0; y < parser.size(); y++){
        //     System.out.print(parser.get(y).Name);
        //     System.out.println(",");
        // }
        // System.out.println(grammar.get(parser.peek().Name).LHS.Kind);

        for(int x = 0; x < temp_size; x++){
            // System.out.println(parser.peek().Name);
            if(isKleenePlus && !isRequired){
                isKleenePlus = false;
                isRequired = true;
                parser.pop();
            }else if(grammar.get(parser.peek().Name).LHS.Kind.equals("epsilon")){
                parser.pop();
            }
        }
        
        if(inputS.isEmpty() && parser.isEmpty()){
            FINAL_STRING += " - ACCEPT";
        }else{
            if(!inputS.isEmpty()){
                FINAL_STRING += " - REJECT. Offending Token '" + inputS.peek() + "'";
            }else{
                // System.out.println(inputS);
                FINAL_STRING += " - REJECT. Missing Token '" + grammar.get(parser.peek().Name).RHS.get(0).get(0).Name + "'";
            }
        }
    }

    public static void performBacktrack(){
        // System.out.println(parser);
        for(int x = 0; x < expandCtr; x++){
            parser.pop();
        }
        expandCtr = 0;

        if(!children.empty()){
            for(int x = children.peek().size()-1; x >= 0; x--){
                parser.push(children.peek().get(x));
                expandCtr++;
            }
    
            children.pop();
        }else{
            if(historyParent == null){
                isAccepted = false;
            }else{
                if(isRequired && isKleenePlus){
                    isAccepted = false;
                    parser.pop();
                    // System.out.println("Test");
                }else if(isKleenePlus && !isRequired){
                    canPop = true;
                    isKleenePlus = false;
                    isRequired = true;
                    resetHistory();
                    parser.pop();
                }else if(grammar.get(historyParent.Name).LHS.Kind.equals("normal")){
                    // System.out.println(grammar.get(historyParent.Name).LHS.Name);
                    isAccepted = false;
                }else if(grammar.get(historyParent.Name).LHS.Kind.equals("epsilon")){
                    historyParent = null;
                }
            }
        }
    }

    public static void expand(){
        String top = parser.peek().Name;
        CString cTop = parser.peek();
        //System.out.println(cTop.Name);

        if(cTop.Kind.equals("kleeneplus")){
            // System.out.println(cTop.Name);
            isKleenePlus = true;
            canPop = false;
        }

        if(canPop){
            parser.pop();
            // System.out.println(parser);
        }
        
        expandCtr = 0;

        if(grammar.get(top).RHS.size() > 1){
            historyParent = cTop;
            for(int x = grammar.get(top).RHS.size()-1; x >= 1 ; x--){ //check if grammar has more than one rule
                children.push(grammar.get(top).RHS.get(x));
            }
        }

        for(int x = grammar.get(top).RHS.get(0).size()-1; x >= 0; x--){ //expand the parser top
            parser.push(grammar.get(top).RHS.get(0).get(x));
            expandCtr++;
        }
    }

    public static void resetHistory(){
        historyParent = new CString();
        children.clear();
        expandCtr = 0;
    }

    public static void reset(){
        inputS.clear();
        parser.clear();
        parser.push(new CString("start"));
        canPop = true;
        isRequired = true;
        historyParent = new CString();
        children.clear();
        expandCtr = 0;
        isAccepted = true;
    }
}
