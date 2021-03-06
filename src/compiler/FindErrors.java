package compiler;

import compiler.accessModifier.AccessModifier;
import compiler.error.Compare;
import compiler.gen.MiniJavaBaseListener;
import compiler.gen.MiniJavaLexer;
import compiler.gen.MiniJavaParser;
import compiler.symbolTable.SymbolTable;
import compiler.symbolTable.symbolTableItem.*;
import compiler.types.Type;
import compiler.types.singleType.IntType;
import compiler.types.singleType.UserDefinedType;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import compiler.error.Error;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class FindErrors extends MiniJavaBaseListener {
    SymbolTable currentSymbolTable;

    private boolean CheckingReturnType;
    private Type ReturnType = null;
    private int sttnum;


    private MiniJavaParser.ClassDeclarationContext CurrClass;
    public static PriorityQueue<Error> errors = new PriorityQueue<>(new Compare());

    @Override
    public void enterProgram(MiniJavaParser.ProgramContext ctx) {
        // get symbolTable: (name_[lineNumber]_[column])  for example: program_1_0
        currentSymbolTable = SymbolTable.getSymbolTableByKey("program_" + ctx.start.getLine() + "_0");
        System.out.println(currentSymbolTable);
//        SymbolTable.printAll();
        // TODO
    }

    @Override
    public void exitProgram(MiniJavaParser.ProgramContext ctx) {
        for(Error e : errors){
            System.out.println(e);
        }
        // TODO
    }

    @Override
    public void enterMainClass(MiniJavaParser.MainClassContext ctx) {
        // TODO
    }

    @Override
    public void exitMainClass(MiniJavaParser.MainClassContext ctx) {
        // TODO
    }

    @Override
    public void enterClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        // TODO
        CurrClass = ctx;

        //Undefined class usage
        if (ctx.parentName != null && !ctx.parentName.getText().equals("String")) {
            if (SymbolTable.getSymbolTableByKey("program_1_0").get("class_" + ctx.parentName.getText()) == null) {
                Error err = new Error(105, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                        "cannot find  class " + ctx.parentName.getText());
                errors.add(err);
            }
        }

        if (((ClassSymbolTableItem) SymbolTable.getSymbolTableByKey("program_1_0")
                .get("class_" + ctx.className.getText())).parent.getName().equals("String")) {
            Error err = new Error(430, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "class " + ctx.className.getText() + " can not inherit from class String");
            errors.add(err);
        }

        ClassSymbolTableItem si = (ClassSymbolTableItem) SymbolTable.getSymbolTableByKey("program_1_0").get("class_" + ctx.className.getText());
        int j = 0;
        if (!si.parents.isEmpty()) {
            for (InterfaceSymbolTableItem i : si.parents) {
                Map<String, SymbolTableItem> m = SymbolTable.getSymbolTableByKey(i.getName() + "_" + i.lineNumber + "_" + i.column)
                        .getAllItems();

                for (Map.Entry<String, SymbolTableItem> e : m.entrySet()) {
                    if (e.getKey().contains("method_")) {
                        MethodSymbolTableItem mi = (MethodSymbolTableItem) e.getValue();
                        for (j = 0; ctx.methodDeclaration(j) != null; j++) {
                            if (!ctx.methodDeclaration(j).methodName.getText().
                                    equals(((MethodSymbolTableItem) e.getValue()).getName())) {

                                if (((MethodSymbolTableItem) e.getValue()).getReturnType().equals(mi.getReturnType())) {
                                    List<Type> l = mi.getArgumentsTypes();
                                    if (l.containsAll(((MethodSymbolTableItem) e.getValue()).getArgumentsTypes())) {
                                        j--;
                                        break;
                                    }
                                }
                            }
                        }
                        if (j == ctx.methodDeclaration().size()) {
                            Error err = new Error(420, mi.lineNumber, mi.column,
                                    " Class " + ctx.className.getText() + " must implement all abstract methods");
                            errors.add(err);
                        }
                    }
                }
            }
        }

        ArrayList<ClassSymbolTableItem> pars = new ArrayList<>();
        ClassSymbolTableItem currc = (ClassSymbolTableItem) SymbolTable.getSymbolTableByKey("program_1_0")
                .get("class_" + ctx.className.getText());
        StringBuilder sb = new StringBuilder();
        while (!currc.parent.getName().equals("Object")) {
            pars.add(currc);
            if (currc.parent instanceof ClassSymbolTableItem) {
                currc = (ClassSymbolTableItem) currc.parent;
                if(currc.parent == null){
                    break;
                }
            } else {
                break;
            }
            if (pars.contains(currc)) {
                pars.add(pars.get(0));
                for (int i = 0; i < pars.size(); i++) {
                    sb.append(pars.get(i).getName());
                    if (i < pars.size() - 1) {
                        sb.append(" -> ");
                    }
                }
                Error err = new Error(410, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                        "Invalid inheritance " + sb.toString());
                errors.add(err);
                break;

            }
        }

    }

    @Override
    public void exitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterInterfaceDeclaration(MiniJavaParser.InterfaceDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void exitInterfaceDeclaration(MiniJavaParser.InterfaceDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterInterfaceMethodDeclaration(MiniJavaParser.InterfaceMethodDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void exitInterfaceMethodDeclaration(MiniJavaParser.InterfaceMethodDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterFieldDeclaration(MiniJavaParser.FieldDeclarationContext ctx) {

    }

    @Override
    public void exitFieldDeclaration(MiniJavaParser.FieldDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterLocalDeclaration(MiniJavaParser.LocalDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void exitLocalDeclaration(MiniJavaParser.LocalDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        SymbolTable s = SymbolTable.getSymbolTableByKey(ctx.methodName.getText() + "_" + ctx.methodName.getLine() + "_" +
                ctx.methodName.getCharPositionInLine());
        MethodSymbolTableItem m;
        m = (MethodSymbolTableItem) s.getPreSymbolTable().get("method_" + ctx.methodName.getText());

        Type t = m.getReturnType();
        MiniJavaParser.ExpressionContext r = ctx.methodBody().expression();
        sttnum = ctx.methodBody().statement().size();
        MiniJavaParser.ExpressionContext e = ctx.methodBody().expression();
        if (e instanceof MiniJavaParser.PowExpressionContext) {
            if (!(t instanceof IntType)) {
                Error err = new Error(210, e.getStart().getLine(), e.getStart().getCharPositionInLine(),
                        "ReturnType of this method must be" + t.toString());
                errors.add(err);
            }
        } else if (e instanceof MiniJavaParser.ArrayLengthExpressionContext && !(t instanceof IntType)) {
            Error err = new Error(210, e.getStart().getLine(), e.getStart().getCharPositionInLine(),
                    "ReturnType of this method must be" + t.toString());
            errors.add(err);
        } else if (e instanceof MiniJavaParser.MethodCallExpressionContext) {
//            MiniJavaParser.MethodCallExpressionContext m = (MethodCall)
        }

        if (ctx.Override() != null) {
            ClassSymbolTableItem ci = (ClassSymbolTableItem) SymbolTable.getSymbolTableByKey("program_1_0").
                    get("class_" + CurrClass.className.getText());

            boolean found = false;
            if(CurrClass.parentName != null) {


                ClassSymbolTableItem si = (ClassSymbolTableItem) ci.parent;
                if (SymbolTable.getSymbolTableByKey(CurrClass.parentName.getText() + "_" + si.lineNumber + "_" + si.column)
                        .get("method_" + ctx.methodName.getText()) != null) {
                    System.out.println("SUPER");
                    found = true;
                }

                if(!found && ci.parents.size() > 0){
                    System.out.println("INTER");
                    List<InterfaceSymbolTableItem> inter = ((ClassSymbolTableItem)SymbolTable.getSymbolTableByKey(CurrClass.parentName.getText() + "_" + si.lineNumber + "_" + si.column)
                            .get(CurrClass.className.getText())).parents;

                    for(InterfaceSymbolTableItem i : inter){
                        Map<String, SymbolTableItem> els = SymbolTable.getSymbolTableByKey(i.getName() + "_" + i.lineNumber + "_" + i.column).getAllItems();
                        System.out.println(SymbolTable.getSymbolTableByKey(i.getName() + "_" + i.lineNumber + "_" + i.column));
                        for(Map.Entry<String, SymbolTableItem> en : els.entrySet()){
                            if(en.getValue() instanceof MethodSymbolTableItem && ctx.methodName.getText().equals(en.getValue().getName())){
                                found = true;
                            }
                        }
                    }
                }

                if(!found){
                    Error err = new Error(440, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                            "method does not override method from its superclass.");
                    errors.add(err);
                }

                MethodSymbolTableItem cmethod = (MethodSymbolTableItem) ci.getSymbolTable().get("method_" + ctx.methodName.getText());
                MethodSymbolTableItem smethod = (MethodSymbolTableItem) si.getSymbolTable().get("method_" + ctx.methodName.getText());

                if (cmethod.getAccessModifier().equals(AccessModifier.ACCESS_MODIFIER_PRIVATE) &&
                        smethod.getAccessModifier().equals(AccessModifier.ACCESS_MODIFIER_PUBLIC)) {
                    Error err = new Error(320, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                            "the access level cannot be more restrictive than the overridden method's access level");
                    errors.add(err);
                }

                if (!cmethod.getReturnType().equals(smethod.getReturnType())) {
                    Error err = new Error(240, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                            "the return type of the overriding method must be the same as that of the overridden method");
                }

                for (int i = 0; i < cmethod.getArgumentsTypes().size(); i++) {
                    if(!cmethod.getArgumentsTypes().get(i).equals(smethod.getArgumentsTypes().get(i))){
                        Error err = new Error(250, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                "the parameters of the overriding method must be the same as that of the overridden method");
                        errors.add(err);
                        break;
                    }
                }

            }
        }


    }

//         if(r.getAltNumber() == ){
//
//
//            }
//         }


    @Override
    public void exitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterParameterList(MiniJavaParser.ParameterListContext ctx) {
        // TODO
    }

    @Override
    public void exitParameterList(MiniJavaParser.ParameterListContext ctx) {
        // TODO
    }

    @Override
    public void enterParameter(MiniJavaParser.ParameterContext ctx) {
        // TODO

    }

    @Override
    public void exitParameter(MiniJavaParser.ParameterContext ctx) {
        // TODO
    }

    @Override
    public void enterMethodBody(MiniJavaParser.MethodBodyContext ctx) {
        // TODO
    }

    @Override
    public void exitMethodBody(MiniJavaParser.MethodBodyContext ctx) {
        // TODO
    }

    @Override
    public void enterType(MiniJavaParser.TypeContext ctx) {
        // TODO
        //Undefined class usage
        if (ctx.Identifier() != null && !ctx.Identifier().getText().equals("String")) {
            if (SymbolTable.getSymbolTableByKey("program_1_0").get("class_" + ctx.Identifier()) == null) {
                Error err = new Error(105, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                        "cannot find  class " + ctx.Identifier().getText());
                errors.add(err);
            }
        }
    }

    @Override
    public void exitType(MiniJavaParser.TypeContext ctx) {
        // TODO
    }

    @Override
    public void enterBooleanType(MiniJavaParser.BooleanTypeContext ctx) {
        // TODO
    }

    @Override
    public void exitBooleanType(MiniJavaParser.BooleanTypeContext ctx) {
        // TODO
    }

    @Override
    public void enterIntType(MiniJavaParser.IntTypeContext ctx) {
        // TODO
    }

    @Override
    public void exitIntType(MiniJavaParser.IntTypeContext ctx) {
        // TODO
    }

    @Override
    public void enterReturnType(MiniJavaParser.ReturnTypeContext ctx) {

    }

    @Override
    public void exitReturnType(MiniJavaParser.ReturnTypeContext ctx) {
        // TODO
    }

    @Override
    public void enterAccessModifier(MiniJavaParser.AccessModifierContext ctx) {
        // TODO
    }

    @Override
    public void exitAccessModifier(MiniJavaParser.AccessModifierContext ctx) {
        // TODO
    }

    @Override
    public void enterNestedStatement(MiniJavaParser.NestedStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitNestedStatement(MiniJavaParser.NestedStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterIfElseStatement(MiniJavaParser.IfElseStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitIfElseStatement(MiniJavaParser.IfElseStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterWhileStatement(MiniJavaParser.WhileStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitWhileStatement(MiniJavaParser.WhileStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterPrintStatement(MiniJavaParser.PrintStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitPrintStatement(MiniJavaParser.PrintStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterVariableAssignmentStatement(MiniJavaParser.VariableAssignmentStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitVariableAssignmentStatement(MiniJavaParser.VariableAssignmentStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterArrayAssignmentStatement(MiniJavaParser.ArrayAssignmentStatementContext ctx) {
        // TODO
    }

    @Override
    public void exitArrayAssignmentStatement(MiniJavaParser.ArrayAssignmentStatementContext ctx) {
        // TODO
    }

    @Override
    public void enterLocalVarDeclaration(MiniJavaParser.LocalVarDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void exitLocalVarDeclaration(MiniJavaParser.LocalVarDeclarationContext ctx) {
        // TODO
    }

    @Override
    public void enterExpressioncall(MiniJavaParser.ExpressioncallContext ctx) {



    }

    @Override
    public void exitExpressioncall(MiniJavaParser.ExpressioncallContext ctx) {
        // TODO
    }

    @Override
    public void enterIfBlock(MiniJavaParser.IfBlockContext ctx) {
        // TODO
    }

    @Override
    public void exitIfBlock(MiniJavaParser.IfBlockContext ctx) {
        // TODO
    }

    @Override
    public void enterElseBlock(MiniJavaParser.ElseBlockContext ctx) {
        // TODO
    }

    @Override
    public void exitElseBlock(MiniJavaParser.ElseBlockContext ctx) {
        // TODO
    }

    @Override
    public void enterWhileBlock(MiniJavaParser.WhileBlockContext ctx) {
        // TODO
    }

    @Override
    public void exitWhileBlock(MiniJavaParser.WhileBlockContext ctx) {
        // TODO
    }

    @Override
    public void enterLtExpression(MiniJavaParser.LtExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitLtExpression(MiniJavaParser.LtExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterObjectInstantiationExpression(MiniJavaParser.ObjectInstantiationExpressionContext ctx) {
    }

    @Override
    public void exitObjectInstantiationExpression(MiniJavaParser.ObjectInstantiationExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterArrayInstantiationExpression(MiniJavaParser.ArrayInstantiationExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitArrayInstantiationExpression(MiniJavaParser.ArrayInstantiationExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterPowExpression(MiniJavaParser.PowExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitPowExpression(MiniJavaParser.PowExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitIdentifierExpression(MiniJavaParser.IdentifierExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitMethodCallExpression(MiniJavaParser.MethodCallExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterNotExpression(MiniJavaParser.NotExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitNotExpression(MiniJavaParser.NotExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterBooleanLitExpression(MiniJavaParser.BooleanLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitBooleanLitExpression(MiniJavaParser.BooleanLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterParenExpression(MiniJavaParser.ParenExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitParenExpression(MiniJavaParser.ParenExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterIntLitExpression(MiniJavaParser.IntLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitIntLitExpression(MiniJavaParser.IntLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterNullLitExpression(MiniJavaParser.NullLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitNullLitExpression(MiniJavaParser.NullLitExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterAndExpression(MiniJavaParser.AndExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitAndExpression(MiniJavaParser.AndExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterArrayAccessExpression(MiniJavaParser.ArrayAccessExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitArrayAccessExpression(MiniJavaParser.ArrayAccessExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterAddExpression(MiniJavaParser.AddExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitAddExpression(MiniJavaParser.AddExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterThisExpression(MiniJavaParser.ThisExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitThisExpression(MiniJavaParser.ThisExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterFieldCallExpression(MiniJavaParser.FieldCallExpressionContext ctx) {
//        System.out.println(ctx.expression());
    }

    @Override
    public void exitFieldCallExpression(MiniJavaParser.FieldCallExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterArrayLengthExpression(MiniJavaParser.ArrayLengthExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitArrayLengthExpression(MiniJavaParser.ArrayLengthExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterIntarrayInstantiationExpression(MiniJavaParser.IntarrayInstantiationExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitIntarrayInstantiationExpression(MiniJavaParser.IntarrayInstantiationExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterSubExpression(MiniJavaParser.SubExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitSubExpression(MiniJavaParser.SubExpressionContext ctx) {
        // TODO
    }

    @Override
    public void enterMulExpression(MiniJavaParser.MulExpressionContext ctx) {
        // TODO
    }

    @Override
    public void exitMulExpression(MiniJavaParser.MulExpressionContext ctx) {
        // TODO
    }


    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        // TODO
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // TODO
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        // TODO
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        // TODO
    }
}