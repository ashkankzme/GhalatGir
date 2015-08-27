/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.iais.ghalatgir;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author ashkan
 */
public class CorrectSessionClosingEnsurance {

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        FileInputStream in = new FileInputStream("/media/ashkan/B/pazhm/GhalatGir/GhalatGir/src/main/java/ir/iais/ghalatgir/HsModifyPanel.txt");

        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        // prints the resulting compilation unit to default system output
        isClosingSessionsProperly(cu);
        //System.out.println(cu.toString());
    }
    
    public static Boolean isClosingSessionsProperly(CompilationUnit cu){
        Boolean containsError = false;
        for (TypeDeclaration type : cu.getTypes()){
            for (BodyDeclaration member : type.getMembers()){
                if (member instanceof ConstructorDeclaration || member instanceof MethodDeclaration || member instanceof FieldDeclaration){
                    if (member instanceof FieldDeclaration && consumesHibernateSession(member.toString())){
                        System.out.println("You are openning Sessions outside any method call! That is bad programming!");
                        containsError = true;
                        continue;
                    }
                    else if (member instanceof FieldDeclaration){
                        continue;
                    }
                    if (consumesHibernateSession(member.toString())){
                        if (!hasTryAndFinallyInTheCode(member.toString())){
                            String methodName = "";
                            if (member instanceof MethodDeclaration){
                                MethodDeclaration method = (MethodDeclaration) member;
                                methodName = method.getName();
                            }
                            else{
                                methodName = "constructor";
                            }
                            System.out.println("The "+methodName+" method opens sessions but doesn't work with it in a try and finally block");
                            containsError = true;
                        }
                        else {
                            BlockStmt body = member instanceof MethodDeclaration ? ((MethodDeclaration) member).getBody() : ((ConstructorDeclaration) member).getBlock();
                            containsError = !isHandlingSessionsProperly(body);
                        }
                        
                    }
                }
            }
        }
        
        return !containsError;
    }
    
    private static Boolean consumesHibernateSession(String methodBody){
        return methodBody.contains("HibernateUtil.getSessionFactory().openSession()");
    }

    private static Boolean isHandlingSessionsProperly(BlockStmt body) {
        for (Statement statement : body.getStmts()){
            for (Node child : body.getChildrenNodes()){
                System.out.println(child.toString());
                System.out.println("##########################################");
            }
        }
        
        return true;
    }

    private static boolean hasTryAndFinallyInTheCode(String methodBody) {
        return methodBody.contains("try") && methodBody.contains("finally");
    }

}
