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
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

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

    public static Boolean isClosingSessionsProperly(CompilationUnit cu) {
        Boolean containsError = false;
        for (TypeDeclaration type : cu.getTypes()) {
            for (BodyDeclaration member : type.getMembers()) {
                if (member instanceof ConstructorDeclaration || member instanceof MethodDeclaration || member instanceof FieldDeclaration) {
                    if (member instanceof FieldDeclaration && opensHibernateSession(member.toString())) {
                        System.out.println("You are openning Sessions outside any method call! That is bad programming!");
                        containsError = true;
                        continue;
                    } else if (member instanceof FieldDeclaration) {
                        continue;
                    }
                    if (opensHibernateSession(member.toString())) {
                        if (!hasTryAndFinallyInTheCode(member.toString())) {
                            String methodName = "";
                            if (member instanceof MethodDeclaration) {
                                MethodDeclaration method = (MethodDeclaration) member;
                                methodName = method.getName();
                            } else {
                                methodName = "constructor";
                            }
                            System.out.println("The " + methodName + " method opens sessions but doesn't work with it in a try and finally block");
                            containsError = true;
                        } else {
                            BlockStmt body = member instanceof MethodDeclaration ? ((MethodDeclaration) member).getBody() : ((ConstructorDeclaration) member).getBlock();
                            containsError = !isHandlingSessionsProperly(body);
                        }

                    }
                }
            }
        }

        return !containsError;
    }

    private static Boolean opensHibernateSession(String methodBody) {
        return methodBody.contains("HibernateUtil.getSessionFactory().openSession()");
    }

    private static Boolean isHandlingSessionsProperly(BlockStmt body) {

        // for each block of code, if you are using a session, it must be in a try block and closing it in the following finally block!
        LinkedList<Node> blocks = new LinkedList<>();

        for (Statement statement : body.getStmts()) {
            for (Node child : body.getChildrenNodes()) {
                blocks.addLast(child);
            }
        }

        while (!blocks.isEmpty()) {
            Node current = blocks.removeFirst();

            if (consumesSession(current.toStringWithoutComments()) && !isUsingSessionsProperly(current)){
                System.out.println("You are using a session variable outside a try-finally block in the following block of code : ");
                System.out.println(current.toString());
            }
//            if (current instanceof TryStmt) {
//                TryStmt tryBlock = (TryStmt) current;
//                if (consumesSession(tryBlock.toStringWithoutComments())) {
//                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//                    System.out.println(tryBlock.toStringWithoutComments());
//                }
//            }
            for (Node child : current.getChildrenNodes()) {
                blocks.addLast(child);
            }
        }

        return true;
    }

    private static boolean hasTryAndFinallyInTheCode(String methodBody) {
        return methodBody.contains("try") && methodBody.contains("finally");
    }

    private static boolean consumesSession(String block) {
        return block.contains(".sessionWithOptions(") || block.contains(".flush()") || block.contains(".setFlushMode(") || block.contains(".getFlushMode()") || block.contains(".setCacheMode(")
                || block.contains(".getCacheMode()") || block.contains(".getSessionFactory()") || block.contains(".close()") || block.contains(".cancelQuery()") || block.contains(".isOpen()")
                || block.contains(".isConnected()") || block.contains(".isDirty()") || block.contains(".setDefaultReadOnly(") || block.contains(".isDefaultReadOnly()") || block.contains(".getIdentifier(")
                || block.contains(".evict(") || block.contains(".load(") || block.contains(".replicate(") || block.contains(".save(") || block.contains(".saveOrUpdate(") || block.contains(".update(")
                || block.contains(".merge(") || block.contains(".persist(") || block.contains(".delete(") || block.contains(".lock(") || block.contains(".buildLockRequest(") || block.contains(".refresh(")
                || block.contains(".getCurrentLockMode(") || block.contains(".createFilter(") || block.contains(".clear()") || block.contains(".getEntityName(") || block.contains(".byId(") || block.contains(".byNaturalId(")
                || block.contains(".bySimpleNaturalId(") || block.contains(".enableFilter(") || block.contains(".getEnabledFilter(") || block.contains(".disableFilter(") || block.contains(".getStatistics()")
                || block.contains(".isReadOnly(") || block.contains(".setReadOnly(") || block.contains(".doWork(") || block.contains(".doReturningWork(") || block.contains(".disconnect()") || block.contains(".reconnect(")
                || block.contains(".isFetchProfileEnabled(") || block.contains(".enableFetchProfile(") || block.contains(".disableFetchProfile(") || block.contains(".getTypeHelper()") || block.contains(".getLobHelper()");
    }

    private static boolean isUsingSessionsProperly(Node block) {
        // we here check that the parent node must be a try type
        while (block.getParentNode() != null && !(block.getParentNode() instanceof BodyDeclaration)){
            block = block.getParentNode();
            System.out.println("@@@@@@@@@@@@@@@@@@@@");
            System.out.println(block.toString());
        }
        //then we check that the session is closed properly here in the finnaly
        
        return true;
    }

}
