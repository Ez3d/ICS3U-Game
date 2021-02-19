//import java.util.*;
//
//public class Test {
//
//    public static void main(String[] args) {
//        Scanner in = new Scanner(System.in);
//        String input = in.nextLine();
//        String inputNew = "";
//        int num = Integer.parseInt(in.nextLine());
//        String condition;
//        int[] conditions = new int[2];
//
//        for (int i = 0; i < num; i++) {
//            condition = in.nextLine();
//            if (condition.equals("confusion"))
//                conditions[i] = 0;
//            else if (condition.equals("bad hearing 1"))
//                conditions[i] = 1;
//            else if (condition.equals("bad hearing 2"))
//                conditions[i] = 2;
//            else if (condition.equals("bad hearing 3"))
//                conditions[i] = 3;
//            else if (condition.equals("half deaf"))
//                conditions[i] = 4;
//        }
//
//        int spaceCount = 0;
//        for (int i = 0; i < num; i ++)
//            if (conditions[i] == 0) {
//                inputNew = input.substring(input.indexOf(' ')) + " " + input.substring(0, input.indexOf(' '));
//                input = inputNew;
//            } else if (conditions[i] == 1) {
//
//            } else if (conditions[i] == 2) {
//            } else if (conditions[i] == 3) {
//            } else if (conditions[i] == 4) {
//                for (int j = 0; j < input.length(); j++) {
//                    if (input.charAt(j) == ' ')
//                        spaceCount++;
//                }
//                inputNew = input.substring(0, );
//            }
//    }
//}