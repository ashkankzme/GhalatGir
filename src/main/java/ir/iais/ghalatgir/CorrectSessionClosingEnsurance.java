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
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
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
        //FileInputStream in = new FileInputStream("/media/ashkan/B/pazhm/GhalatGir/GhalatGir/src/main/java/ir/iais/ghalatgir/HsModifyPanel.txt");
        for (String arg : args) {
            FileInputStream in = new FileInputStream(arg);

            CompilationUnit cu;
            try {
                // parse the file
                cu = JavaParser.parse(in);
            } finally {
                in.close();
            }

            // prints the resulting compilation unit to default system output
            if (isClosingSessionsProperly(cu)) {
                System.out.println("no problems detected with the file : "+arg);
            }
        }
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
                            Boolean handlingSessionsProperlyResult = isHandlingSessionsProperly(body);
                            if (!containsError) {
                                containsError = !handlingSessionsProperlyResult;
                            }
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
                if (!blocks.contains(child) && consumesSession(child.toStringWithoutComments())) {
                    blocks.addLast(child);
                }
            }
        }

        for (Node block : blocks) {
            System.out.println(block.toStringWithoutComments());
        }

        while (!blocks.isEmpty()) {
            Node current = blocks.removeFirst();

            if (current.getChildrenNodes().isEmpty() || (current instanceof MethodCallExpr)) {
                if (!isInsideATryBlock(current) || (isClosingSession(current.toStringWithoutComments()) && !isClosingSessionInsideFinally(current))) {// see about that finally later :*
                    return false;
                }
            }

            for (Node child : current.getChildrenNodes()) {
                if (!blocks.contains(child) && consumesSession(child.toStringWithoutComments())) {
                    blocks.addLast(child);
                }
            }
        }

        return true;
    }

    private static boolean hasTryAndFinallyInTheCode(String methodBody) {
        return methodBody.contains("try") && methodBody.contains("finally");
    }

    private static boolean consumesSession(String block) {
        return block.contains(".sessionWithOptions(") || block.contains(".flush()") || block.contains(".setFlushMode(") || block.contains(".getFlushMode()") || block.contains(".setCacheMode(")
                || block.contains(".getCacheMode()") /*|| block.contains(".getSessionFactory()")*/ || block.contains(".close()") || block.contains(".cancelQuery()") || block.contains(".isOpen()")
                || block.contains(".isConnected()") || block.contains(".isDirty()") || block.contains(".setDefaultReadOnly(") || block.contains(".isDefaultReadOnly()") || block.contains(".getIdentifier(")
                || block.contains(".evict(") || block.contains(".load(") || block.contains(".replicate(") || block.contains(".save(") || block.contains(".saveOrUpdate(") || block.contains(".update(")
                || block.contains(".merge(") || block.contains(".persist(") || block.contains(".delete(") || block.contains(".lock(") || block.contains(".buildLockRequest(") || block.contains(".refresh(")
                || block.contains(".getCurrentLockMode(") || block.contains(".createFilter(") || block.contains(".clear()") || block.contains(".getEntityName(") || block.contains(".byId(") || block.contains(".byNaturalId(")
                || block.contains(".bySimpleNaturalId(") || block.contains(".enableFilter(") || block.contains(".getEnabledFilter(") || block.contains(".disableFilter(") || block.contains(".getStatistics()")
                || block.contains(".isReadOnly(") || block.contains(".setReadOnly(") || block.contains(".doWork(") || block.contains(".doReturningWork(") || block.contains(".disconnect()") || block.contains(".reconnect(")
                || block.contains(".isFetchProfileEnabled(") || block.contains(".enableFetchProfile(") || block.contains(".disableFetchProfile(") || block.contains(".getTypeHelper()") || block.contains(".getLobHelper()")
                || block.contains(".connection()") || block.contains(".getEntityMode()") || block.contains(".getSession(") || block.contains(".beginTransaction()") || block.contains(".getTransaction()")
                || block.contains(".createCriteria(") || block.contains(".createQuery(") || block.contains(".createSQLQuery(") || block.contains(".getNamedQuery(");
    }

    private static boolean consumesSessionWithoutClosingIt(String block) {
        return block.contains(".sessionWithOptions(") || block.contains(".flush()") || block.contains(".setFlushMode(") || block.contains(".getFlushMode()") || block.contains(".setCacheMode(")
                || block.contains(".getCacheMode()") || block.contains(".cancelQuery()") || block.contains(".isConnected()") || block.contains(".createQuery(") || block.contains(".createSQLQuery(")
                || block.contains(".isDirty()") || block.contains(".setDefaultReadOnly(") || block.contains(".isDefaultReadOnly()") || block.contains(".getIdentifier(") || block.contains(".getNamedQuery(")
                || block.contains(".evict(") || block.contains(".load(") || block.contains(".replicate(") || block.contains(".save(") || block.contains(".saveOrUpdate(") || block.contains(".update(")
                || block.contains(".merge(") || block.contains(".persist(") || block.contains(".delete(") || block.contains(".lock(") || block.contains(".buildLockRequest(") || block.contains(".refresh(")
                || block.contains(".getCurrentLockMode(") || block.contains(".createFilter(") || block.contains(".getEntityName(") || block.contains(".byId(") || block.contains(".byNaturalId(")
                || block.contains(".bySimpleNaturalId(") || block.contains(".enableFilter(") || block.contains(".getEnabledFilter(") || block.contains(".disableFilter(") || block.contains(".getStatistics()")
                || block.contains(".isReadOnly(") || block.contains(".setReadOnly(") || block.contains(".doWork(") || block.contains(".doReturningWork(") || block.contains(".disconnect()") || block.contains(".reconnect(")
                || block.contains(".isFetchProfileEnabled(") || block.contains(".enableFetchProfile(") || block.contains(".disableFetchProfile(") || block.contains(".getTypeHelper()") || block.contains(".getLobHelper()")
                || block.contains(".connection()") || block.contains(".getEntityMode()") || block.contains(".getSession(") || block.contains(".beginTransaction()") || block.contains(".getTransaction()")
                || block.contains(".createCriteria(");
    }

    private static boolean isInsideATryBlock(Node block) {
        // we here check that the parent node must be a try type
        while (block.getParentNode() != null && !(block.getParentNode() instanceof BodyDeclaration)) {
            if (block instanceof TryStmt /*|| block.toStringWithoutComments().startsWith("finally")*/) {
                return true;
            }
            block = block.getParentNode();
        }
        //then we check that the session is closed properly here in the finnaly
        return false;
    }

    private static boolean isClosingSession(String block) {
        return block.contains(".clear()") || block.contains(".close()");
    }

    private static boolean isClosingSessionInsideFinally(Node block) {
        while (block.getParentNode() != null && !(block.getParentNode() instanceof BodyDeclaration)) {
            if (block instanceof TryStmt /*|| block.toStringWithoutComments().startsWith("finally")*/) {
                break;
            }
            block = block.getParentNode();
        }

        if (block instanceof TryStmt) {
            TryStmt tryBlock = (TryStmt) block;
            if (tryBlock.getFinallyBlock() == null) {
                return false;
            }
            String finallyAsString = tryBlock.getFinallyBlock().toStringWithoutComments();
            finallyAsString = finallyAsString.replace("\n", "");
            return finallyAsString.matches("(.*)if(.*)\\Q!=\\E(.*)null(.*)\\Q&&\\E(.*)\\Q.isOpen()\\E(.*)\\Q.clear()\\E(.*)\\Q.close()\\E(.*)")
                    && !consumesSessionWithoutClosingIt(finallyAsString);
        } else {
            return false;
        }
    }

}
