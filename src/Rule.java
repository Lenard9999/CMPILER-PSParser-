import java.util.*;

public class Rule {

    CString LHS;
    ArrayList<ArrayList<CString>> RHS = new ArrayList<ArrayList<CString>>();
    boolean isEpsilon = false;
    boolean isMany = false;
    boolean isKleene = false;

    public Rule() {
    }

    public Rule(String left, String[] right){
        for(String x : right){

            if(right.length > 1){
                isMany = true;
            }

            if(x.equals("   ")){
                isEpsilon = true;
            }else{
                x = x.trim();
                String[] iRule = x.split(" ");
    
                ArrayList<CString> temp = new ArrayList<>();
    
                for(String y : iRule){
                    if(y.length() == 1)
                        temp.add(new CString(y));
                    else if(y.charAt(y.length()-1) == '+' && y.length() != 1)
                        temp.add(new CString(y.replace("+", ""), "kleeneplus"));       
                    else
                        temp.add(new CString(y));
                }
                RHS.add(temp);
            }
        }
        
        if(isEpsilon){
            LHS = new CString(left, "epsilon");
        }else if (isKleene){
            LHS = new CString(left, "kleeneplus");
        }else{
            LHS = new CString(left);
        }
        
    }

    public String getGrammar(){
        String printVal = "";
        for (ArrayList<CString> x : RHS) { 
            printVal += " ";
            for(CString y : x)
                printVal += y.Name;
        }
        return printVal;
    }

}