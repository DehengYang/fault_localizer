/**
 * 
 */
package apr.apr.repair.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.Pair;

import static com.github.javaparser.GeneratedJavaParserConstants.MULTI_LINE_COMMENT;
import static com.github.javaparser.Providers.provider;
import static com.github.javaparser.utils.Utils.EOL;

/**
 * Refer to: 
 * Insert new nodes while traversing through AST using TreeVisitor #1029 -> https://github.com/javaparser/javaparser/issues/1029
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class TestChangeNode {
    public static void main(String[] args) {
        final BodyDeclaration<?> mainMethod = StaticJavaParser.parseBodyDeclaration("int main(){" + EOL +
                "          return 0;" + EOL +
                "     }");

        new TreeVisitor() {
            int count = 0;

            @Override
            public void process(Node node) {
                node.getTokenRange().ifPresent(r -> {
                    count++;
                    JavaToken startComment = new JavaToken(MULTI_LINE_COMMENT, "/* " + count + " [ */");
                    JavaToken endComment = new JavaToken(MULTI_LINE_COMMENT, "/* ] " + count + " */");
                    // ???
                    // Go to the beginning of the node in the token list, and insert the start comment there.
                    r.getBegin().insert(startComment);
                    // Go to the end of the node in the token list, and insert the end comment after it.
                    r.getEnd().insertAfter(endComment);
                    // Update the node's token range to include these comments.
                    // (Otherwise tokenRange.toString misses the first and last comment)
                    node.setTokenRange(r.withBegin(startComment).withEnd(endComment));
                });
            }
        }.visitPreOrder(mainMethod);

        mainMethod.getTokenRange().ifPresent(System.out::println);
    }
}