/**
 * 
 */
package apr.apr.repair.parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/** visit all stmts
 * 
 * refer to: file:///E:/MyResearchLib/refs/javaparser-core-3.15.15-javadoc/index.html
 * 
 * @author apr
 * @version Mar 20, 2020
 *
 */
public class AttemptStmtVisitor extends VoidVisitorAdapter <Void> {
	//AssertStmt, BlockStmt, BreakStmt, ContinueStmt, DoStmt, EmptyStmt, ExplicitConstructorInvocationStmt, ExpressionStmt, ForEachStmt, ForStmt, IfStmt, LabeledStmt, LocalClassDeclarationStmt, ReturnStmt, SwitchStmt, SynchronizedStmt, ThrowStmt, TryStmt, UnparsableStmt, WhileStmt, YieldStmt
	
	private int lineNo;
	private Node node;
	
	public AttemptStmtVisitor(int lineNo){
		this.lineNo = lineNo;
	}
	
	public void takeAction(Node n){
		
	}
	
	@Override
	public void	visit(AssertStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	
	@Override
	public void	visit(BlockStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	
	@Override
	public void	visit(BreakStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	
	@Override
	public void	visit(ContinueStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(DoStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	
	@Override
	public void	visit(EmptyStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(ExplicitConstructorInvocationStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(ExpressionStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(ForEachStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	
	@Override
	public void	visit(ForStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(IfStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(LabeledStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(LocalClassDeclarationStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(ReturnStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(SwitchStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(SynchronizedStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(ThrowStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(TryStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(UnparsableStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(WhileStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}
	@Override
	public void	visit(YieldStmt n, Void arg) {
		super.visit(n, arg);
		takeAction(n);
	}	
}
